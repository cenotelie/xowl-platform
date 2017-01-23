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
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.server.xsp.XSPReplyUnauthenticated;
import org.xowl.infra.server.xsp.XSPReplyUnauthorized;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements an authorization policy which can be configured by the users.
 * Authorization rules:
 * - Platform administrators can do anything, even if not specified
 * - If no security policy is defined for an action, it is forbidden (except for platform administrators)
 * - Otherwise the authorization is delegated to the security policy for the action
 *
 * @author Laurent Wouters
 */
public class KernelSecurityPolicyCustom implements SecurityPolicy {
    /**
     * The file storage for the policy
     */
    private final File storage;
    /**
     * The policies for the secured actions
     */
    private Map<String, SecuredActionPolicy> policies;

    /**
     * Initializes this policy
     *
     * @param configuration The configuration for the policy
     */
    public KernelSecurityPolicyCustom(Section configuration) {
        this.storage = new File(System.getenv(Env.ROOT), configuration.get("storage"));
    }

    /**
     * Gets the policy for a secured action, if defined
     *
     * @param actionId The identifier of a secured action
     * @return The policy, if defined
     */
    private SecuredActionPolicy getPolicyFor(String actionId) {
        if (policies != null)
            return policies.get(actionId);
        synchronized (this) {
            if (policies != null)
                return policies.get(actionId);
            policies = new HashMap<>();
            if (storage.exists()) {
                try (InputStream stream = new FileInputStream(storage)) {
                    String content = Files.read(stream, Files.CHARSET);
                    ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
                    if (definition == null)
                        return null;
                    for (ASTNode mapping : definition.getChildren()) {
                        loadMapping(mapping);
                    }
                } catch (IOException exception) {
                    Logging.getDefault().error(exception);
                    return null;
                }
            }
            return policies.get(actionId);
        }
    }

    /**
     * Loads a policy definition
     *
     * @param mapping The mapping to load
     */
    private void loadMapping(ASTNode mapping) {
        String action = null;
        SecuredActionPolicy policy = null;
        for (ASTNode member : mapping.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("action".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                action = value.substring(1, value.length() - 1);
            } else if ("policy".equals(head)) {
                policy = SecuredActionPolicyBase.load(member.getChildren().get(1));
            }
        }
        if (action != null && policy != null)
            policies.put(action, policy);
    }

    @Override
    public String getIdentifier() {
        return KernelSecurityPolicyCustom.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Custom Security Policy";
    }

    @Override
    public XSPReply checkAction(SecurityService securityService, SecuredAction action) {
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            // no user => un-authenticated
            return XSPReplyUnauthenticated.instance();
        if (securityService.getRealm().checkHasRole(user.getIdentifier(), PlatformRoleAdmin.INSTANCE.getIdentifier()))
            // user is platform admin => authorized
            return XSPReplySuccess.instance();
        // check the custom action policies
        SecuredActionPolicy policy = getPolicyFor(action.getIdentifier());
        if (policy != null && policy.isAuthorized(securityService, user, action))
            return XSPReplySuccess.instance();
        return XSPReplyUnauthorized.instance();
    }

    @Override
    public XSPReply checkAction(SecurityService securityService, SecuredAction action, Object data) {
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            // no user => un-authenticated
            return XSPReplyUnauthenticated.instance();
        if (securityService.getRealm().checkHasRole(user.getIdentifier(), PlatformRoleAdmin.INSTANCE.getIdentifier()))
            // user is platform admin => authorized
            return XSPReplySuccess.instance();
        // check the custom action policies
        SecuredActionPolicy policy = getPolicyFor(action.getIdentifier());
        if (policy != null && policy.isAuthorized(securityService, user, action, data))
            return XSPReplySuccess.instance();
        return XSPReplyUnauthorized.instance();
    }
}
