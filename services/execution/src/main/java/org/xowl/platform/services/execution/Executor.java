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
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;

/**
 * Represents an executor for executable pieces of content
 *
 * @author Laurent Wouters
 */
public interface Executor extends Identifiable, Serializable {
    /**
     * Gets the executables that can be executed by this executor
     *
     * @return The protocol reply
     */
    XSPReply getExecutables();

    /**
     * Launches the execution of an executable with this executor
     *
     * @param executable The executable to execute
     * @return The protocol reply
     */
    XSPReply execute(Executable executable);
}