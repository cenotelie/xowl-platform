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

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.Service;

import java.util.Collection;

/**
 * Represents a service that can create workflow elements
 *
 * @author Laurent Wouters
 */
public interface WorkflowFactoryService extends Service {
    /**
     * Gets the types of workflow actions supported by this service
     *
     * @return The types of workflow actions
     */
    Collection<String> getActionTypes();

    /**
     * Creates a new action object
     *
     * @param type           The action type
     * @param jsonDefinition The definition of the action
     * @return The new action
     */
    WorkflowAction create(String type, ASTNode jsonDefinition);
}
