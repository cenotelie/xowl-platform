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

package org.xowl.platform.kernel.jobs;

import org.xowl.infra.utils.api.ApiError;
import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.collections.Couple;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricBase;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.statistics.MeasurableService;

import java.util.List;

/**
 * A service for the execution of background jobs
 *
 * @author Laurent Wouters
 */
public interface JobExecutionService extends SecuredService, MeasurableService {
    /**
     * The metric for the total number of processed jobs
     */
    Metric METRIC_TOTAL_PROCESSED_JOBS = new MetricBase(JobExecutionService.class.getCanonicalName() + ".TotalProcessedJobs",
            "Job Execution Service - Total processed jobs",
            "jobs",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));
    /**
     * The metric for the number of scheduled jobs
     */
    Metric METRIC_SCHEDULED_JOBS = new MetricBase(JobExecutionService.class.getCanonicalName() + ".ScheduledJobs",
            "Job Execution Service - Scheduled jobs",
            "jobs",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));
    /**
     * The metric for the number of executing jobs
     */
    Metric METRIC_EXECUTING_JOBS = new MetricBase(JobExecutionService.class.getCanonicalName() + ".ExecutingJobs",
            "Job Execution Service - Executing jobs",
            "jobs",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));

    /**
     * Service action to get the current jobs
     */
    SecuredAction ACTION_GET_JOBS = new SecuredAction(JobExecutionService.class.getCanonicalName() + ".GetJobs", "Jobs Management Service - Get Jobs", SecuredActionPolicyIsJobOwner.DESCRIPTOR);
    /**
     * Service action to cancel running jobs
     */
    SecuredAction ACTION_CANCEL = new SecuredAction(JobExecutionService.class.getCanonicalName() + ".Cancel", "Jobs Management Service - Cancel running job", SecuredActionPolicyIsJobOwner.DESCRIPTOR);

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_GET_JOBS,
            ACTION_CANCEL
    };

    /**
     * API error - The job is already cancelled
     */
    ApiError ERROR_ALREADY_CANCELLED = new ApiError(0x00000011,
            "The job is already cancelled.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000011.html");
    /**
     * API error - The job is already completed
     */
    ApiError ERROR_ALREADY_COMPLETED = new ApiError(0x00000012,
            "The job is already completed.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000012.html");
    /**
     * API error - Invalid job state
     */
    ApiError ERROR_INVALID_JOB_STATE = new ApiError(0x00000013,
            "The job is already completed.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000013.html");

    /**
     * Schedules a job for execution
     *
     * @param job The job to execute
     * @return The operation's result
     */
    Reply schedule(Job job);

    /**
     * Cancels a job
     *
     * @param job The job to cancel
     * @return The operation's result
     */
    Reply cancel(Job job);

    /**
     * Gets the current queue
     *
     * @return The current queue
     */
    List<Job> getQueue();

    /**
     * Gets the running jobs
     *
     * @return The running jobs
     */
    List<Job> getRunning();

    /**
     * Gets the completed jobs
     *
     * @return The completed jobs
     */
    List<Job> getCompleted();

    /**
     * Gets the job from a job's identifier
     *
     * @param identifier     The identifier of the job to retrieve
     * @param expectedStatus The expected status of the job
     * @return The job, or null if it cannot be found
     */
    Job getJob(String identifier, JobStatus expectedStatus);
}
