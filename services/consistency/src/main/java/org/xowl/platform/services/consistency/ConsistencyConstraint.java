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

package org.xowl.platform.services.consistency;

import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;

/**
 * Represents a consistency constraint
 *
 * @author Laurent Wouters
 */
public interface ConsistencyConstraint extends Identifiable, Serializable {
    /**
     * Gets the source definition of this constraint
     *
     * @return The source definition of this constraint
     */
    String getDefinition();

    /**
     * Gets whether this constraint is active
     *
     * @return Whether the constraint is active
     */
    boolean isActive();
}
