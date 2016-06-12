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

import org.xowl.platform.kernel.HttpAPIService;

import java.util.Collection;

/**
 * Represents a service for describing the platform
 *
 * @author Laurent Wouters
 */
public interface PlatformDescriptorService extends HttpAPIService {
    /**
     * Gets the description of the bundles on this platform
     *
     * @return The bundle on this platform
     */
    Collection<OSGiBundle> getPlatformBundles();
}
