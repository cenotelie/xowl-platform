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
import org.xowl.infra.store.Serializable;
import org.xowl.platform.kernel.Identifiable;

/**
 * Represents a job to be executed on the platform
 *
 * @author Laurent Wouters
 */
public interface Job extends Identifiable, Serializable, Runnable {
    /**
     * Gets the job's current status
     *
     * @return The job's current status
     */
    JobStatus getStatus();

    /**
     * Gets the result for this job, or null if it not yet complete
     *
     * @return The result for this job
     */
    XSPReply getResult();

    /**
     * Event when the job is being scheduled
     */
    void onScheduled();

    /**
     * Event when the job is going to be run
     */
    void onRun();

    /**
     * Callback when the job has been completed
     */
    void onCompleted();
}
