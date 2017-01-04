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

package org.xowl.platform.connectors.csv;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.platform.services.connection.ConnectorDescription;
import org.xowl.platform.services.connection.ConnectorDescriptionParam;
import org.xowl.platform.services.connection.ConnectorServiceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Factory of CSV connectors
 *
 * @author Laurent Wouters
 */
public class CSVFactory implements ConnectorServiceFactory {
    /**
     * The descriptions for this factory
     */
    private final Collection<ConnectorDescription> descriptions;

    /**
     * The singleton instance of the factory
     */
    public static final CSVFactory INSTANCE = new CSVFactory();

    /**
     * Initializes the factory
     */
    private CSVFactory() {
        descriptions = new ArrayList<>(1);
        descriptions.add(CSVDescription.INSTANCE);
    }

    @Override
    public String getIdentifier() {
        return CSVFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - CSV Connector Factory";
    }

    @Override
    public Collection<ConnectorDescription> getDescriptors() {
        return descriptions;
    }

    @Override
    public XSPReply newConnector(ConnectorDescription description, String identifier, String name, String[] uris, Map<ConnectorDescriptionParam, Object> parameters) {
        return new XSPReplyResult<>(new CSVConnector(identifier, name, uris));
    }
}
