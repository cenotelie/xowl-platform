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

import org.xowl.infra.utils.RichString;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a role is deleted
 *
 * @author Laurent Wouters
 */
public class PlatformRoleDeletedEvent extends EventBase {
    /**
     * The type for this event
     */
    public static final String TYPE = PlatformRoleDeletedEvent.class.getCanonicalName();

    /**
     * The deleted role
     */
    private final PlatformRole role;

    /**
     * Gets the deleted role
     *
     * @return The deleted role
     */
    public PlatformRole getDeletedRole() {
        return role;
    }

    /**
     * Initializes this event
     *
     * @param role    The deleted role
     * @param emitter The service that emitted this event
     */
    public PlatformRoleDeletedEvent(PlatformRole role, Service emitter) {
        super(TYPE, emitter, new RichString("Deleted role ", role));
        this.role = role;
    }
}
