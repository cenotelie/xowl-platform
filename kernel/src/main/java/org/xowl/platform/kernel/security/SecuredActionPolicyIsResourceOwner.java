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

import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents an authorization policy that requires the user to be a owner of the secured resource
 *
 * @author Laurent Wouters
 */
public class SecuredActionPolicyIsResourceOwner extends SecuredActionPolicyBase {
    /**
     * The descriptor for this policy
     */
    public static final SecuredActionPolicyDescriptor DESCRIPTOR = new SecuredActionPolicyDescriptor(SecuredActionPolicyIsResourceOwner.class.getCanonicalName(), "User is resource owner");

    /**
     * The singleton instance for this policy
     */
    public static final SecuredActionPolicy INSTANCE = new SecuredActionPolicyIsResourceOwner();

    /**
     * Initializes this policy
     */
    private SecuredActionPolicyIsResourceOwner() {
        super(DESCRIPTOR.getIdentifier(), DESCRIPTOR.getName());
    }

    @Override
    public SecuredActionPolicyDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, SecuredAction action) {
        return false;
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, SecuredAction action, Object data) {
        if (data == null)
            return false;
        if (data instanceof SecuredResource) {
            // the secured resource itself
            return securityService.getSecuredResources().checkIsResourceOwner(securityService, user, ((SecuredResource) data).getIdentifier()).isSuccess();
        } else if (data instanceof SecuredResourceDescriptor) {
            // the security descriptor for the resource
            return securityService.getSecuredResources().checkIsResourceOwner(securityService, user, ((SecuredResourceDescriptor) data).getIdentifier()).isSuccess();
        } else if (data instanceof String) {
            // the identifier of the resource
            return securityService.getSecuredResources().checkIsResourceOwner(securityService, user, (String) data).isSuccess();
        }
        return false;
    }
}
