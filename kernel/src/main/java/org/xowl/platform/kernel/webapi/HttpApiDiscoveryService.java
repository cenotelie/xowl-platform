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

package org.xowl.platform.kernel.webapi;

import org.xowl.platform.kernel.Service;

import java.util.Collection;

/**
 * Represents a service for the discovery of other HTTP API services
 *
 * @author Laurent Wouters
 */
public interface HttpApiDiscoveryService extends Service {
    /**
     * Gets the known API services
     *
     * @return The known API services
     */
    Collection<HttpApiService> getServices();

    /**
     * Gets the known documentation resources
     *
     * @return The known documentation resources
     */
    Collection<HttpApiResource> getResources();
}
