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

package org.xowl.platform.services.community.profiles;

import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.*;

/**
 * Represents an authorization policy that requires the user to be the owner of the associated profile
 *
 * @author Laurent Wouters
 */
public class SecuredActionPolicyIsProfileOwner extends SecuredActionPolicyBase {
    /**
     * The descriptor for this policy
     */
    public static final SecuredActionPolicyDescriptor DESCRIPTOR = new SecuredActionPolicyDescriptor(SecuredActionPolicyIsProfileOwner.class.getCanonicalName(), "User is profile owner");

    /**
     * The singleton instance for this policy
     */
    public static final SecuredActionPolicy INSTANCE = new SecuredActionPolicyIsProfileOwner();

    /**
     * Initializes this policy
     */
    private SecuredActionPolicyIsProfileOwner() {
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
        return data instanceof PublicProfile && ((PublicProfile) data).getIdentifier().equals(user.getIdentifier());
    }
}
