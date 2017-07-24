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

import org.xowl.infra.utils.api.Reply;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.Collection;

/**
 * Service for the management of user profiles for the platform
 *
 * @author Laurent Wouters
 */
public interface ProfileService extends SecuredService, BadgeProvider {
    /**
     * Service action to update the data of a public profile
     */
    SecuredAction ACTION_UPDATE_PROFILE = new SecuredAction(ProfileService.class.getCanonicalName() + ".UpdateProfile", "Profile Service - Update Profile", SecuredActionPolicyIsProfileOwner.DESCRIPTOR);
    /**
     * Service action to award a badge to a user
     */
    SecuredAction ACTION_AWARD_BADGE = new SecuredAction(ProfileService.class.getCanonicalName() + ".AwardBadge", "Profile Service - Award Badge", SecuredAction.DEFAULT_POLICIES);
    /**
     * Service action to rescind a badge from a user
     */
    SecuredAction ACTION_RESCIND_BADGE = new SecuredAction(ProfileService.class.getCanonicalName() + ".RescindBadge", "Profile Service - Rescind Badge", SecuredAction.DEFAULT_POLICIES);
    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_UPDATE_PROFILE,
            ACTION_AWARD_BADGE,
            ACTION_RESCIND_BADGE
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
    Reply updatePublicProfile(PublicProfile profile);

    /**
     * Gets the description of all the badges
     *
     * @return The description of all the badges
     */
    Collection<Badge> getBadges();

    /**
     * Gets the description of a specific badge
     *
     * @param badgeId The identifier of a badge
     * @return The description of the badge
     */
    Badge getBadge(String badgeId);

    /**
     * Awards a badge to a user
     *
     * @param userId  The identifier of the user
     * @param badgeId The identifier of the badge
     * @return The protocol reply
     */
    Reply awardBadge(String userId, String badgeId);

    /**
     * Rescinds a badge from a user
     *
     * @param userId  The identifier of the user
     * @param badgeId The identifier of the badge
     * @return The protocol reply
     */
    Reply rescindBadge(String userId, String badgeId);
}
