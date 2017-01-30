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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.Register;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user action for a service
 *
 * @author Laurent Wouters
 */
public class SecuredAction implements Identifiable, Serializable {
    /**
     * The default authorization policies
     */
    public static final SecuredActionPolicyDescriptor[] DEFAULT_POLICIES = new SecuredActionPolicyDescriptor[]{
            SecuredActionPolicyDenyAll.DESCRIPTOR,
            SecuredActionPolicyAllowAll.DESCRIPTOR,
            SecuredActionPolicyHasRole.DESCRIPTOR,
            SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR
    };

    /**
     * The identifier for this action
     */
    protected final String identifier;
    /**
     * The name of this action
     */
    protected final String name;
    /**
     * The possible authorization policies for this action
     */
    protected final SecuredActionPolicyDescriptor[] policies;

    /**
     * Initializes this action
     *
     * @param identifier The identifier for this action
     * @param name       The name of this action
     */
    public SecuredAction(String identifier, String name) {
        this(identifier, name, DEFAULT_POLICIES);
    }

    /**
     * Initializes this action
     *
     * @param identifier The identifier for this action
     * @param name       The name of this action
     * @param policies   The identifiers of the possible authorization policies for this action
     */
    public SecuredAction(String identifier, String name, SecuredActionPolicyDescriptor... policies) {
        this.identifier = identifier;
        this.name = name;
        this.policies = policies;
    }

    /**
     * Initializes this action
     *
     * @param definition The AST node for the serialized definition
     */
    public SecuredAction(ASTNode definition) {
        String identifier = "";
        String name = "";
        Collection<SecuredActionPolicyDescriptor> policies = new ArrayList<>();
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("policies".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    policies.add(new SecuredActionPolicyDescriptor(child));
                }
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.policies = (SecuredActionPolicyDescriptor[]) policies.toArray();
    }

    /**
     * Gets the identifiers of the possible authorization policies for this action
     *
     * @return The identifiers of the possible authorization policies for this action
     */
    public SecuredActionPolicyDescriptor[] getPolicies() {
        return policies;
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
        builder.append(TextUtils.escapeStringJSON(SecuredAction.class.getCanonicalName()));
        builder.append("\", \"identifier\":\"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"policies\": [");
        for (int i = 0; i != policies.length; i++) {
            if (i != 0)
                builder.append(", ");
            builder.append(policies[i].serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }

    /**
     * Retrieves all the currently registered secured actions on the platform
     *
     * @return The registered secured actions
     */
    public static Map<String, SecuredAction> getAll() {
        Map<String, SecuredAction> actions = new HashMap<>();
        for (SecuredService securedService : Register.getComponents(SecuredService.class)) {
            for (SecuredAction securedAction : securedService.getActions()) {
                actions.put(securedAction.getIdentifier(), securedAction);
            }
        }
        return actions;
    }
}
