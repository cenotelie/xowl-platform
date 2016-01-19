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
import org.xowl.store.rdf.LiteralNode;
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
        this.message = interpolate(message, antecedents);
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

    /**
     * Builds the interpolated message string
     *
     * @param origin      The original message
     * @param antecedents The mapped values
     * @return The interpolated string
     */
    private static String interpolate(String origin, Map<String, Node> antecedents) {
        if (!origin.contains("?"))
            return origin;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i != origin.length(); i++) {
            if (origin.charAt(i) == '?') {
                int j = i + 1;
                while (j < origin.length() && Character.isLetterOrDigit(origin.charAt(j)))
                    j++;
                if (j == i + 1)
                    continue;
                String name = origin.substring(i + 1, j);
                Node value = antecedents.get(name);
                if (value == null)
                    continue;
                if (value.getNodeType() == Node.TYPE_IRI || value.getNodeType() == Node.TYPE_BLANK) {
                    builder.append(value.toString());
                    i = j - 1;
                } else if (value.getNodeType() == Node.TYPE_LITERAL) {
                    builder.append(((LiteralNode) value).getLexicalValue());
                    i = j - 1;
                }
            } else {
                builder.append(origin.charAt(i));
            }
        }
        return builder.toString();
    }
}
