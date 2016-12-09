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

package org.xowl.platform.kernel.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.*;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.infra.utils.metrics.MetricSnapshotInt;
import org.xowl.infra.utils.metrics.MetricSnapshotLong;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.jobs.JobStatus;
import org.xowl.platform.kernel.platform.PlatformRebootJob;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implements a job execution service
 *
 * @author Laurent Wouters
 */
public class XOWLJobExecutor implements JobExecutionService, HttpApiService, Closeable {
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
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/kernel/jobs";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_jobs.raml", "Jobs Management Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_jobs.html", "Jobs Management Service - Documentation", HttpApiResource.MIME_HTML);


    /**
     * API error - The job is already cancelled
     */
    public static final ApiError ERROR_ALREADY_CANCELLED = new ApiError(0x0011,
            "The job is already cancelled.",
            ERROR_HELP_PREFIX + "0x0011.html");
    /**
     * API error - The job is already completed
     */
    public static final ApiError ERROR_ALREADY_COMPLETED = new ApiError(0x0012,
            "The job is already completed.",
            ERROR_HELP_PREFIX + "0x0012.html");
    /**
     * API error - Invalid job state
     */
    public static final ApiError ERROR_INVALID_JOB_STATE = new ApiError(0x0013,
            "The job is already completed.",
            ERROR_HELP_PREFIX + "0x0013.html");

    /**
     * The pool of executor threads
     */
    private ThreadPoolExecutor executorPool;
    /**
     * The directory for the persistent storage of queued job
     */
    private File storage;
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
     */
    public XOWLJobExecutor() {
        this.completed = new Job[COMPLETED_QUEUED_BOUND];
        this.completedStart = -1;
        this.running = new Job[EXECUTOR_POOL_MAX];
    }

    /**
     * Gets the executor pool
     *
     * @return The executor pool
     */
    private ThreadPoolExecutor getExecutorPool() {
        if (executorPool == null) {
            ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
            Configuration configuration = configurationService != null ? configurationService.getConfigFor(this) : null;
            int queueBound = EXECUTOR_QUEUE_BOUND;
            int poolMin = EXECUTOR_POOL_MIN;
            int poolMax = EXECUTOR_POOL_MAX;
            if (configuration != null) {
                String value = configuration.get("storage");
                if (value != null)
                    storage = new File(value);
                else
                    storage = new File(System.getProperty("user.dir"));
                try {
                    value = configuration.get("queueBound");
                    if (value != null)
                        queueBound = Integer.parseInt(value);
                    value = configuration.get("poolMinThreads");
                    if (value != null)
                        poolMin = Integer.parseInt(value);
                    value = configuration.get("poolMaxThreads");
                    if (value != null)
                        poolMax = Integer.parseInt(value);
                } catch (NumberFormatException exception) {
                    // do nothing
                }
            }
            ArrayBlockingQueue<Runnable> executorQueue = new ArrayBlockingQueue<>(queueBound);
            executorPool = new ThreadPoolExecutor(poolMin, poolMax, 0, TimeUnit.SECONDS, executorQueue) {
                @Override
                protected void beforeExecute(Thread thread, Runnable runnable) {
                    onJobRun((Job) runnable);
                }

                @Override
                protected void afterExecute(Runnable runnable, Throwable throwable) {
                    onJobFinished((Job) runnable);
                }
            };
            reloadQueue();
        }
        return executorPool;
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
        Logging.getDefault().info(new RichString("Begin running job ", job));
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
                Logging.getDefault().error("Failed to delete " + file.getAbsolutePath());
            }
        }
        // callback on completion
        job.onTerminated(job.getStatus() == JobStatus.Cancelled);
        Logging.getDefault().info(new RichString("Ended job ", job));
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
                    try (Reader reader = Files.getReader(files[i].getAbsolutePath())) {
                        String content = Files.read(reader);
                        reloadJob(files[i], content);
                    } catch (IOException exception) {
                        Logging.getDefault().error(exception);
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
        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
        if (definition == null) {
            Logging.getDefault().error("Failed to parse the job " + file.getAbsolutePath());
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
            Logging.getDefault().error("Unknown job type " + file);
            return;
        }
        if (type.equals(PlatformRebootJob.class.getCanonicalName())) {
            // do not reload reboot job
            if (!file.delete()) {
                Logging.getDefault().error("Failed to delete " + file.getAbsolutePath());
            }
        }
        Collection<JobFactory> factories = ServiceUtils.getServices(JobFactory.class);
        for (JobFactory factory : factories) {
            if (factory.canDeserialize(type)) {
                Job job = factory.newJob(type, definition);
                if (job != null)
                    executorPool.execute(job);
                return;
            }
        }
        Logging.getDefault().error("Could not find a factory for job " + file.getAbsolutePath());
    }

    @Override
    public void close() {
        if (executorPool != null) {
            executorPool.shutdownNow();
        }
    }

    @Override
    public String getIdentifier() {
        return XOWLJobExecutor.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Jobs Management Service";
    }

    @Override
    public Collection<Metric> getMetrics() {
        return Arrays.asList(METRIC_TOTAL_PROCESSED_JOBS, METRIC_SCHEDULED_JOBS, METRIC_EXECUTING_JOBS);
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        if (metric == METRIC_TOTAL_PROCESSED_JOBS)
            return new MetricSnapshotLong(getExecutorPool().getCompletedTaskCount());
        if (metric == METRIC_SCHEDULED_JOBS)
            return new MetricSnapshotInt(getExecutorPool().getQueue().size());
        if (metric == METRIC_EXECUTING_JOBS)
            return new MetricSnapshotInt(getExecutorPool().getActiveCount());
        return null;
    }

    @Override
    public void schedule(Job job) {
        ThreadPoolExecutor pool = getExecutorPool();
        boolean exists = storage.exists();
        if (!exists) {
            exists = storage.mkdirs();
        }
        if (exists) {
            try (Writer write = Files.getWriter(new File(storage, getFileName(job)).getAbsolutePath())) {
                write.write(job.serializedJSON());
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
            }
        } else {
            Logging.getDefault().error("Cannot serialize the job, storage is inaccessible");
        }
        job.onScheduled();
        pool.execute(job);
        Logging.getDefault().info(new RichString("Scheduled job ", job));
    }

    @Override
    public XSPReply cancel(Job job) {
        boolean success = getExecutorPool().remove(job);
        if (success) {
            // the job was queued and prevented from running
            job.onTerminated(true);
            return XSPReplySuccess.instance();
        }
        switch (job.getStatus()) {
            case Unscheduled:
            case Scheduled:
                job.onTerminated(true);
                return XSPReplySuccess.instance();
            case Running:
                return job.cancel();
            case Completed:
                return new XSPReplyApiError(ERROR_ALREADY_COMPLETED);
            case Cancelled:
                return new XSPReplyApiError(ERROR_ALREADY_CANCELLED);
            default:
                return new XSPReplyApiError(ERROR_INVALID_JOB_STATE, job.getStatus().toString());
        }
    }

    @Override
    public List<Job> getQueue() {
        List<Job> result = new ArrayList<>();
        for (Runnable runnable : getExecutorPool().getQueue())
            result.add((Job) runnable);
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Job> getRunning() {
        List<Job> result = new ArrayList<>();
        synchronized (running) {
            for (int i = 0; i != running.length; i++) {
                if (running[i] != null)
                    result.add(running[i]);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Job> getCompleted() {
        List<Job> result = new ArrayList<>();
        synchronized (completed) {
            for (int i = completedStart; i != -1; i--) {
                result.add(completed[i]);
            }
            for (int i = COMPLETED_QUEUED_BOUND - 1; i != completedStart; i--) {
                if (completed[i] == null)
                    break;
                result.add(completed[i]);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Job getJob(String identifier, JobStatus expectedStatus) {
        Job job = getJobScheduled(identifier);
        if (job != null)
            return job;
        job = getJobRunning(identifier);
        if (job != null)
            return job;
        return getJobCompleted(identifier);
    }

    /**
     * Looks for a job in the queue
     *
     * @param identifier The identifier of a job
     * @return The job, or null if it is not found
     */
    private Job getJobScheduled(String identifier) {
        for (Runnable runnable : getExecutorPool().getQueue()) {
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

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);

        if (request.getUri().equals(URI_API)) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return onRequestJobs(securityService);
        }

        if (request.getUri().startsWith(URI_API)) {
            String rest = request.getUri().substring(URI_API.length() + 1);
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
                if (securityService.getCurrentUser().equals(job.getOwner()))
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
                return new HttpResponse(HttpURLConnection.HTTP_FORBIDDEN);
            } else if (rest.substring(index).equals("/cancel")) {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                Job job = getJob(jobId, JobStatus.Completed);
                if (job == null)
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                if (securityService.getCurrentUser().equals(job.getOwner()))
                    return XSPReplyUtils.toHttpResponse(cancel(job), null);
                return new HttpResponse(HttpURLConnection.HTTP_FORBIDDEN);
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
     * Responds to a request for the queue
     *
     * @param securityService The current security service
     * @return The response
     */
    private HttpResponse onRequestJobs(SecurityService securityService) {
        // check for platform admin role
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

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
