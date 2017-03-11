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

import org.xowl.infra.utils.RichString;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a badge has been awarded to a user
 *
 * @author Laurent Wouters
 */
public class BadgeAwardedEvent extends EventBase {
    /**
     * The profile that received the award
     */
    private final PublicProfile profile;
    /**
     * The awarded badge
     */
    private final Badge badge;

    /**
     * Gets the profile that received the award
     *
     * @return The profile that received the award
     */
    public PublicProfile getProfile() {
        return profile;
    }

    /**
     * Gets the awarded badge
     *
     * @return The awarded badge
     */
    public Badge getBadge() {
        return badge;
    }

    /**
     * Initializes this event
     *
     * @param profile The profile that received the award
     * @param badge   The awarded badge
     */
    public BadgeAwardedEvent(PublicProfile profile, Badge badge) {
        super(
                new RichString("Badge ", badge, " has been awarded to ", profile),
                BadgeAwardedEvent.class.getCanonicalName(),
                Register.getComponent(ProfileService.class));
        this.profile = profile;
        this.badge = badge;
    }
}
