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
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredActionPolicy;
import org.xowl.platform.kernel.security.SecuredActionPolicyDenyAll;
import org.xowl.platform.kernel.security.SecurityPolicyConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a snapshot of the configuration of a security policy
 *
 * @author Laurent Wouters
 */
class KernelSecurityPolicyConfiguration extends SecurityPolicyConfiguration {
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
            try (Reader reader = IOUtils.getReader(storage)) {
                ASTNode definition = JsonLoader.parseJson(Logging.get(), reader);
                if (definition == null)
                    return;
                loadDefinition(definition, SecuredAction.getAll());
            } catch (IOException exception) {
                Logging.get().error(exception);
            }
        }
    }

    /**
     * Write the current configuration to the storage
     *
     * @return The protocol reply
     */
    private XSPReply writeBack() {
        try (Writer writer = IOUtils.getWriter(storage)) {
            writer.write(serializedJSON());
            writer.flush();
        } catch (IOException exception) {
            Logging.get().error(exception);
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
