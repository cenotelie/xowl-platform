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

package org.xowl.platform.kernel;

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
     */
    void cancel(Job job);

    /**
     * Gets the current queue
     *
     * @return The current queue
     */
    List<Job> getQueue();

    /**
     * Retrieves a scheduled (or running) job from an identifier
     *
     * @param identifier The identifier of a job
     * @return The corresponding job
     */
    Job getScheduledJob(String identifier);

    /**
     * Retrieves a completed job from an identifier
     *
     * @param identifier The identifier of a job
     * @return The corresponding job
     */
    Job getCompletedJob(String identifier);
}
