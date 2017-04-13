/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.services.execution;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.platform.PlatformLogMessage;
import org.xowl.platform.kernel.platform.PlatformUser;

import java.util.List;

/**
 * Represents the execution of a piece of content
 *
 * @author Laurent Wouters
 */
public interface Execution {
    /**
     * Gets the executed content
     *
     * @return The executed content
     */
    Executable getExecutable();

    /**
     * Gets the description of the used executor
     *
     * @return The used executor
     */
    Executor getExecutor();

    /**
     * Gets the owner of this execution
     *
     * @return The owner of this execution
     */
    PlatformUser getOwner();

    /**
     * Gets whether this execution is currently running
     *
     * @return Whether this execution is currently running
     */
    boolean isRunning();

    /**
     * Tries to abort the running execution
     *
     * @return The protocol reply
     */
    XSPReply abort();

    /**
     * Gets the log for this execution
     *
     * @return The log for this execution
     */
    List<PlatformLogMessage> getLog();
}
