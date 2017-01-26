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
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.*;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.io.File;

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
     * The storage for the configuration
     */
    private final File storage;
    /**
     * The configuration for this policy
     */
    private KernelSecurityPolicyConfiguration configuration;

    /**
     * Initializes this policy
     *
     * @param configuration The configuration for the policy
     */
    public KernelSecurityPolicyCustom(Section configuration) {
        this.storage = new File(System.getenv(Env.ROOT), configuration.get("storage"));
    }

    /**
     * Resolves the configuration for this policy
     *
     * @return The configuration
     */
    private KernelSecurityPolicyConfiguration resolveConfig() {
        synchronized (this) {
            if (configuration == null)
                configuration = new KernelSecurityPolicyConfiguration(storage);
            return configuration;
        }
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
        SecuredActionPolicy policy = resolveConfig().getPolicyFor(action);
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
        SecuredActionPolicy policy = resolveConfig().getPolicyFor(action);
        if (policy != null && policy.isAuthorized(securityService, user, action, data))
            return XSPReplySuccess.instance();
        return XSPReplyUnauthorized.instance();
    }

    @Override
    public XSPReply getConfiguration() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_GET_POLICY);
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(resolveConfig());
    }

    @Override
    public XSPReply setPolicy(String actionId, SecuredActionPolicy policy) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_SET_POLICY);
        if (!reply.isSuccess())
            return reply;
        for (SecuredService securedService : Register.getComponents(SecuredService.class)) {
            for (SecuredAction securedAction : securedService.getActions()) {
                if (securedAction.getIdentifier().equals(actionId)) {
                    return resolveConfig().put(securedAction, policy);
                }
            }
        }
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply setPolicy(String actionId, String policyDefinition) {
        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), policyDefinition);
        if (definition == null)
            return new XSPReplyApiError(HttpApiService.ERROR_CONTENT_PARSING_FAILED);
        SecuredActionPolicy policy = SecuredActionPolicyBase.load(definition);
        if (policy == null)
            return new XSPReplyApiError(HttpApiService.ERROR_CONTENT_PARSING_FAILED, "Invalid policy definition");
        return setPolicy(actionId, policy);
    }
}
