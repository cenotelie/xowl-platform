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

package org.xowl.platform.kernel;

import java.io.File;

/**
 * Implements a platform locator service
 *
 * @author Laurent Wouters
 */
class PlatformLocatorImpl implements PlatformLocator {
    /**
     * The platform's location on the file system
     */
    private final File location;

    /**
     * Initializes this locator
     *
     * @param bundleLocation The location of the current bundle on the file system
     */
    public PlatformLocatorImpl(String bundleLocation) {
        if (bundleLocation.startsWith("file:"))
            bundleLocation = bundleLocation.substring("file:".length());
        File location = new File(bundleLocation);
        this.location = location.getParentFile().getParentFile().getParentFile();
    }

    @Override
    public String getIdentifier() {
        return PlatformLocatorImpl.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Platform Locator";
    }

    @Override
    public File getLocation() {
        return location;
    }
}
