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
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformGroup;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecurityRealm;

import java.util.Collection;
import java.util.Collections;

/**
 * A security realm for the platform that delegates to Shiro
 *
 * @author Laurent Wouters
 */
public class ShiroRealm implements SecurityRealm {
    /**
     * The internal representation of a user for this realm
     */
    private static class User implements PlatformUser {
        /**
         * The identifier of the user
         */
        private final String identifier;

        /**
         * Initializes this user
         *
         * @param identifier The identifier of the user
         */
        public User(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String getName() {
            return identifier;
        }

        @Override
        public Collection<PlatformRole> getRoles() {
            return Collections.emptyList();
        }

        @Override
        public String serializedString() {
            return identifier;
        }

        @Override
        public String serializedJSON() {
            return "{\"type\": \"" +
                    TextUtils.escapeStringJSON(PlatformUser.class.getCanonicalName()) +
                    "\", \"identifier\": \"" +
                    TextUtils.escapeStringJSON(identifier) +
                    "\",  \"name\": \"" +
                    TextUtils.escapeStringJSON(identifier) +
                    "\"}";
        }

        @Override
        public String toString() {
            return identifier;
        }
    }

    /**
     * The Shiro security manager
     */
    private final DefaultSecurityManager manager;

    /**
     * Initializes this realm
     */
    public ShiroRealm() {
        ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
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
    public PlatformUser authenticate(String login, String password) {
        ThreadContext.bind(manager);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(login, password));
            String principal = subject.getPrincipal().toString();
            return new User(principal);
        } catch (AuthenticationException exception) {
            return null;
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

    @Override
    public Collection<PlatformUser> getUsers() {
        return Collections.emptyList();
    }

    @Override
    public Collection<PlatformGroup> getGroups() {
        return Collections.emptyList();
    }

    @Override
    public Collection<PlatformRole> getRoles() {
        return Collections.emptyList();
    }

    @Override
    public PlatformUser getUser(String identifier) {
        return null;
    }

    @Override
    public PlatformGroup getGroup(String identifier) {
        return null;
    }

    @Override
    public PlatformRole getRole(String identifier) {
        return null;
    }

    @Override
    public XSPReply createUser(String identifier, String name, String key) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply createGroup(String identifier, String name, String adminId) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply createRole(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply renameUser(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply renameGroup(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply renameRole(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply deleteUser(String identifier) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply deleteGroup(String identifier) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply deleteRole(String identifier) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply changeUserKey(String identifier, String oldKey, String newKey) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply resetUserKey(String identifier, String newKey) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply addUserToGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply addAdminToGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply removeUserFromGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply removeAdminFromGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply assignRoleToUser(String user, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply assignRoleToGroup(String group, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply unassignRoleToUser(String user, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply unassignRoleToGroup(String group, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply addRoleImplication(String sourceRole, String targetRole) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply removeRoleImplication(String sourceRole, String targetRole) {
        return XSPReplyUnsupported.instance();
    }
}
