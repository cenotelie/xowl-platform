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

import org.xowl.platform.kernel.PlatformUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the accesses of users to a remote platform
 *
 * @author Laurent Wouters
 */
public class RemotePlatformAccessManager implements RemotePlatformAccessProvider {
    /**
     * The API endpoint for the remote platform
     */
    private final String endpoint;
    /**
     * The common factory to use
     */
    private final PlatformApiDeserializer deserializer;
    /**
     * The managed connections
     */
    private final Map<String, RemotePlatformAccess> connections;

    /**
     * Initializes this manager
     *
     * @param endpoint     The API endpoint for the remote platform
     * @param deserializer The common factory to use
     */
    public RemotePlatformAccessManager(String endpoint, PlatformApiDeserializer deserializer) {
        this.endpoint = endpoint;
        this.deserializer = deserializer;
        this.connections = new HashMap<>();
    }

    @Override
    public String getIdentifier() {
        return RemotePlatformAccessManager.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Access Manager for " + endpoint;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public synchronized RemotePlatformAccess getAccess(String userId) {
        if (userId == null || userId.isEmpty())
            return null;
        RemotePlatformAccess connection = connections.get(userId);
        if (connection == null) {
            connection = new RemotePlatformAccess(endpoint, deserializer);
            connections.put(userId, connection);
        }
        return connection;
    }
}
