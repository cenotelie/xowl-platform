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

package org.xowl.platform.kernel.platform;

import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.RichString;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a new user is created
 *
 * @author Laurent Wouters
 */
public class PlatformUserCreatedEvent extends EventBase {
    /**
     * The created user
     */
    private final PlatformUser user;

    /**
     * Gets the created user
     *
     * @return The created user
     */
    public PlatformUser getCreatedUser() {
        return user;
    }

    /**
     * Initializes this event
     *
     * @param user       The created user
     * @param originator The originator for this event
     */
    public PlatformUserCreatedEvent(PlatformUser user, Identifiable originator) {
        super(new RichString("Created user ", user), PlatformUserCreatedEvent.class.getCanonicalName(), originator);
        this.user = user;
    }
}