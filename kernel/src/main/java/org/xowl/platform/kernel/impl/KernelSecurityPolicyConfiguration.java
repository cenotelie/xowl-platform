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

package org.xowl.platform.kernel.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyException;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.security.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a snapshot of the configuration of a security policy
 *
 * @author Laurent Wouters
 */
public class KernelSecurityPolicyConfiguration implements SecurityPolicyConfiguration {
    /**
     * The file storage for the policy
     */
    private final File storage;
    /**
     * The parts of this configuration
     */
    private final Map<SecuredAction, SecuredActionPolicy> policies;

    /**
     * Initializes this configuration
     *
     * @param storage The file storage for the policy
     */
    public KernelSecurityPolicyConfiguration(File storage) {
        this.storage = storage;
        this.policies = new HashMap<>();
        if (storage.exists()) {
            try (InputStream stream = new FileInputStream(storage)) {
                String content = Files.read(stream, Files.CHARSET);
                ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
                if (definition == null)
                    return;
                loadDefinition(definition);
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
            }
        }
    }

    /**
     * Loads the configuration
     *
     * @param definition The serialized definition
     */
    private void loadDefinition(ASTNode definition) {
        Map<String, SecuredAction> actions = new HashMap<>();
        for (SecuredService securedService : Register.getComponents(SecuredService.class)) {
            for (SecuredAction securedAction : securedService.getActions()) {
                actions.put(securedAction.getIdentifier(), securedAction);
            }
        }

        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("parts".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    loadDefinitionPart(actions, child);
                }
            }
        }
    }

    /**
     * Loads a policy definition
     *
     * @param actions The known secured actions
     * @param mapping The mapping to load
     */
    private void loadDefinitionPart(Map<String, SecuredAction> actions, ASTNode mapping) {
        SecuredAction action = null;
        SecuredActionPolicy policy = null;
        for (ASTNode member : mapping.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("action".equals(head)) {
                action = getAction(actions, member.getChildren().get(1));
            } else if ("policy".equals(head)) {
                policy = SecuredActionPolicyBase.load(member.getChildren().get(1));
            }
        }
        if (action != null && policy != null)
            policies.put(action, policy);
    }

    /**
     * Gets the secured action for the specified identifier
     *
     * @param actions    The known actions
     * @param definition The AST definition
     * @return The secured action, or null if it does not exist
     */
    private SecuredAction getAction(Map<String, SecuredAction> actions, ASTNode definition) {
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(0).getValue());
                value = head.substring(1, value.length() - 1);
                return actions.get(value);
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
    public XSPReply put(SecuredAction action, SecuredActionPolicy policy) {
        policies.put(action, policy);
        try (FileOutputStream stream = new FileOutputStream(storage)) {
            OutputStreamWriter writer = new OutputStreamWriter(stream, Files.CHARSET);
            writer.write(serializedJSON());
            writer.flush();
            writer.close();
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
        return XSPReplySuccess.instance();
    }

    /**
     * Gets the policy associated to a secured action
     *
     * @param action The secured action
     * @return The associated policy
     */
    public SecuredActionPolicy getPolicyFor(SecuredAction action) {
        return policies.get(action);
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(KernelSecurityPolicyConfiguration.class.getCanonicalName()));
        builder.append("\", \"parts\": [");
        boolean first = true;
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
        builder.append("]}");
        return builder.toString();
    }
}
