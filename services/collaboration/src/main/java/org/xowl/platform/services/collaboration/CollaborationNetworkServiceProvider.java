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

package org.xowl.platform.services.collaboration;

import org.xowl.platform.kernel.Service;

/**
 * A provider of implementations of the collaboration network service
 *
 * @author Laurent Wouters
 */
public interface CollaborationNetworkServiceProvider extends Service {
    /**
     * Tries to newPolicy a collaboration network service
     *
     * @param identifier The identifier of the service
     * @return The instance, or null if it cannot be created
     */
    CollaborationNetworkService instantiate(String identifier);
}
