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

package org.xowl.platform.services.consistency.impl;

import org.xowl.platform.services.consistency.ConsistencyRule;
import org.xowl.store.IOUtils;

/**
 * Implements a consistency rule on the xOWL platform
 *
 * @author Laurent Wouters
 */
class XOWLConsistencyRule implements ConsistencyRule {
    /**
     * The rule's unique IRI
     */
    private final String iri;
    /**
     * The rule's name
     */
    private final String name;
    /**
     * The rule's definition
     */
    private final String definition;
    /**
     * Whether the rule is active
     */
    private final boolean isActive;

    /**
     * Initializes this rule
     *
     * @param iri        The rule's unique IRI
     * @param name       The rule's name
     * @param isActive   Whether the rule is active
     * @param definition The rule's definition
     */
    public XOWLConsistencyRule(String iri, String name, boolean isActive, String definition) {
        this.iri = iri;
        this.name = name;
        this.definition = definition;
        this.isActive = isActive;
    }

    @Override
    public String getIdentifier() {
        return iri;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public String getDefinition() {
        return definition;
    }

    @Override
    public String serializedString() {
        return iri;
    }

    @Override
    public String serializedJSON() {
        return "{\"id\": \"" +
                IOUtils.escapeStringJSON(iri) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(name) +
                "\", \"definition\": \"" +
                IOUtils.escapeStringJSON(definition) +
                "\", \"isActive\": " +
                Boolean.toString(isActive) +
                "}";
    }
}
