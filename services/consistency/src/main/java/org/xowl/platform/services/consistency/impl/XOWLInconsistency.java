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

package org.xowl.platform.services.consistency.impl;

import org.xowl.infra.store.RDFUtils;
import org.xowl.infra.store.rdf.Node;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.services.consistency.ConsistencyRule;
import org.xowl.platform.services.consistency.Inconsistency;

import java.io.IOException;
import java.io.StringWriter;
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
        StringWriter builder = new StringWriter();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(Inconsistency.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(iri));
        builder.append("\", \"message\": \"");
        builder.append(TextUtils.escapeStringJSON(message));
        builder.append("\", \"ruleId\": \"");
        builder.append(TextUtils.escapeStringJSON(rule.getIdentifier()));
        builder.append("\", \"ruleName\": \"");
        builder.append(TextUtils.escapeStringJSON(rule.getName()));
        builder.append("\", \"antecedents\": {");
        boolean first = true;
        for (Map.Entry<String, Node> antecedent : antecedents.entrySet()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(antecedent.getKey()));
            builder.append("\": ");
            try {
                RDFUtils.serializeJSON(builder, antecedent.getValue());
            } catch (IOException exception) {
                // cannot happen
            }
        }
        builder.append("}}");
        return builder.toString();
    }
}
