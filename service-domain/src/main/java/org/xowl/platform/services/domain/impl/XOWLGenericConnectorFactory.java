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

package org.xowl.platform.services.domain.impl;

import org.xowl.platform.services.domain.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Implements a factory of generic connectors
 *
 * @author Laurent Wouters
 */
public class XOWLGenericConnectorFactory implements DomainConnectorFactory {
    /**
     * The description of the generic domain
     */
    private static final DomainDescription DESCRIPTION = new DomainDescriptionBase(
            DomainDescription.class.getCanonicalName() + ".GenericDomain",
            "Generic Domain",
            "This is a generic domain that accepts as input any form of semantic data (triples, quads, ontologies)."
    );

    /**
     * The descriptions of the supported domains
     */
    private static final Collection<DomainDescription> DESCRIPTIONS = Collections.unmodifiableCollection(Arrays.asList(DESCRIPTION));

    @Override
    public String getIdentifier() {
        return XOWLGenericConnectorFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Generic Connector Factory";
    }

    @Override
    public String getProperty(String name) {
        if (name == null)
            return null;
        if ("identifier".equals(name))
            return getIdentifier();
        if ("name".equals(name))
            return getName();
        return null;
    }

    @Override
    public Collection<DomainDescription> getDomains() {
        return DESCRIPTIONS;
    }

    @Override
    public DomainConnectorService newConnector(DomainDescription description, String identifier, String name, String[] uris, Map<DomainDescriptionParam, Object> parameters) {
        return new ParametricDomainConnector(identifier, name, uris);
    }
}
