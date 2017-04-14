/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.api.XOWLRule;
import org.xowl.infra.server.base.BaseRule;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.services.consistency.ReasoningRule;

/**
 * Implements a reasoning rule for the platform
 *
 * @author Laurent Wouters
 */
class XOWLReasoningRule implements ReasoningRule {
    /**
     * The original RDF rule
     */
    private final XOWLRule original;
    /**
     * The user-friendly name
     */
    private final String name;

    /**
     * Initializes this rule
     *
     * @param original The original RDF rule
     * @param name     The user-friendly name
     */
    public XOWLReasoningRule(XOWLRule original, String name) {
        this.original = original;
        this.name = name;
    }

    /**
     * Initializes this rule
     *
     * @param root The rule's definition
     */
    public XOWLReasoningRule(ASTNode root) {
        String identifier = null;
        String name = null;
        String definition = null;
        boolean isActive = false;
        for (ASTNode child : root.getChildren()) {
            ASTNode nodeMemberName = child.getChildren().get(0);
            String keyName = TextUtils.unescape(nodeMemberName.getValue());
            keyName = keyName.substring(1, keyName.length() - 1);
            switch (keyName) {
                case "identifier": {
                    ASTNode nodeValue = child.getChildren().get(1);
                    String value = TextUtils.unescape(nodeValue.getValue());
                    identifier = value.substring(1, value.length() - 1);
                    break;
                }
                case "name": {
                    ASTNode nodeValue = child.getChildren().get(1);
                    String value = TextUtils.unescape(nodeValue.getValue());
                    name = value.substring(1, value.length() - 1);
                    break;
                }
                case "definition": {
                    ASTNode nodeValue = child.getChildren().get(1);
                    String value = TextUtils.unescape(nodeValue.getValue());
                    definition = value.substring(1, value.length() - 1);
                    break;
                }
                case "isActive": {
                    ASTNode nodeValue = child.getChildren().get(1);
                    String value = TextUtils.unescape(nodeValue.getValue());
                    isActive = value.equalsIgnoreCase("true");
                    break;
                }
            }
        }
        this.original = new BaseRule(identifier, definition, isActive);
        this.name = name;
    }

    @Override
    public String getIdentifier() {
        return original.getIdentifier();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActive() {
        return original.isActive();
    }

    @Override
    public String getDefinition() {
        return original.getDefinition();
    }

    @Override
    public String serializedString() {
        return original.getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(ReasoningRule.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(original.getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"definition\": \"" +
                TextUtils.escapeStringJSON(original.getDefinition()) +
                "\", \"isActive\": " +
                Boolean.toString(original.isActive()) +
                "}";
    }
}
