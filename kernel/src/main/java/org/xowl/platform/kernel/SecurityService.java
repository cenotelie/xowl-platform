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

package org.xowl.platform.kernel;

import org.apache.shiro.subject.Subject;

/**
 * Manages the security on the platform
 *
 * @author Laurent Wouters
 */
public interface SecurityService extends Service {
    /**
     * The platform administration role
     */
    String ROLE_ADMIN = "org.xowl.platform.kernel.security.roles.Admin";

    /**
     * Gets the realm of this platform
     *
     * @return The realm of this platform
     */
    String getRealm();

    /**
     * Performs the login of a user on this thread
     *
     * @param host     The requesting host
     * @param username The username
     * @param password The password
     * @return Whether the operation succeed
     */
    boolean login(String host, String username, char[] password);

    /**
     * Logout the current user
     */
    void logout();

    /**
     * Gets the current subject
     *
     * @return The current subject, if any
     */
    Subject getSubject();
}
