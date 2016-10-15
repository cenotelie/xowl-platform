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
 * Represents a security realm for users, groups and roles on this platform
 *
 * @author Laurent Wouters
 */
public interface Realm extends Service {
    /**
     * The identifier of the property for the realm identifier
     */
    String PROPERTY_ID = "realmId";

    /**
     * On a new request, performs the authentication of a user
     *
     * @param userId The identifier of a user
     * @param key    The key used to identified the user (e.g. a password)
     * @return Whether the operation succeed
     */
    XSPReply authenticate(String userId, char[] key);

    /**
     * Event when the request terminated
     *
     * @param userId The identifier of a user
     */
    void onRequestEnd(String userId);

    /**
     * Checks whether a specified user has a role
     *
     * @param userId The identifier of a user
     * @param roleId The identifier of a role
     * @return Whether the user has the role
     */
    boolean checkHasRole(String userId, String roleId);
}
