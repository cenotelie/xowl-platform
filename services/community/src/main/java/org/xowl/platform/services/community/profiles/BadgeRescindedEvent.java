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
 * Event when a badge has been rescinded from a user
 *
 * @author Laurent Wouters
 */
public class BadgeRescindedEvent extends EventBase {
    /**
     * The type for this event
     */
    public static final String TYPE = BadgeRescindedEvent.class.getCanonicalName();

    /**
     * The profile that previously received the award
     */
    private final PublicProfile profile;
    /**
     * The rescinded badge
     */
    private final Badge badge;

    /**
     * Gets the profile that previously received the award
     *
     * @return The profile that previously received the award
     */
    public PublicProfile getProfile() {
        return profile;
    }

    /**
     * Gets the rescinded badge
     *
     * @return The rescinded badge
     */
    public Badge getBadge() {
        return badge;
    }

    /**
     * Initializes this event
     *
     * @param profile The profile that previously received the award
     * @param badge   The rescinded badge
     */
    public BadgeRescindedEvent(PublicProfile profile, Badge badge) {
        super(
                TYPE,
                Register.getComponent(ProfileService.class),
                new RichString("Badge ", badge, " has been rescinded from ", profile)
        );
        this.profile = profile;
        this.badge = badge;
    }
}
