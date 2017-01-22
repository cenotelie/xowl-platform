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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.server.xsp.XSPReplyUnauthenticated;
import org.xowl.platform.kernel.ServiceAction;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecurityPolicy;
import org.xowl.platform.kernel.security.SecurityService;

/**
 * Implements an authorization policy which can be configured by the users.
 * When an authorization policy for a service action is not found the action is authorized for authenticated users.
 *
 * @author Laurent Wouters
 */
public class XOWLSecurityPolicyCustom implements SecurityPolicy {
    @Override
    public String getIdentifier() {
        return XOWLSecurityPolicyAuthenticated.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Custom Authorization Policy";
    }

    @Override
    public XSPReply checkAction(SecurityService securityService, ServiceAction action) {
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            return XSPReplyUnauthenticated.instance();
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply checkAction(SecurityService securityService, ServiceAction action, Object data) {
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            return XSPReplyUnauthenticated.instance();
        return XSPReplySuccess.instance();
    }
}
