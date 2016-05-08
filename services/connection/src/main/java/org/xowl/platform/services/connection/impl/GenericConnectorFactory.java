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

package org.xowl.platform.services.connection.impl;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.platform.services.connection.ConnectorDescription;
import org.xowl.platform.services.connection.ConnectorDescriptionBase;
import org.xowl.platform.services.connection.ConnectorDescriptionParam;
import org.xowl.platform.services.connection.ConnectorServiceFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Implements a factory of generic connectors
 *
 * @author Laurent Wouters
 */
public class GenericConnectorFactory implements ConnectorServiceFactory {
    /**
     * The description of the generic connector
     */
    private static final ConnectorDescription DESCRIPTION = new ConnectorDescriptionBase(
            "org.xowl.platform.services.connection.GenericDomain",
            "Generic Domain",
            "This is a generic domain that accepts as input any form of semantic data (triples, quads, ontologies)."
    );

    /**
     * The descriptions of the supported domains
     */
    private static final Collection<ConnectorDescription> DESCRIPTIONS = Collections.unmodifiableCollection(Arrays.asList(DESCRIPTION));

    @Override
    public String getIdentifier() {
        return GenericConnectorFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Generic Connector Factory";
    }

    @Override
    public Collection<ConnectorDescription> getDescriptors() {
        return DESCRIPTIONS;
    }

    @Override
    public XSPReply newConnector(ConnectorDescription descriptor, String identifier, String name, String[] uris, Map<ConnectorDescriptionParam, Object> parameters) {
        return new XSPReplyResult<>(new GenericConnector(identifier, name, uris));
    }
}
