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
import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.commons.utils.api.ReplyApiError;
import fr.cenotelie.commons.utils.api.ReplySuccess;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the current configuration of a security policy
 *
 * @author Laurent Wouters
 */
public class SecurityPolicyConfiguration implements Serializable {
    /**
     * The parts of this configuration
     */
    protected final Map<SecuredAction, SecuredActionPolicy> policies;
    /**
     * The policies for actions that are configured but cannot be found on the platform
     */
    protected final Map<String, SecuredActionPolicy> unknownActions;

    /**
     * Initializes this configuration
     */
    public SecurityPolicyConfiguration() {
        this.policies = new HashMap<>();
        this.unknownActions = new HashMap<>();
    }

    /**
     * Initializes this configuration
     *
     * @param definition The serialized definition
     */
    public SecurityPolicyConfiguration(ASTNode definition) {
        this.policies = new HashMap<>();
        this.unknownActions = new HashMap<>();
        loadDefinition(definition);
    }

    /**
     * Loads the configuration
     *
     * @param definition The serialized definition
     */
    protected void loadDefinition(ASTNode definition) {
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("parts".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    loadDefinitionPart(child);
                }
            }
        }
    }

    /**
     * Loads a policy definition
     *
     * @param mapping The mapping to load
     */
    private void loadDefinitionPart(ASTNode mapping) {
        SecuredAction action = null;
        SecuredActionPolicy policy = null;
        for (ASTNode member : mapping.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("action".equals(head)) {
                action = new SecuredAction(member.getChildren().get(1));
            } else if ("policy".equals(head)) {
                policy = SecuredActionPolicyBase.load(member.getChildren().get(1));
            }
        }
        if (action == null || policy == null) {
            // the action is invalid or the policy is invalid
            return;
        }
        policies.put(action, policy);
    }

    /**
     * Initializes this configuration
     *
     * @param definition The serialized definition
     * @param actions    The known secured actions
     */
    public SecurityPolicyConfiguration(ASTNode definition, Map<String, SecuredAction> actions) {
        this.policies = new HashMap<>();
        this.unknownActions = new HashMap<>();
        loadDefinition(definition, actions);
    }

    /**
     * Loads the configuration
     *
     * @param definition The serialized definition
     * @param actions    The known secured actions
     */
    protected void loadDefinition(ASTNode definition, Map<String, SecuredAction> actions) {
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("parts".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    loadDefinitionPart(actions, child);
                }
            }
        }

        // non-mapped actions
        for (SecuredAction action : actions.values()) {
            policies.put(action, SecuredActionPolicyDenyAll.INSTANCE);
        }
    }

    /**
     * Loads a policy definition
     *
     * @param actions The known secured actions
     * @param mapping The mapping to load
     */
    private void loadDefinitionPart(Map<String, SecuredAction> actions, ASTNode mapping) {
        String actionId = null;
        SecuredActionPolicy policy = null;
        for (ASTNode member : mapping.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("action".equals(head)) {
                actionId = getActionId(member.getChildren().get(1));
            } else if ("policy".equals(head)) {
                policy = SecuredActionPolicyBase.load(member.getChildren().get(1));
            }
        }
        if (actionId == null || policy == null) {
            // the action is invalid or the policy is invalid
            return;
        }
        SecuredAction action = actions.remove(actionId);
        if (action == null) {
            // the action is unknown
            unknownActions.put(actionId, policy);
        } else {
            policies.put(action, policy);
        }
    }

    /**
     * Gets the identifier of the secured action for the specified serialized configuration
     *
     * @param definition The AST definition
     * @return The identifier of the serialized action
     */
    private String getActionId(ASTNode definition) {
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                value = value.substring(1, value.length() - 1);
                return value;
            }
        }
        return null;
    }

    /**
     * Adds a configuration for a secured action
     *
     * @param action The secured action
     * @param policy The associated policy
     * @return The protocol reply
     */
    public Reply put(SecuredAction action, SecuredActionPolicy policy) {
        SecuredActionPolicyDescriptor[] allowedPolicies = action.getPolicies();
        boolean found = false;
        for (int i = 0; i != allowedPolicies.length; i++) {
            if (policy.getDescriptor() == allowedPolicies[i]) {
                found = true;
                break;
            }
        }
        if (!found)
            return new ReplyApiError(HttpApiService.ERROR_PARAMETER_RANGE, "The specified policy is not allowed for this action");
        synchronized (policies) {
            policies.put(action, policy);
            unknownActions.remove(action.getIdentifier());
        }
        return ReplySuccess.instance();
    }

    /**
     * Gets the secured actions known to this configuration
     *
     * @return The secured actions
     */
    public Collection<SecuredAction> getActions() {
        return policies.keySet();
    }

    /**
     * Gets the policy associated to a secured action
     *
     * @param action The secured action
     * @return The associated policy
     */
    public SecuredActionPolicy getPolicyFor(SecuredAction action) {
        synchronized (policies) {
            SecuredActionPolicy policy = policies.get(action);
            if (policy != null)
                return policy;
            policy = unknownActions.get(action.getIdentifier());
            if (policy != null) {
                policies.put(action, policy);
                unknownActions.remove(action.getIdentifier());
            }
            return policy;
        }
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(SecurityPolicyConfiguration.class.getCanonicalName()));
        builder.append("\", \"parts\": [");
        boolean first = true;
        synchronized (policies) {
            for (Map.Entry<SecuredAction, SecuredActionPolicy> couple : policies.entrySet()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append("{\"action\": ");
                builder.append(couple.getKey().serializedJSON());
                builder.append(", \"policy\": ");
                builder.append(couple.getValue().serializedJSON());
                builder.append("}");
            }
            for (Map.Entry<String, SecuredActionPolicy> couple : unknownActions.entrySet()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append("{\"action\": {\"identifier\": \"");
                builder.append(TextUtils.escapeStringJSON(couple.getKey()));
                builder.append("\", \"name\": \"");
                builder.append(TextUtils.escapeStringJSON(couple.getKey()));
                builder.append("\", \"policies\": [");
                builder.append(couple.getValue().getDescriptor().serializedJSON());
                builder.append("]}, \"policy\": ");
                builder.append(couple.getValue().serializedJSON());
                builder.append("}");
            }
        }
        builder.append("]}");
        return builder.toString();
    }
}
