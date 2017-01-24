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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.platform.services.connection.ConnectorDescriptor;
import org.xowl.platform.services.connection.ConnectorDescriptorParam;
import org.xowl.platform.services.connection.ConnectorServiceFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * The factory for Semantic Web connectors
 *
 * @author Laurent Wouters
 */
public class SemanticWebConnectorFactory implements ConnectorServiceFactory {
    /**
     * The descriptions of the supported domains
     */
    private static final Collection<ConnectorDescriptor> DESCRIPTIONS = Collections.unmodifiableCollection(Collections.singletonList((ConnectorDescriptor) SemanticWebConnectorDescriptor.INSTANCE));

    @Override
    public String getIdentifier() {
        return SemanticWebConnectorFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - Semantic Web Connector Factory";
    }

    @Override
    public Collection<ConnectorDescriptor> getDescriptors() {
        return DESCRIPTIONS;
    }

    @Override
    public XSPReply newConnector(ConnectorDescriptor descriptor, String identifier, String name, String[] uris, Map<ConnectorDescriptorParam, Object> parameters) {
        return new XSPReplyResult<>(new SemanticWebConnector(identifier, name, uris));
    }
}
