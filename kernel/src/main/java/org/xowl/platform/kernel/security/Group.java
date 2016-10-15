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

package org.xowl.platform.kernel.security;

import org.xowl.infra.store.Serializable;
import org.xowl.platform.kernel.Identifiable;

import java.util.Collection;

/**
 * Represents a group of users on this platform
 *
 * @author Laurent Wouters
 */
public interface Group extends Identifiable, Serializable {
    /**
     * Gets the users in this group
     *
     * @return The users in this group
     */
    Collection<User> getUsers();

    /**
     * Gets the admins for this group
     *
     * @return The admins for this group
     */
    Collection<User> getAdmins();

    /**
     * Gets the user roles associated to this group
     *
     * @return The user roles associated to this group
     */
    Collection<Role> getRoles();
}
