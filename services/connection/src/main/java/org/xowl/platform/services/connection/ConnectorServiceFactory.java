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

import org.xowl.platform.kernel.Registrable;

/**
 * A factory that can create new connectors for the platform
 *
 * @author Laurent Wouters
 */
public interface ConnectorServiceFactory extends Registrable {
    /**
     * Instantiates a new connector for a domain
     *
     * @param descriptor    The connector's description
     * @param specification The specification for the new connector
     * @return The new connector, or null if it cannot be created
     */
    ConnectorService newConnector(ConnectorDescriptor descriptor, ConnectorServiceData specification);
}
