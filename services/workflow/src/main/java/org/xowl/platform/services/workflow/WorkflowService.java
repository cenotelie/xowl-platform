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

package org.xowl.platform.services.workflow;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.Service;

/**
 * Represents a service that drives a workflow on the platform
 *
 * @author Laurent Wouters
 */
public interface WorkflowService extends Service {
    /**
     * Gets the current workflow
     *
     * @return The current workflow
     */
    Workflow getCurrentWorkflow();

    /**
     * Gets the active phase, if any
     *
     * @return The active phase
     */
    WorkflowPhase getActivePhase();

    /**
     * Gets the active activity, if any
     *
     * @return The active activity
     */
    WorkflowActivity getActiveActivity();

    /**
     * Executes the action identified by the specified action identifier
     *
     * @param action    The identifier of the action to execute
     * @param parameter A parameter for the action, if any
     * @return The action's result
     */
    XSPReply execute(WorkflowAction action, Object parameter);
}
