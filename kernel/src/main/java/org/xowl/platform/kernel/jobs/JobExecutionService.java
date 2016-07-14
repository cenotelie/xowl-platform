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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.Service;

import java.util.List;

/**
 * A service for the execution of background jobs
 *
 * @author Laurent Wouters
 */
public interface JobExecutionService extends Service {
    /**
     * Schedules a job for execution
     *
     * @param job The job to execute
     */
    void schedule(Job job);

    /**
     * Cancels a job
     *
     * @param job The job to cancel
     * @return The operation's result
     */
    XSPReply cancel(Job job);

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
