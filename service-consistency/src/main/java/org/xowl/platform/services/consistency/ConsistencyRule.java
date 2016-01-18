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

package org.xowl.platform.services.consistency;

import org.xowl.platform.kernel.Identifiable;
import org.xowl.store.Serializable;

/**
 * Represents a consistency rule
 *
 * @author Laurent Wouters
 */
public interface ConsistencyRule extends Identifiable, Serializable {
    /**
     * Gets whether this rule is active
     *
     * @return Whether the rule is active
     */
    boolean isActive();

    /**
     * Gets the rule's definition
     *
     * @return The rule's definition
     */
    String getDefinition();
}
