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

import org.xowl.platform.kernel.Identifiable;
import org.xowl.store.Serializable;

/**
 * Represents an action that can be triggered in a workflow within the platform
 *
 * @author Laurent Wouters
 */
public interface WorkflowAction extends Identifiable, Serializable {
    /**
     * Executes/triggers this action
     *
     * @param parameter The parameter, if any
     * @return The result
     */
    WorkflowActionReply execute(Object parameter);
}
