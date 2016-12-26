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

import org.xowl.infra.utils.RichString;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a new addon has been installed
 *
 * @author Laurent Wouters
 */
public class AddonInstalledEvent extends EventBase {
    /**
     * The type for this event
     */
    public static final String TYPE = AddonInstalledEvent.class.getCanonicalName();

    /**
     * The addon that has been installed
     */
    private final Addon addon;

    /**
     * Initializes this event
     *
     * @param managementService The platform management service
     * @param addon             The addon that has been installed
     */
    public AddonInstalledEvent(PlatformManagementService managementService, Addon addon) {
        super(new RichString("Installed addon ", addon), TYPE, managementService);
        this.addon = addon;
    }

    /**
     * Gets the addon that has been installed
     *
     * @return The addon that has been installed
     */
    public Addon getAddon() {
        return addon;
    }
}
