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
 * Event when a public profile has been update
 *
 * @author Laurent Wouters
 */
public class PublicProfileUpdatedEvent extends EventBase {
    /**
     * The updated profile
     */
    private final PublicProfile profile;

    /**
     * Gets the updated profile
     *
     * @return The updated profile
     */
    public PublicProfile getProfile() {
        return profile;
    }

    /**
     * Initializes this event
     *
     * @param profile The updated profile
     */
    public PublicProfileUpdatedEvent(PublicProfile profile) {
        super(
                new RichString("Profile ", profile, " has been updated"),
                PublicProfileUpdatedEvent.class.getCanonicalName(),
                Register.getComponent(ProfileService.class));
        this.profile = profile;
    }
}
