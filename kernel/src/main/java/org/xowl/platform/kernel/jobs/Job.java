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

import fr.cenotelie.commons.utils.Identifiable;
import fr.cenotelie.commons.utils.Serializable;
import fr.cenotelie.commons.utils.api.Reply;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents a job to be executed on the platform
 *
 * @author Laurent Wouters
 */
public interface Job extends Identifiable, Serializable, Runnable {
    /**
     * Gets the owner of this job
     *
     * @return The owner of this job
     */
    PlatformUser getOwner();

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
    Reply getResult();

    /**
     * Gets an estimation of the current completion rate
     *
     * @return An estimation of the current completion rate (between 0.0 and 1.0)
     */
    float getCompletionRate();

    /**
     * Gets whether this job can be cancelled
     *
     * @return Whether this job can be cancelled
     */
    boolean canCancel();

    /**
     * Tries to cancel this job
     *
     * @return The result of the attemps to cancel the job
     */
    Reply cancel();

    /**
     * Event when the job is being scheduled
     */
    void onScheduled();

    /**
     * Event when the job is going to be run
     */
    void onRun();

    /**
     * Callback when the job has been terminated
     *
     * @param cancelled Whether the job was cancelled (instead of normal termination)
     */
    void onTerminated(boolean cancelled);
}
