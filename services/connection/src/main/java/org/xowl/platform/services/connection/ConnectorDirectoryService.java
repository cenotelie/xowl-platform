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

package org.xowl.platform.services.connection;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.util.Collection;
import java.util.Map;

/**
 * Implements a directory service for the domain connectors
 *
 * @author Laurent Wouters
 */
public interface ConnectorDirectoryService extends HttpApiService {
    /**
     * Gets the available connectors
     *
     * @return The available connectors
     */
    Collection<ConnectorService> getConnectors();

    /**
     * Gets the connector for the specified identifier
     *
     * @param identifier The identifier of a connector
     * @return The connector, or null if it does not exist
     */
    ConnectorService get(String identifier);

    /**
     * Gets the descriptions of the supported connectors
     *
     * @return The descriptions of the supported connectors
     */
    Collection<ConnectorDescription> getDescriptors();

    /**
     * Spawns a new connector for a domain
     *
     * @param description The domain's description
     * @param identifier  The new connector's unique identifier
     * @param name        The new connector's name
     * @param uris        The new connector's API uris, if any
     * @param parameters  The parameters for the new connector, if any
     * @return The operation's result
     */
    XSPReply spawn(ConnectorDescription description, String identifier, String name, String[] uris, Map<ConnectorDescriptionParam, Object> parameters);

    /**
     * Deletes a spawned connector
     * Only previously spawned parametric connectors can be deleted
     *
     * @param identifier The identifier of a connector
     * @return The operation's result
     */
    XSPReply delete(String identifier);
}
