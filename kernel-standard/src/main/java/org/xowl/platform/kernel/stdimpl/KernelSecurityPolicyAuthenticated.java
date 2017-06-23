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

package org.xowl.platform.kernel.stdimpl;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.server.xsp.XSPReplyUnauthenticated;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredActionPolicy;
import org.xowl.platform.kernel.security.SecurityPolicy;
import org.xowl.platform.kernel.security.SecurityService;

/**
 * Implements an authorization policy in which any authenticated user can do anything
 *
 * @author Laurent Wouters
 */
public class KernelSecurityPolicyAuthenticated implements SecurityPolicy {
    @Override
    public String getIdentifier() {
        return KernelSecurityPolicyAuthenticated.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Authenticated Security Policy";
    }

    @Override
    public XSPReply checkAction(SecurityService securityService, SecuredAction action) {
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            return XSPReplyUnauthenticated.instance();
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply checkAction(SecurityService securityService, SecuredAction action, Object data) {
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            return XSPReplyUnauthenticated.instance();
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply getConfiguration() {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply setPolicy(String actionId, SecuredActionPolicy policy) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply setPolicy(String actionId, String policyDefinition) {
        return XSPReplyUnsupported.instance();
    }
}
