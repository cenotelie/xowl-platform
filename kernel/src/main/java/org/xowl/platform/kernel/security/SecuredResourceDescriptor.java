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

package org.xowl.platform.kernel.security;

import fr.cenotelie.commons.utils.Identifiable;
import fr.cenotelie.commons.utils.Serializable;

import java.util.Collection;

/**
 * Represents the security descriptor of a secured resource
 *
 * @author Laurent Wouters
 */
public interface SecuredResourceDescriptor extends Identifiable, Serializable {
    /**
     * Gets the owners of this resource
     *
     * @return The owners of this resource
     */
    Collection<String> getOwners();

    /**
     * Gets the specifications of how this resource is shared
     *
     * @return The specifications of how this resource is shared
     */
    Collection<SecuredResourceSharing> getSharing();
}
