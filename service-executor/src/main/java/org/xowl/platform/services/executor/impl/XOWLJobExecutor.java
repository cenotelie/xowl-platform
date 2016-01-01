/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.executor.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.*;
import org.xowl.platform.services.config.ConfigurationService;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.platform.utils.Utils;
import org.xowl.store.IOUtils;
import org.xowl.utils.Files;
import org.xowl.utils.config.Configuration;
import org.xowl.utils.logging.Logger;

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
public class XOWLJobExecutor implements JobExecutionService, HttpAPIService {
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
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "connectors",
            "domains"
    };


    /**
     * The pool of executor threads
     */
    private ThreadPoolExecutor executorPool;
    /**
     * The directory for the persistent storage of queued job
     */
    private File storage;

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
                protected void afterExecute(Runnable runnable, Throwable throwable) {
                    onJobFinished((Job) runnable);
                }
            };
            reloadQueue();
        }
        return executorPool;
    }

    /**
     * Action when a job is finished
     *
     * @param job the finished job
     */
    private void onJobFinished(Job job) {
        File file = new File(storage, getFileName(job));
        if (file.exists()) {
            if (!file.delete()) {
                Logger.DEFAULT.error("Failed to delete " + file.getAbsolutePath());
            }
        }
        job.onComplete();
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
                        reloadJob(files[i].getAbsolutePath(), content);
                    } catch (IOException exception) {
                        Logger.DEFAULT.error(exception);
                    }
                }
            }
        }
    }

    /**
     * Tries to reload a job
     *
     * @param file    The name of the file
     * @param content The job's content
     */
    private void reloadJob(String file, String content) {
        ASTNode definition = Utils.parseJSON(Logger.DEFAULT, content);
        if (definition == null) {
            Logger.DEFAULT.error("Failed to parse the job " + file);
            return;
        }
        String type = null;
        for (ASTNode member : definition.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            String value = IOUtils.unescape(member.getChildren().get(1).getValue());
            head = head.substring(1, head.length() - 1);
            if ("type".equals(head)) {
                type = value;
            }
        }
        if (type == null) {
            Logger.DEFAULT.error("Unknown job type " + file);
            return;
        }
        JobFactory factory = ServiceUtils.getService(JobFactory.class, "type", type);
        if (factory == null) {
            Logger.DEFAULT.error("Could not find a factory for job " + file);
            return;
        }
        Job job = factory.newJob(type, definition);
        if (job != null)
            executorPool.execute(job);
    }

    @Override
    public String getIdentifier() {
        return XOWLJobExecutor.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Job Execution Service";
    }

    @Override
    public String getProperty(String name) {
        if (name == null)
            return null;
        if ("identifier".equals(name))
            return getIdentifier();
        if ("name".equals(name))
            return getName();
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
                Logger.DEFAULT.error(exception);
            }
        } else {
            Logger.DEFAULT.error("Cannot serialize the job, storage is inaccessible");
        }
        pool.execute(job);
    }

    @Override
    public void cancel(Job job) {
        getExecutorPool().remove(job);
    }

    @Override
    public boolean isScheduled(Job job) {
        return getExecutorPool().getQueue().contains(job);
    }

    @Override
    public List<Job> getQueue() {
        List<Job> result = new ArrayList<>();
        for (Runnable runnable : getExecutorPool().getQueue())
            result.add((Job) runnable);
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        List<Job> queue = getQueue();
        StringWriter builder = new StringWriter();
        builder.append("{\"type\": \"");
        builder.append(IOUtils.escapeStringJSON(JobExecutionService.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(IOUtils.escapeStringJSON(getIdentifier()));
        builder.append("\", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(getName()));
        builder.append("\", \"executed\": ");
        builder.append(Long.toString(getExecutorPool().getCompletedTaskCount()));
        builder.append(", \"queue\": [");
        for (int i = 0; i != queue.size(); i++) {
            if (i == 0)
                builder.append(", ");
            builder.append(queue.get(i).serializedJSON());
        }
        builder.append("]}");
        return new HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, builder.toString());
    }

    /**
     * Gets the file name for a job
     *
     * @param job The job
     * @return The file name
     */
    private static String getFileName(Job job) {
        return "job-" + Utils.encode(job.getIdentifier()) + ".json";
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
