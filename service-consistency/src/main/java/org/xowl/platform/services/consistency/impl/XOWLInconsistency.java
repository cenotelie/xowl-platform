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
import org.xowl.platform.services.consistency.Inconsistency;
import org.xowl.store.IOUtils;
import org.xowl.store.rdf.Node;

import java.util.Map;

/**
 * Implements an inconsistency on the xOWL platform
 *
 * @author Laurent Wouters
 */
class XOWLInconsistency implements Inconsistency {
    /**
     * The IRI for the entity that represents the inconsistency
     */
    private final String iri;
    /**
     * The message for this inconsistency
     */
    private final String message;
    /**
     * The consistency rule that produces this inconsistency
     */
    private final ConsistencyRule rule;
    /**
     * The antecedents that matched the rule
     */
    private final Map<String, Node> antecedents;

    /**
     * Initializes this inconsistency
     *
     * @param iri         The IRI for the entity that represents the inconsistency
     * @param message     The message for this inconsistency
     * @param rule        The consistency rule that produces this inconsistency
     * @param antecedents The antecedents that matched the rule
     */
    public XOWLInconsistency(String iri, String message, ConsistencyRule rule, Map<String, Node> antecedents) {
        this.iri = iri;
        this.message = message;
        this.rule = rule;
        this.antecedents = antecedents;
    }

    @Override
    public String getIdentifier() {
        return iri;
    }

    @Override
    public String getName() {
        return iri;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ConsistencyRule getRule() {
        return rule;
    }

    @Override
    public Map<String, Node> getAntecedents() {
        return antecedents;
    }

    @Override
    public String serializedString() {
        return iri;
    }

    @Override
    public String serializedJSON() {
        return "{\"id\": \"" +
                IOUtils.escapeStringJSON(iri) +
                "\", \"message\": \"" +
                IOUtils.escapeStringJSON(message) +
                "\", \"rule\": \"" +
                IOUtils.escapeStringJSON(rule.getIdentifier()) +
                "}";
    }
}
