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

import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents an authorization policy that requires the user to have the role of platform administrator
 *
 * @author Laurent Wouters
 */
public class SecuredActionPolicyRoleAdmin extends SecuredActionPolicyBase {
    /**
     * The singleton instance for this policy
     */
    public static final SecuredActionPolicy INSTANCE = new SecuredActionPolicyRoleAdmin();

    /**
     * Initializes this policy
     */
    private SecuredActionPolicyRoleAdmin() {
        super(SecuredActionPolicyRoleAdmin.class.getCanonicalName(), "Admin policy");
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, SecuredAction action) {
        return securityService.getRealm().checkHasRole(user.getIdentifier(), PlatformRoleAdmin.INSTANCE.getIdentifier());
    }
}
