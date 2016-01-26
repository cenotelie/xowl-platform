/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.kernel.impl;

import org.apache.shiro.authc.*;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.xowl.infra.server.api.XOWLServer;
import org.xowl.infra.server.api.remote.RemoteServer;
import org.xowl.infra.server.xsp.XSPReply;

/**
 * An authentication realm that uses a xOWL server as a user base
 *
 * @author Laurent Wouters
 */
public class XOWLSecurityRealm extends AuthenticatingRealm {
    /**
     * The remote server
     */
    private final XOWLServer server;

    /**
     * Initialize this realm
     *
     * @param endpoint The xOWL server API endpoint
     */
    public XOWLSecurityRealm(String endpoint) {
        this.server = new RemoteServer(endpoint);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        if (authenticationToken instanceof UsernamePasswordToken) {
            UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
            XSPReply reply = server.login(usernamePasswordToken.getUsername(), new String(usernamePasswordToken.getPassword()));
            if (reply.isSuccess())
                return new SimpleAuthenticationInfo(authenticationToken.getPrincipal(), authenticationToken.getCredentials(), this.getName());
        }
        return null;
    }
}
