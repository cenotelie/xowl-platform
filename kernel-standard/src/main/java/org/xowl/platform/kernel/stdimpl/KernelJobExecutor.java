/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.xowl.platform.kernel.stdimpl;

import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.RichString;
import fr.cenotelie.commons.utils.SHA1;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.*;
import fr.cenotelie.commons.utils.http.HttpConstants;
import fr.cenotelie.commons.utils.http.HttpResponse;
import fr.cenotelie.commons.utils.http.URIUtils;
import fr.cenotelie.commons.utils.ini.IniDocument;
import fr.cenotelie.commons.utils.json.Json;
import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.commons.utils.metrics.Metric;
import fr.cenotelie.commons.utils.metrics.MetricSnapshot;
import fr.cenotelie.commons.utils.metrics.MetricSnapshotInt;
import fr.cenotelie.commons.utils.metrics.MetricSnapshotLong;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.jobs.JobStatus;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements a job execution service
 *
 * @author Laurent Wouters
 */
public class KernelJobExecutor implements JobExecutionService, ManagedService, HttpApiService {
    /**
     * The bound of the executor queue
     */
    private static final int EXECUTOR_QUEUE_BOUND = 128;
    /**
     * The executor core pool size
     */
    private static final int EXECUTOR_POOL_MIN = 8;
    /**
     * The executor max pool size
     */
    private static final int EXECUTOR_POOL_MAX = 16;
    /**
     * The bound of the buffer of completed jobs
     */
    private static final int COMPLETED_QUEUED_BOUND = EXECUTOR_QUEUE_BOUND;

    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(KernelJobExecutor.class, "/org/xowl/platform/kernel/stdimpl/api_jobs.raml", "Jobs Management Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(KernelJobExecutor.class, "/org/xowl/platform/kernel/stdimpl/api_jobs.html", "Jobs Management Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The URI for the API services
     */
    private final String apiUri;
    /**
     * The queue to use before the executor is activated
     */
    private final ConcurrentLinkedQueue<Runnable> initQueue;
    /**
     * Whether this executor can begin executing jobs
     */
    private final AtomicBoolean canExecute;
    /**
     * The pool of executor threads
     */
    private final ThreadPoolExecutor executorPool;
    /**
     * The directory for the persistent storage of queued job
     */
    private final File storage;
    /**
     * The buffer of completed jobs
     */
    private final Job[] completed;
    /**
     * The index of the first completed job in the buffer
     */
    private int completedStart;
    /**
     * The buffer of running jobs
     */
    private final Job[] running;

    /**
     * Initializes this service
     *
     * @param configurationService The configuration service to use
     */
    public KernelJobExecutor(ConfigurationService configurationService) {
        IniDocument configuration = configurationService.getConfigFor(JobExecutionService.class.getCanonicalName());
        int queueBound = EXECUTOR_QUEUE_BOUND;
        int poolMin = EXECUTOR_POOL_MIN;
        int poolMax = EXECUTOR_POOL_MAX;
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/kernel/jobs";
        this.storage = PlatformUtils.resolve(configuration.get("storage"));
        try {
            String value = configuration.get("queueBound");
            if (value != null)
                queueBound = Integer.parseInt(value);
            value = configuration.get("poolMinThreads");
            if (value != null)
                poolMin = Integer.parseInt(value);
            value = configuration.get("poolMaxThreads");
            if (value != null)
                poolMax = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            Logging.get().error(exception);
        }

        this.initQueue = new ConcurrentLinkedQueue<>();
        this.canExecute = new AtomicBoolean(false);
        this.executorPool = new ThreadPoolExecutor(poolMin, poolMax, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueBound)) {
            @Override
            protected void beforeExecute(Thread thread, Runnable runnable) {
                onJobRun((Job) runnable);
            }

            @Override
            protected void afterExecute(Runnable runnable, Throwable throwable) {
                onJobFinished((Job) runnable);
            }
        };
        this.completed = new Job[COMPLETED_QUEUED_BOUND];
        this.completedStart = -1;
        this.running = new Job[EXECUTOR_POOL_MAX];
        reloadQueue();
    }

    /**
     * Action when a job is going to run
     *
     * @param job The job
     */
    private void onJobRun(Job job) {
        // register as running
        synchronized (running) {
            for (int i = 0; i != running.length; i++) {
                if (running[i] == null) {
                    running[i] = job;
                    break;
                }
            }
        }
        // event before running
        job.onRun();
        Logging.get().info(new RichString("Begin running job ", job));
    }

    /**
     * Action when a job is finished
     *
     * @param job The finished job
     */
    private void onJobFinished(Job job) {
        // register as completed
        synchronized (completed) {
            // remove from the running
            synchronized (running) {
                for (int i = 0; i != running.length; i++) {
                    if (running[i] == job) {
                        running[i] = null;
                        break;
                    }
                }
            }
            completedStart++;
            if (completedStart == COMPLETED_QUEUED_BOUND)
                completedStart = 0;
            completed[completedStart] = job;
        }


        // delete the serialized definition
        File file = new File(storage, getFileName(job));
        if (file.exists()) {
            if (!file.delete()) {
                Logging.get().error("Failed to delete " + file.getAbsolutePath());
            }
        }
        // callback on completion
        job.onTerminated(job.getStatus() == JobStatus.Cancelled);
        Logging.get().info(new RichString("Ended job ", job));
    }

