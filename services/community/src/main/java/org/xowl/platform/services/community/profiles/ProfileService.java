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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

/**
 * Service for the management of user profiles for the platform
 *
 * @author Laurent Wouters
 */
public interface ProfileService extends SecuredService {
    /**
     * Service action to get the description of bots
     */
    SecuredAction ACTION_UPDATE_PROFILE = new SecuredAction(ProfileService.class.getCanonicalName() + ".UpdateProfile", "Profile Service - Update Profile", SecuredActionPolicyIsProfileOwner.DESCRIPTOR);
    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_UPDATE_PROFILE
    };

    /**
     * Gets the public profile for user corresponding to the specified identifier
     *
     * @param identifier The identifier of a user
     * @return The associated profile, or null if there is none
     */
    PublicProfile getPublicProfile(String identifier);

    /**
     * Updates the public profile
     *
     * @param profile The profile to update
     * @return The protocol reply
     */
    XSPReply updatePublicProfile(PublicProfile profile);
}
