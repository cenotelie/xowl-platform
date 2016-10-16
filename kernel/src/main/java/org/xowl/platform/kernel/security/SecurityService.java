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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.Service;

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
    Realm getRealm();

    /**
     * On a new request, performs the authentication of a user
     *
     * @param client The requesting client
     * @param userId The identifier of a user
     * @param key    The key used to identified the user (e.g. a password)
     * @return Whether the operation succeed
     */
    XSPReply authenticate(String client, String userId, char[] key);

    /**
     * Event when the request terminated
     *
     * @param client The requesting client
     */
    void onRequestEnd(String client);

    /**
     * Checks whether the current user has a role
     *
     * @param roleId The identifier of a role
     * @return Whether the user has the role
     */
    boolean checkCurrentHasRole(String roleId);
}
