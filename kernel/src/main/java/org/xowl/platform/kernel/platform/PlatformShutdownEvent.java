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

package org.xowl.platform.kernel.platform;

import org.xowl.platform.kernel.events.Event;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when the platform is shutting down
 *
 * @author Laurent Wouters
 */
public class PlatformShutdownEvent extends EventBase {
    /**
     * The singleton instance
     */
    public static final Event INSTANCE = new PlatformShutdownEvent();

    /**
     * Initializes this event
     */
    private PlatformShutdownEvent() {
        super("Platform is shutting down",
                PlatformStartupEvent.class.getCanonicalName(),
                PlatformDescriptor.INSTANCE);
    }
}
