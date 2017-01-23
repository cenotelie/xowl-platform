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

import org.xowl.platform.kernel.platform.PlatformGroup;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents an authorization policy that requires the user to be the administrator of the relevant group
 *
 * @author Laurent Wouters
 */
public class SecuredActionPolicyIsGroupAdmin extends SecuredActionPolicyBase {
    /**
     * The singleton instance for this policy
     */
    public static final SecuredActionPolicy INSTANCE = new SecuredActionPolicyIsGroupAdmin();

    /**
     * Initializes this policy
     */
    private SecuredActionPolicyIsGroupAdmin() {
        super(SecuredActionPolicyIsGroupAdmin.class.getCanonicalName(), "Is Group Admin");
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, SecuredAction action) {
        return false;
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, SecuredAction action, Object data) {
        return data instanceof PlatformGroup && ((PlatformGroup) data).getAdmins().contains(user);
    }
}
