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
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredActionPolicy;
import org.xowl.platform.kernel.security.SecuredActionPolicyDenyAll;
import org.xowl.platform.kernel.security.SecurityPolicyConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a snapshot of the configuration of a security policy
 *
 * @author Laurent Wouters
 */
public class KernelSecurityPolicyConfiguration extends SecurityPolicyConfiguration {
    /**
     * The file storage for the policy
     */
    private final File storage;

    /**
     * Initializes this configuration
     *
     * @param storage The file storage for the policy
     */
    public KernelSecurityPolicyConfiguration(File storage) {
        this.storage = storage;
        if (storage.exists()) {
            try (InputStream stream = new FileInputStream(storage)) {
                String content = Files.read(stream, Files.CHARSET);
                ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
                if (definition == null)
                    return;
                loadDefinition(definition, SecuredAction.getAll());
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
            }
        }
    }

    /**
     * Write the current configuration to the storage
     *
     * @return The protocol reply
     */
    private XSPReply writeBack() {
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
     * Synchronizes the secured actions with the current state of the configuration
     *
     * @return This configuration
     */
    public KernelSecurityPolicyConfiguration synchronize() {
        Map<String, SecuredAction> actions = SecuredAction.getAll();
        boolean changed = false;
        synchronized (policies) {
            // remove the mapped actions
            for (SecuredAction action : policies.keySet()) {
                actions.remove(action.getIdentifier());
            }
            // try to resolve the unknown actions
            if (unknownActions.size() > 0) {
                Collection<String> identifiers = new ArrayList<>(unknownActions.keySet());
                for (String identifier : identifiers) {
                    SecuredAction action = actions.get(identifier);
                    if (action != null) {
                        actions.remove(identifier);
                        policies.put(action, unknownActions.remove(identifier));
                        changed = true;
                    }
                }
            }
            // the remaining actions are then unmapped
            for (SecuredAction action : actions.values()) {
                policies.put(action, SecuredActionPolicyDenyAll.INSTANCE);
                changed = true;
            }
        }
        if (changed)
            writeBack();
        return this;
    }

    @Override
    public XSPReply put(SecuredAction action, SecuredActionPolicy policy) {
        XSPReply reply = super.put(action, policy);
        if (!reply.isSuccess())
            return reply;
        return writeBack();
    }
}
