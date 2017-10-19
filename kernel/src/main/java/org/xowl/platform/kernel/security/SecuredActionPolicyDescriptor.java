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

package org.xowl.platform.kernel.security;

import fr.cenotelie.commons.utils.Serializable;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.Registrable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents the description of a policy for a secured action
 *
 * @author Laurent Wouters
 */
public class SecuredActionPolicyDescriptor implements Registrable, Serializable {
    /**
     * Represents a parameter for the associated policy
     */
    public static class Parameter implements Serializable {
        /**
         * The name of the parameter
         */
        private final String name;
        /**
         * The parameter's type
         */
        private final String type;

        /**
         * Initializes this parameter
         *
         * @param name The name of the parameter
         * @param type The parameter's type
         */
        public Parameter(String name, String type) {
            this.name = name;
            this.type = type;
        }

        /**
         * Initializes this parameter
         *
         * @param definition The AST node for the serialized definition
         */
        public Parameter(ASTNode definition) {
            String name = "";
            String type = "";
            for (ASTNode member : definition.getChildren()) {
                String head = TextUtils.unescape(member.getChildren().get(0).getValue());
                head = head.substring(1, head.length() - 1);
                if ("name".equals(head)) {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    name = value.substring(1, value.length() - 1);
                } else if ("type".equals(head)) {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    type = value.substring(1, value.length() - 1);
                }
            }
            this.name = name;
            this.type = type;
        }

        @Override
        public String serializedString() {
            return name;
        }

        @Override
        public String serializedJSON() {
            return "{\"name\": \"" +
                    TextUtils.escapeStringJSON(name) +
                    "\", \"type\": \"" +
                    TextUtils.escapeStringJSON(type) +
                    "\"}";
        }
    }

    /**
     * The identifier for this descriptor
     */
    private final String identifier;
    /**
     * The name of this descriptor
     */
    private final String name;
    /**
     * The parameters for the associated policy
     */
    private final Parameter[] parameters;

    /**
     * Initializes this descriptor
     *
     * @param identifier The identifier for this descriptor
     * @param name       The name of this descriptor
     */
    public SecuredActionPolicyDescriptor(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
        this.parameters = new Parameter[0];
    }

    /**
     * Initializes this descriptor
     *
     * @param identifier The identifier for this descriptor
     * @param name       The name of this descriptor
     * @param parameters The parameters for the associated policy
     */
    public SecuredActionPolicyDescriptor(String identifier, String name, Parameter... parameters) {
        this.identifier = identifier;
        this.name = name;
        this.parameters = parameters;
    }

    /**
     * Initializes this descriptor
     *
     * @param definition The AST node for the serialized definition
     */
    public SecuredActionPolicyDescriptor(ASTNode definition) {
        String identifier = "";
        String name = "";
        Collection<Parameter> parameters = new ArrayList<>();
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("parameters".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    parameters.add(new Parameter(child));
                }
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.parameters = parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(SecuredActionPolicyDescriptor.class.getCanonicalName()));
        builder.append("\", \"identifier\":\"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"parameters\": [");
        for (int i = 0; i != parameters.length; i++) {
            if (i != 0)
                builder.append(", ");
            builder.append(parameters[i].serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}
