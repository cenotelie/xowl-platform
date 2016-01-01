/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.domain;

import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.store.xsp.XSPReply;

import java.util.Collection;
import java.util.Map;

/**
 * Implements a directory service for the domain connectors
 *
 * @author Laurent Wouters
 */
public interface DomainDirectoryService extends HttpAPIService {
    /**
     * Gets the available connectors
     *
     * @return The available connectors
     */
    Collection<DomainConnectorService> getConnectors();

    /**
     * Gets the connector for the specified identifier
     *
     * @param identifier The identifier of a connector
     * @return The connector, or null if it does not exist
     */
    DomainConnectorService get(String identifier);

    /**
     * Gets all the supported domains
     *
     * @return The supported domains
     */
    Collection<DomainDescription> getDomains();

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
    XSPReply spawn(DomainDescription description, String identifier, String name, String[] uris, Map<DomainDescriptionParam, Object> parameters);

    /**
     * Deletes a spawned connector
     * Only previously spawned parametric connectors can be deleted
     *
     * @param identifier The identifier of a connector
     * @return The operation's result
     */
    XSPReply delete(String identifier);
}
