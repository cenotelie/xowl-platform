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

package org.xowl.platform.services.connection.events;

import org.xowl.platform.kernel.events.EventBase;
import org.xowl.platform.services.connection.ConnectorDirectoryService;
import org.xowl.platform.services.connection.ConnectorService;

/**
 * Represents an event when a connector is spawned
 *
 * @author Laurent Wouters
 */
public class ConnectorSpawnedEvent extends EventBase {
    /**
     * The type of event
     */
    public static final String TYPE = ConnectorSpawnedEvent.class.getCanonicalName();

    /**
     * The spawned connector service
     */
    private final ConnectorService connectorService;

    /**
     * Initializes this event
     *
     * @param directoryService The originator service
     * @param connector        The spawned connector
     */
    public ConnectorSpawnedEvent(ConnectorDirectoryService directoryService, ConnectorService connector) {
        super("Spawned connector " + connector.getIdentifier(), TYPE, directoryService);
        this.connectorService = connector;
    }

    /**
     * Gets the spawned connector service
     *
     * @return The spawned connector service
     */
    public ConnectorService getConnectorService() {
        return connectorService;
    }
}
