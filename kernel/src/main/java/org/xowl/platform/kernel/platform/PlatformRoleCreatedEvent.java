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
 * Event when a new role is created
 *
 * @author Laurent Wouters
 */
public class PlatformRoleCreatedEvent extends EventBase {
    /**
     * The created role
     */
    private final PlatformRole role;

    /**
     * Gets the created role
     *
     * @return The created role
     */
    public PlatformRole getCreatedRole() {
        return role;
    }

    /**
     * Initializes this event
     *
     * @param role       The created role
     * @param originator The originator for this event
     */
    public PlatformRoleCreatedEvent(PlatformRole role, Identifiable originator) {
        super(new RichString("Created role ", role), PlatformRoleCreatedEvent.class.getCanonicalName(), originator);
        this.role = role;
    }
}