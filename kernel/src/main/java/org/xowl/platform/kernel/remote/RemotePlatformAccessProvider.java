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

package org.xowl.platform.kernel.remote;

import org.xowl.platform.kernel.Registrable;

/**
 * Providers API accesses per user to a specific remote platform
 *
 * @author Laurent Wouters
 */
public interface RemotePlatformAccessProvider extends Registrable {
    /**
     * Gets the endpoint of the remote platform
     *
     * @return The endpoint of the remote platform
     */
    String getEndpoint();

    /**
     * Resolves a connection for a user
     *
     * @param userId The identifier of the user
     * @return The connection
     */
    RemotePlatformAccess getAccess(String userId);
}