    /**
     * Tries to reload the queue
     */
    private void reloadQueue() {
        if (!storage.exists())
            return;
        File[] files = storage.listFiles();
        if (files != null) {
            for (int i = 0; i != files.length; i++) {
                if (isJobFile(files[i].getName())) {
                    try (Reader reader = IOUtils.getReader(files[i].getAbsolutePath())) {
                        String content = IOUtils.read(reader);
                        reloadJob(files[i], content);
                    } catch (IOException exception) {
                        Logging.get().error(exception);
                    }
                }
            }
        }
    }

    /**
     * Tries to reload a job
     *
     * @param file    The job's file
     * @param content The job's content
     */
    private void reloadJob(File file, String content) {
        ASTNode definition = Json.parse(Logging.get(), content);
        if (definition == null) {
            Logging.get().error("Failed to parse the job " + file.getAbsolutePath());
            return;
        }
        String type = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            ASTNode nodeValue = member.getChildren().get(1);
            String value = null;
            if (nodeValue.getValue() != null) {
                value = TextUtils.unescape(nodeValue.getValue());
                value = value.substring(1, value.length() - 1);
            }
            if ("jobType".equals(head)) {
                type = value;
            }
        }
        if (type == null) {
            Logging.get().error("Unknown job type " + file);
            return;
        }
        Collection<JobFactory> factories = Register.getComponents(JobFactory.class);
        for (JobFactory factory : factories) {
            Job job = factory.newJob(type, definition);
            if (job != null) {
                executorPool.execute(job);
                return;
            }
        }
        Logging.get().error("Could not find a factory for job " + file.getAbsolutePath());
    }

    @Override
    public String getIdentifier() {
        return KernelJobExecutor.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Jobs Management Service";
    }

    @Override
    public int getLifecycleTier() {
        return TIER_ASYNC;
    }

    @Override
    public void onLifecycleStart() {
        // activate this executor
        synchronized (initQueue) {
            canExecute.set(true);
            while (!initQueue.isEmpty()) {
                executorPool.execute(initQueue.poll());
            }
        }
    }

    @Override
    public void onLifecycleStop() {
        if (executorPool != null) {
            executorPool.shutdownNow();
        }
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public Collection<Metric> getMetrics() {
        return Arrays.asList(METRIC_TOTAL_PROCESSED_JOBS, METRIC_SCHEDULED_JOBS, METRIC_EXECUTING_JOBS);
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        if (metric == METRIC_TOTAL_PROCESSED_JOBS)
            return new MetricSnapshotLong(executorPool.getCompletedTaskCount());
        if (metric == METRIC_SCHEDULED_JOBS)
            return new MetricSnapshotInt(executorPool.getQueue().size());
        if (metric == METRIC_EXECUTING_JOBS)
            return new MetricSnapshotInt(executorPool.getActiveCount());
        return null;
    }

    @Override
    public Reply schedule(Job job) {
        boolean exists = storage.exists();
        if (!exists) {
            exists = storage.mkdirs();
        }
        if (exists) {
            try (Writer write = IOUtils.getWriter(new File(storage, getFileName(job)).getAbsolutePath())) {
                write.write(job.serializedJSON());
            } catch (IOException exception) {
                Logging.get().error(exception);
            }
        } else {
            Logging.get().error("Cannot serialize the job, storage is inaccessible");
        }
        job.onScheduled();
        synchronized (initQueue) {
            if (canExecute.get()) {
                executorPool.execute(job);
            } else {
                initQueue.add(job);
            }
        }
        Logging.get().info(new RichString("Scheduled job ", job));
        return new ReplyResult<>(job);
    }

    @Override
    public Reply cancel(Job job) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_CANCEL, job);
        if (!reply.isSuccess())
            return reply;

        boolean success = executorPool.remove(job);
        if (success) {
            // the job was queued and prevented from running
            job.onTerminated(true);
            return ReplySuccess.instance();
        }
        switch (job.getStatus()) {
            case Unscheduled:
            case Scheduled:
                job.onTerminated(true);
                return ReplySuccess.instance();
            case Running:
                return job.cancel();
            case Completed:
                return new ReplyApiError(ERROR_ALREADY_COMPLETED);
            case Cancelled:
                return new ReplyApiError(ERROR_ALREADY_CANCELLED);
            default:
                return new ReplyApiError(ERROR_INVALID_JOB_STATE, job.getStatus().toString());
        }
    }

    @Override
    public List<Job> getQueue() {
        List<Job> result = new ArrayList<>();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return result;

        for (Runnable runnable : executorPool.getQueue()) {
            Job job = (Job) runnable;
            if (securityService.checkAction(ACTION_GET_JOBS, job).isSuccess())
                result.add(job);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Job> getRunning() {
        List<Job> result = new ArrayList<>();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return result;

        synchronized (running) {
            for (int i = 0; i != running.length; i++) {
                if (running[i] != null && (securityService.checkAction(ACTION_GET_JOBS, running[i]).isSuccess()))
                    result.add(running[i]);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Job> getCompleted() {
        List<Job> result = new ArrayList<>();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return result;

        synchronized (completed) {
            for (int i = completedStart; i != -1; i--) {
                if (completed[i] != null && (securityService.checkAction(ACTION_GET_JOBS, completed[i]).isSuccess()))
                    result.add(completed[i]);
            }
            for (int i = COMPLETED_QUEUED_BOUND - 1; i != completedStart; i--) {
                if (completed[i] != null && (securityService.checkAction(ACTION_GET_JOBS, completed[i]).isSuccess()))
                    result.add(completed[i]);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Job getJob(String identifier, JobStatus expectedStatus) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;

        Job job = getJobScheduled(identifier);
        if (job != null) {
            if ((securityService.checkAction(ACTION_GET_JOBS, job).isSuccess()))
                return job;
            return null;
        }
        job = getJobRunning(identifier);
        if (job != null) {
            if ((securityService.checkAction(ACTION_GET_JOBS, job).isSuccess()))
                return job;
            return null;
        }
        job = getJobCompleted(identifier);
        if (job != null) {
            if ((securityService.checkAction(ACTION_GET_JOBS, job).isSuccess()))
                return job;
            return null;
        }
        return null;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public boolean requireAuth(HttpApiRequest request) {
        return true;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (request.getUri().equals(apiUri)) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return onRequestJobs();
        }

        if (request.getUri().startsWith(apiUri)) {
            String rest = request.getUri().substring(apiUri.length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String jobId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                Job job = getJob(jobId, JobStatus.Completed);
                if (job == null)
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
            } else if (rest.substring(index).equals("/cancel")) {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                Job job = getJob(jobId, JobStatus.Completed);
                if (job == null)
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                return ReplyUtils.toHttpResponse(cancel(job));
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Override
    public HttpApiResource getApiSpecification() {
        return RESOURCE_SPECIFICATION;
    }

    @Override
    public HttpApiResource getApiDocumentation() {
        return RESOURCE_DOCUMENTATION;
    }

    @Override
    public HttpApiResource[] getApiOtherResources() {
        return null;
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(HttpApiService.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"specification\": " +
                RESOURCE_SPECIFICATION.serializedJSON() +
                ", \"documentation\": " +
                RESOURCE_DOCUMENTATION.serializedJSON() +
                "}";
    }

    /**
     * Looks for a job in the queue
     *
     * @param identifier The identifier of a job
     * @return The job, or null if it is not found
     */
    private Job getJobScheduled(String identifier) {
        for (Runnable runnable : executorPool.getQueue()) {
            Job job = (Job) runnable;
            if (job.getIdentifier().equals(identifier))
                return job;
        }
        return null;
    }

    /**
     * Looks for a job in the running buffer
     *
     * @param identifier The identifier of a job
     * @return The job, or null if it is not found
     */
    private Job getJobRunning(String identifier) {
        synchronized (running) {
            for (int i = 0; i != running.length; i++) {
                if (running[i] != null && running[i].getIdentifier().equals(identifier))
                    return running[i];
            }
            return null;
        }
    }

    /**
     * Looks for a job in the completed buffer
     *
     * @param identifier The identifier of a job
     * @return The job, or null if it is not found
     */
    private Job getJobCompleted(String identifier) {
        synchronized (completed) {
            if (completedStart < 0)
                return null;
            for (int i = 0; i != completed.length; i++) {
                if (completed[i] == null)
                    return null;
                if (completed[i].getIdentifier().equals(identifier))
                    return completed[i];
            }
        }
        return null;
    }

    /**
     * Responds to a request for the queue
     *
     * @return The response
     */
    private HttpResponse onRequestJobs() {
        List<Job> scheduled = getQueue();
        List<Job> running = getRunning();
        List<Job> completed = getCompleted();
        boolean first = true;
        StringWriter builder = new StringWriter();
        builder.append("[");
        for (Job job : scheduled) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(job.serializedJSON());
        }
        for (Job job : running) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(job.serializedJSON());
        }
        for (Job job : completed) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(job.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Gets the file name for a job
     *
     * @param job The job
     * @return The file name
     */
    private static String getFileName(Job job) {
        return "job-" + SHA1.hashSHA1(job.getIdentifier()) + ".json";
    }

    /**
     * Gets whether a file name is a serialized job
     *
     * @param name The name of a file
     * @return Whether this is a serialized job
     */
    private static boolean isJobFile(String name) {
        return name.startsWith("job-") && name.endsWith(".json");
    }
}
