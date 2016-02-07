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

package org.xowl.platform.kernel.impl;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.kernel.SecurityService;
import org.xowl.platform.kernel.ServiceUtils;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Manages the security on the platform
 *
 * @author Laurent Wouters
 */
public class XOWLSecurityService implements SecurityService, HttpAPIService {
    /**
     * The URIs for this service
     */
    private static final String[] URIS = new String[]{
            "security"
    };

    /**
     * The security manager for the platform
     */
    private DefaultSecurityManager manager;

    /**
     * Initializes this service
     *
     * @param configurationService The configuration service
     */
    public XOWLSecurityService(ConfigurationService configurationService) {
        Configuration configuration = configurationService.getConfigFor(this);
        String realmId = configuration.get("realm");
        AuthenticatingRealm realm = null;
        if (XOWLSecurityRealm.class.getCanonicalName().equals(realmId)) {
            realm = new XOWLSecurityRealm(configuration.get(realmId, "endpoint"));
        } else if (JndiLdapRealm.class.getCanonicalName().equals(realmId)) {
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
        }
        manager = new DefaultSecurityManager(realm);
        DefaultSubjectDAO subjectDAO = ((DefaultSubjectDAO) manager.getSubjectDAO());
        DefaultSessionStorageEvaluator storageEvaluator = (DefaultSessionStorageEvaluator) subjectDAO.getSessionStorageEvaluator();
        storageEvaluator.setSessionStorageEnabled(false);
    }

    @Override
    public String getIdentifier() {
        return XOWLSecurityService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Security Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIS);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return new HttpResponse(HttpURLConnection.HTTP_FORBIDDEN);
        Subject subject = securityService.getSubject();
        if (subject == null)
            return new HttpResponse(HttpURLConnection.HTTP_FORBIDDEN);
        Object principal = subject.getPrincipal();
        if (principal == null)
            return new HttpResponse(HttpURLConnection.HTTP_FORBIDDEN);
        return new HttpResponse(HttpURLConnection.HTTP_OK, principal.toString(), HttpConstants.MIME_TEXT_PLAIN);
    }

    @Override
    public String getRealm() {
        Realm realm = manager.getRealms().iterator().next();
        return realm.getName();
    }

    @Override
    public boolean login(String host, String username, char[] password) {
        ThreadContext.bind(manager);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(username, password, host));
            return true;
        } catch (AuthenticationException exception) {
            return false;
        }
    }

    @Override
    public void logout() {
        SecurityUtils.getSubject().logout();
        ThreadContext.unbindSubject();
        ThreadContext.unbindSecurityManager();
    }

    @Override
    public Subject getSubject() {
        return SecurityUtils.getSubject();
    }
}
