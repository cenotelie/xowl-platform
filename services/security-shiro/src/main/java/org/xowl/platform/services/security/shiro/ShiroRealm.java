/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.services.security.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.security.Realm;

/**
 * A security realm for the platform that delegates to Shiro
 *
 * @author Laurent Wouters
 */
public class ShiroRealm implements Realm {
    /**
     * The Shiro security manager
     */
    private final DefaultSecurityManager manager;

    /**
     * Initializes this realm
     */
    public ShiroRealm() {
        ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
        Configuration configuration = configurationService != null ? configurationService.getConfigFor(this) : null;
        String realmId = configuration.get("provider");
        AuthenticatingRealm realm = null;

        if (JndiLdapRealm.class.getCanonicalName().equals(realmId)) {
            JndiLdapContextFactory factory = new JndiLdapContextFactory();
            factory.setUrl(configuration.get(realmId, "url"));
            String username = configuration.get(realmId, "username");
            String password = configuration.get(realmId, "password");
            String authMechanism = configuration.get(realmId, "authenticationMechanism");
            if (username != null)
                factory.setSystemUsername(username);
            if (password != null)
                factory.setSystemPassword(password);
            if (authMechanism != null)
                factory.setAuthenticationMechanism(authMechanism);
            JndiLdapRealm jndiLdapRealm = new JndiLdapRealm();
            jndiLdapRealm.setContextFactory(factory);
            realm = jndiLdapRealm;
        } else {
            realm = new SimpleAccountRealm();
        }

        manager = new DefaultSecurityManager(realm);
        DefaultSubjectDAO subjectDAO = ((DefaultSubjectDAO) manager.getSubjectDAO());
        DefaultSessionStorageEvaluator storageEvaluator = (DefaultSessionStorageEvaluator) subjectDAO.getSessionStorageEvaluator();
        storageEvaluator.setSessionStorageEnabled(false);
        manager.setAuthorizer(new ShiroAuthorizer());
    }

    @Override
    public String getIdentifier() {
        return ShiroRealm.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Shiro Realm";
    }

    @Override
    public XSPReply authenticate(String userId, char[] key) {
        ThreadContext.bind(manager);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(userId, key));
            return new XSPReplyResult<>(subject.getPrincipal().toString());
        } catch (AuthenticationException exception) {
            return XSPReplyFailure.instance();
        }
    }

    @Override
    public void onRequestEnd(String userId) {
        SecurityUtils.getSubject().logout();
        ThreadContext.unbindSubject();
        ThreadContext.unbindSecurityManager();
    }

    @Override
    public boolean checkHasRole(String userId, String roleId) {
        Subject subject = SecurityUtils.getSubject();
        return subject != null && subject.getPrincipal().equals(userId) && subject.hasRole(roleId);
    }
}
