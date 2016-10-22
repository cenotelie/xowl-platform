/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.services.security.internal;

import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.RichString;
import org.xowl.platform.kernel.events.EventBase;
import org.xowl.platform.kernel.platform.PlatformGroup;

/**
 * Event when a new group is created
 *
 * @author Laurent Wouters
 */
public class GroupCreatedEvent extends EventBase {
    /**
     * The created group
     */
    private final XOWLInternalGroup group;

    /**
     * Gets the created group
     *
     * @return The created group
     */
    public PlatformGroup getCreatedGroup() {
        return group;
    }

    /**
     * Initializes this event
     *
     * @param group      The created group
     * @param originator The originator for this event
     */
    public GroupCreatedEvent(XOWLInternalGroup group, Identifiable originator) {
        super(new RichString("Created group ", group), GroupCreatedEvent.class.getCanonicalName(), originator);
        this.group = group;
    }
}
