/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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

package org.xowl.platform.kernel;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.xowl.platform.kernel.impl.XOWLSecurityRealm;

/**
 * Manages the security on the platform
 *
 * @author Laurent Wouters
 */
public class Security {
    /**
     * Initializes the platform's security
     */
    static void initialize() {
        AuthenticatingRealm realm = new XOWLSecurityRealm(null);
        RealmSecurityManager manager = new DefaultSecurityManager(realm);
        SecurityUtils.setSecurityManager(manager);
    }

    /**
     * Gets the realm of this platform
     *
     * @return The realm of this platform
     */
    public static String getRealm() {
        Realm realm = ((RealmSecurityManager) SecurityUtils.getSecurityManager()).getRealms().iterator().next();
        return realm.getName();
    }

    /**
     * Logins a user on this thread
     *
     * @param host     The requesting host
     * @param username The username
     * @param password The password
     * @return Whether the operation succeed
     */
    public static boolean login(String host, String username, char[] password) {
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(username, password, host));
            return true;
        } catch (AuthenticationException exception) {
            return false;
        }
    }

    /**
     * Logouts the user on this thread
     */
    public static void logout() {
        SecurityUtils.getSubject().logout();
    }
}
