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

package org.xowl.platform.connectors.semanticweb;

import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.services.connection.ConnectorDescriptor;
import org.xowl.platform.services.connection.ConnectorService;
import org.xowl.platform.services.connection.ConnectorServiceData;
import org.xowl.platform.services.connection.ConnectorServiceFactory;

/**
 * The factory for Semantic Web connectors
 *
 * @author Laurent Wouters
 */
public class SemanticWebConnectorFactory implements ConnectorServiceFactory {
    @Override
    public String getIdentifier() {
        return SemanticWebConnectorFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Semantic Web Connector Factory";
    }

    @Override
    public ConnectorService newConnector(ConnectorDescriptor descriptor, ConnectorServiceData specification) {
        if (descriptor == SemanticWebConnectorDescriptor.INSTANCE)
            return new SemanticWebConnector(specification);
        return null;
    }
}
