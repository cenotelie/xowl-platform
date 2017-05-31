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
 * Represents an authorization policy that requires the user to have been shared the owned resource
 *
 * @author Laurent Wouters
 */
public class SecuredActionPolicyIsAllowedAccessToResource extends SecuredActionPolicyBase {
    /**
     * The descriptor for this policy
     */
    public static final SecuredActionPolicyDescriptor DESCRIPTOR = new SecuredActionPolicyDescriptor(SecuredActionPolicyIsAllowedAccessToResource.class.getCanonicalName(), "User is allowed access to resource");

    /**
     * The singleton instance for this policy
     */
    public static final SecuredActionPolicy INSTANCE = new SecuredActionPolicyIsAllowedAccessToResource();

    /**
     * Initializes this policy
     */
    private SecuredActionPolicyIsAllowedAccessToResource() {
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
        if (!(data instanceof OwnedResource))
            return false;
        OwnedResource resource = (OwnedResource) data;
        if (resource.getOwner().equals(user.getIdentifier()))
            // resource owner can access the resource
            return true;
        // look for a sharing of the resource matching the requesting user
        for (OwnedResourceSharing sharing : ((OwnedResource) data).getSharings()) {
            if (sharing.isAllowedAccess(securityService, user))
                return true;
        }
        return false;
    }
}
