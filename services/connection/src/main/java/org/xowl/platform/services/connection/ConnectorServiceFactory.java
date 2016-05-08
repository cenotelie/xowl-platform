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
import org.xowl.platform.kernel.Service;

import java.util.Collection;
import java.util.Map;

/**
 * A factory that can create new connectors for the platform
 *
 * @author Laurent Wouters
 */
public interface ConnectorServiceFactory extends Service {
    /**
     * Gets the descriptions of the connectors supported by this factory
     *
     * @return The supported connectors
     */
    Collection<ConnectorDescription> getDescriptors();

    /**
     * Instantiates a new connector for a domain
     *
     * @param descriptor The connector's description
     * @param identifier The new connector's unique identifier
     * @param name       The new connector's name
     * @param uris       The new connector's API uris, if any
     * @param parameters The parameters for the new connector, if any
     * @return The new connector
     */
    XSPReply newConnector(ConnectorDescription descriptor, String identifier, String name, String[] uris, Map<ConnectorDescriptionParam, Object> parameters);
}
