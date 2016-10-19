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

package org.xowl.platform.kernel.impl;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.Realm;
import org.xowl.platform.kernel.security.SecurityService;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.*;

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
            "services/core/security"
    };

    /**
     * The data about a client
     */
    private static class ClientLogin {
        /**
         * The number of failed attempt
         */
        public int failedAttempt = 0;
        /**
         * The timestamp of the ban
         */
        public long banTimeStamp = -1;
    }

    /**
     * The context of a thread
     */
    private static final ThreadLocal<PlatformUser> CONTEXT = new ThreadLocal<>();
    /**
     * The maximum number of login failure before ban
     */
    private final int maxLoginFailure;
    /**
     * The length of a ban in second
     */
    private final int banLength;
    /**
     * The identifier of the security realm
     */
    private final String realmId;
    /**
     * The map of clients with failed login attempts
     */
    private final Map<String, ClientLogin> clients;
    /**
     * The security realm
     */
    private Realm realm;

    /**
     * Initializes this service
     *
     * @param configurationService The configuration service
     */
    public XOWLSecurityService(ConfigurationService configurationService) {
        Configuration configuration = configurationService.getConfigFor(this);
        this.maxLoginFailure = Integer.parseInt(configuration.get("maxLoginFailure"));
        this.banLength = Integer.parseInt(configuration.get("banLength"));
        this.realmId = configuration.get("realm");
        this.clients = new HashMap<>();
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
        PlatformUser principal = CONTEXT.get();
        if (principal == null)
            return new HttpResponse(HttpURLConnection.HTTP_FORBIDDEN);
        return new HttpResponse(HttpURLConnection.HTTP_OK, principal.serializedJSON(), HttpConstants.MIME_JSON);
    }

    @Override
    public Realm getRealm() {
        if (realm != null)
            return realm;
        realm = ServiceUtils.getService(Realm.class, Realm.PROPERTY_ID, realmId);
        if (realm == null)
            realm = new XOWLSecurityNosecRealm();
        return realm;
    }

    @Override
    public XSPReply authenticate(String client, String userId, char[] key) {
        if (isBanned(client))
            return null;
        if (userId == null || userId.isEmpty() || key == null || key.length == 0) {
            boolean banned = onLoginFailure(client);
            Logging.getDefault().info("Login failure for " + userId + " from " + client);
            return banned ? null : XSPReplyFailure.instance();
        }
        XSPReply reply = getRealm().authenticate(userId, key);
        if (reply.isSuccess()) {
            CONTEXT.set(((XSPReplyResult<PlatformUser>) reply).getData());
            return reply;
        }
        boolean banned = onLoginFailure(client);
        Logging.getDefault().info("Login failure for " + userId + " from " + client);
        return banned ? null : XSPReplyFailure.instance();
    }

    @Override
    public void onRequestEnd(String userId) {
        CONTEXT.remove();
        getRealm().onRequestEnd(userId);
    }

    @Override
    public boolean checkCurrentHasRole(String roleId) {
        PlatformUser user = CONTEXT.get();
        return user != null && getRealm().checkHasRole(user.getIdentifier(), roleId);
    }

    /**
     * Gets whether a client is banned
     *
     * @param client A client
     * @return Whether the client is banned
     */
    private boolean isBanned(String client) {
        synchronized (clients) {
            ClientLogin cl = clients.get(client);
            if (cl == null)
                return false;
            if (cl.banTimeStamp == -1)
                return false;
            long now = Calendar.getInstance().getTime().getTime();
            long diff = now - cl.banTimeStamp;
            if (diff < 1000 * banLength) {
                // still banned
                return true;
            } else {
                // not banned anymore
                clients.remove(client);
                return false;
            }
        }
    }

    /**
     * Handles a login failure from a client
     *
     * @param client The client trying to login
     * @return Whether the failure resulted in the client being banned
     */
    private boolean onLoginFailure(String client) {
        synchronized (clients) {
            ClientLogin cl = clients.get(client);
            if (cl == null) {
                cl = new ClientLogin();
                clients.put(client, cl);
            }
            cl.failedAttempt++;
            if (InetAddress.getLoopbackAddress().getHostAddress().equals(client)) {
                // the loopback client cannot be banned
                return false;
            }
            if (cl.failedAttempt >= maxLoginFailure) {
                // too much failure, ban this client for a while
                Logging.getDefault().info("Banned client " + client + " for " + banLength + " seconds");
                cl.banTimeStamp = Calendar.getInstance().getTime().getTime();
                return true;
            }
            return false;
        }
    }
}
