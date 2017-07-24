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

import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.api.ApiError;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.Collection;

/**
 * Implements a directory service for the domain connectors
 *
 * @author Laurent Wouters
 */
public interface ConnectionService extends SecuredService {
    /**
     * Service action to spawn a new connector
     */
    SecuredAction ACTION_SPAWN = new SecuredAction(ConnectionService.class.getCanonicalName() + ".Spawn", "Connection Service - Spawn Connector");
    /**
     * Service action to delete a connector
     */
    SecuredAction ACTION_DELETE = new SecuredAction(ConnectionService.class.getCanonicalName() + ".Delete", "Connection Service - Delete Connector");

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_SPAWN,
            ACTION_DELETE
    };

    /**
     * API error - A connector with the same identifier already exists
     */
    ApiError ERROR_CONNECTOR_SAME_ID = new ApiError(0x00000131,
            "A connector with the same identifier already exists.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000131.html");
    /**
     * API error - Could not find a factory for the specified connector descriptor
     */
    ApiError ERROR_NO_FACTORY = new ApiError(0x00000132,
            "Could not find a factory for the specified connector descriptor.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000132.html");
    /**
     * API error - The connector's queue is empty
     */
    ApiError ERROR_EMPTY_QUEUE = new ApiError(0x00000133,
            "The connector's queue is empty.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000133.html");

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
    ConnectorService getConnector(String identifier);

    /**
     * Gets the descriptions of the supported connectors
     *
     * @return The descriptions of the supported connectors
     */
    Collection<ConnectorDescriptor> getDescriptors();

    /**
     * Spawns a new connector for a domain
     *
     * @param description   The domain's description
     * @param specification The specification for the new connector
     * @return The operation's result
     */
    Reply spawn(ConnectorDescriptor description, ConnectorServiceData specification);

    /**
     * Deletes a spawned connector
     * Only previously spawned parametric connectors can be deleted
     *
     * @param identifier The identifier of a connector
     * @return The operation's result
     */
    Reply delete(String identifier);
}
