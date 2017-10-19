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

import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.commons.utils.api.ReplySuccess;
import fr.cenotelie.commons.utils.api.ReplyUnauthenticated;
import fr.cenotelie.commons.utils.api.ReplyUnsupported;
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
    public Reply checkAction(SecurityService securityService, SecuredAction action) {
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            return ReplyUnauthenticated.instance();
        return ReplySuccess.instance();
    }

    @Override
    public Reply checkAction(SecurityService securityService, SecuredAction action, Object data) {
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            return ReplyUnauthenticated.instance();
        return ReplySuccess.instance();
    }

    @Override
    public Reply getConfiguration() {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply setPolicy(String actionId, SecuredActionPolicy policy) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply setPolicy(String actionId, String policyDefinition) {
        return ReplyUnsupported.instance();
    }
}
