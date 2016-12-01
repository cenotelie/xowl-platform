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

import org.xowl.infra.server.xsp.*;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.HttpApiService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.platform.PlatformGroup;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.Realm;
import org.xowl.platform.kernel.security.SecurityService;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Manages the security on the platform
 *
 * @author Laurent Wouters
 */
public class XOWLSecurityService implements SecurityService, HttpApiService {
    /**
     * The URIs for this service
     */
    private static final String[] URIS = new String[]{
            "services/core/security",
            "services/core/security/users",
            "services/core/security/groups",
            "services/core/security/roles"
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
     * The Message Authentication Code algorithm to use for securing user tokens
     */
    private final Mac securityMAC;
    /**
     * The private security key for the Message Authentication Code
     */
    private final Key securityKey;
    /**
     * The time to live in seconds of an authentication token
     */
    private final long securityTokenTTL;
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
        Mac mac = null;
        KeyGenerator keyGenerator = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            keyGenerator.init(256);
        } catch (NoSuchAlgorithmException exception) {
            // should not happen
            Logging.getDefault().error(exception);
        }
        this.securityMAC = mac;
        this.securityKey = keyGenerator.generateKey();
        this.securityTokenTTL = Integer.parseInt(configuration.get("tokenTTL"));
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
        switch (uri) {
            case "services/core/security":
                return onMessageCore(method);
            case "services/core/security/users":
                return onMessageUsers(method, parameters);
            case "services/core/security/groups":
                return onMessageGroups(method, parameters);
            case "services/core/security/roles":
                return onMessageRoles(method, parameters);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
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
    public XSPReply login(String client, String login, String password) {
        if (isBanned(client))
            return XSPReplyUnauthenticated.instance();
        if (login == null || login.isEmpty() || password == null || password.length() == 0) {
            onLoginFailure(client);
            Logging.getDefault().info("Authentication failure from " + client + " on initial login with " + login);
            return XSPReplyUnauthenticated.instance();
        }
        PlatformUser user = getRealm().authenticate(login, password);
        if (user != null) {
            CONTEXT.set(user);
            return new XSPReplyResult<>(buildTokenFor(login));
        }
        onLoginFailure(client);
        Logging.getDefault().info("Authentication failure from " + client + " on initial login with " + login);
        return XSPReplyUnauthenticated.instance();
    }

    @Override
    public XSPReply logout(String client, String token) {
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply authenticate(String client, String token) {
        if (isBanned(client))
            return XSPReplyUnauthenticated.instance();
        XSPReply reply = checkToken(token);
        if (reply == XSPReplyUnauthenticated.instance()) {
            // the token is invalid
            onLoginFailure(client);
            Logging.getDefault().info("Authentication failure from " + client + " with invalid token");
            return reply;
        }
        if (!reply.isSuccess()) {
            Logging.getDefault().info("Authentication failure from " + client + " with invalid token");
            return reply;
        }
        PlatformUser user = realm.getUser(((XSPReplyResult<String>) reply).getData());
        CONTEXT.set(user);
        return new XSPReplyResult<>(user);
    }

    @Override
    public void authenticate(PlatformUser user) {
        CONTEXT.set(user);
    }

    @Override
    public PlatformUser getCurrentUser() {
        return CONTEXT.get();
    }

    @Override
    public void onRequestEnd(String userId) {
        CONTEXT.remove();
        getRealm().onRequestEnd(userId);
    }

    @Override
    public XSPReply checkCurrentHasRole(String roleId) {
        PlatformUser user = CONTEXT.get();
        if (user == null)
            return XSPReplyUnauthenticated.instance();
        if (!getRealm().checkHasRole(user.getIdentifier(), roleId))
            return XSPReplyUnauthorized.instance();
        return XSPReplySuccess.instance();
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
                Logging.getDefault().info("Client " + client + " is no longer banned");
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

    /**
     * Responds to a request on the core URI
     *
     * @param method The HTTP method
     * @return The HTTP response
     */
    private HttpResponse onMessageCore(String method) {
        if ("GET".equals(method))
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, getCurrentUser().serializedJSON());
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Responds to a request on the URI for users
     *
     * @param method     The HTTP method
     * @param parameters The parameters
     * @return The HTTP response
     */
    private HttpResponse onMessageUsers(String method, Map<String, String[]> parameters) {
        if ("GET".equals(method)) {
            String[] ids = parameters.get("id");
            if (ids != null && ids.length > 0) {
                PlatformUser user = realm.getUser(ids[0]);
                if (user == null)
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, user.serializedJSON());
            }
            Collection<PlatformUser> users = realm.getUsers();
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (PlatformUser user : users) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(user.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else if ("PUT".equals(method)) {
            String[] ids = parameters.get("id");
            String[] names = parameters.get("name");
            String[] keys = parameters.get("key");
            if (ids == null || ids.length == 0 || names == null || names.length == 0 || keys == null || keys.length == 0)
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            return XSPReplyUtils.toHttpResponse(realm.createUser(ids[0], names[0], keys[0]), null);
        } else if ("DELETE".equals(method)) {
            String[] ids = parameters.get("id");
            if (ids == null || ids.length == 0)
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            return XSPReplyUtils.toHttpResponse(realm.deleteUser(ids[0]), null);
        } else if ("POST".equals(method)) {
            String[] actions = parameters.get("action");
            String[] ids = parameters.get("id");
            if (ids == null || ids.length == 0 || actions == null || actions.length == 0)
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            if ("rename".equals(actions[0])) {
                String[] names = parameters.get("name");
                if (names == null || names.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.renameUser(ids[0], names[0]), null);
            } else if ("changeKey".equals(actions[0])) {
                String[] oldKeys = parameters.get("oldKey");
                String[] newKeys = parameters.get("newKey");
                if (oldKeys == null || oldKeys.length == 0 || newKeys == null || newKeys.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.changeUserKey(ids[0], oldKeys[0], newKeys[0]), null);
            } else if ("resetKey".equals(actions[0])) {
                String[] newKeys = parameters.get("newKey");
                if (newKeys == null || newKeys.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.resetUserKey(ids[0], newKeys[0]), null);
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Responds to a request on the URI for groups
     *
     * @param method     The HTTP method
     * @param parameters The parameters
     * @return The HTTP response
     */
    private HttpResponse onMessageGroups(String method, Map<String, String[]> parameters) {
        if ("GET".equals(method)) {
            String[] ids = parameters.get("id");
            if (ids != null && ids.length > 0) {
                PlatformGroup group = realm.getGroup(ids[0]);
                if (group == null)
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, group.serializedJSON());
            }
            Collection<PlatformGroup> groups = realm.getGroups();
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (PlatformGroup group : groups) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(group.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else if ("PUT".equals(method)) {
            String[] ids = parameters.get("id");
            String[] names = parameters.get("name");
            String[] admins = parameters.get("admin");
            if (ids == null || ids.length == 0 || names == null || names.length == 0 || admins == null || admins.length == 0)
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            return XSPReplyUtils.toHttpResponse(realm.createGroup(ids[0], names[0], admins[0]), null);
        } else if ("DELETE".equals(method)) {
            String[] ids = parameters.get("id");
            if (ids == null || ids.length == 0)
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            return XSPReplyUtils.toHttpResponse(realm.deleteGroup(ids[0]), null);
        } else if ("POST".equals(method)) {
            String[] actions = parameters.get("action");
            String[] ids = parameters.get("id");
            if (ids == null || ids.length == 0 || actions == null || actions.length == 0)
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            if ("rename".equals(actions[0])) {
                String[] names = parameters.get("name");
                if (names == null || names.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.renameGroup(ids[0], names[0]), null);
            } else if ("addMember".equals(actions[0])) {
                String[] users = parameters.get("user");
                if (users == null || users.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.addUserToGroup(users[0], ids[0]), null);
            } else if ("removeMember".equals(actions[0])) {
                String[] users = parameters.get("user");
                if (users == null || users.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.removeUserFromGroup(users[0], ids[0]), null);
            } else if ("addAdmin".equals(actions[0])) {
                String[] users = parameters.get("user");
                if (users == null || users.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.addAdminToGroup(users[0], ids[0]), null);
            } else if ("removeAdmin".equals(actions[0])) {
                String[] users = parameters.get("user");
                if (users == null || users.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.removeAdminFromGroup(users[0], ids[0]), null);
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Responds to a request on the URI for roles
     *
     * @param method     The HTTP method
     * @param parameters The parameters
     * @return The HTTP response
     */
    private HttpResponse onMessageRoles(String method, Map<String, String[]> parameters) {
        if ("GET".equals(method)) {
            String[] ids = parameters.get("id");
            if (ids != null && ids.length > 0) {
                PlatformRole role = realm.getRole(ids[0]);
                if (role == null)
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, role.serializedJSON());
            }
            Collection<PlatformRole> roles = realm.getRoles();
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (PlatformRole role : roles) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(role.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else if ("PUT".equals(method)) {
            String[] ids = parameters.get("id");
            String[] names = parameters.get("name");
            if (ids == null || ids.length == 0 || names == null || names.length == 0)
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            return XSPReplyUtils.toHttpResponse(realm.createRole(ids[0], names[0]), null);
        } else if ("DELETE".equals(method)) {
            String[] ids = parameters.get("id");
            if (ids == null || ids.length == 0)
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            return XSPReplyUtils.toHttpResponse(realm.deleteRole(ids[0]), null);
        } else if ("POST".equals(method)) {
            String[] actions = parameters.get("action");
            String[] ids = parameters.get("id");
            if (ids == null || ids.length == 0 || actions == null || actions.length == 0)
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            if ("rename".equals(actions[0])) {
                String[] names = parameters.get("name");
                if (names == null || names.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.renameRole(ids[0], names[0]), null);
            } else if ("assign".equals(actions[0])) {
                String[] users = parameters.get("user");
                if (users != null && users.length > 0)
                    return XSPReplyUtils.toHttpResponse(realm.assignRoleToUser(users[0], ids[0]), null);
                String[] groups = parameters.get("group");
                if (groups != null && groups.length > 0)
                    return XSPReplyUtils.toHttpResponse(realm.assignRoleToGroup(groups[0], ids[0]), null);
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            } else if ("unassign".equals(actions[0])) {
                String[] users = parameters.get("user");
                if (users != null && users.length > 0)
                    return XSPReplyUtils.toHttpResponse(realm.unassignRoleToUser(users[0], ids[0]), null);
                String[] groups = parameters.get("group");
                if (groups != null && groups.length > 0)
                    return XSPReplyUtils.toHttpResponse(realm.unassignRoleToGroup(groups[0], ids[0]), null);
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            } else if ("addImplication".equals(actions[0])) {
                String[] targets = parameters.get("target");
                if (targets == null || targets.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.addRoleImplication(ids[0], targets[0]), null);
            } else if ("removeImplication".equals(actions[0])) {
                String[] targets = parameters.get("target");
                if (targets == null || targets.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(realm.removeRoleImplication(ids[0], targets[0]), null);
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD);
    }


    /**
     * Builds an authentication token for the specified login
     *
     * @param login The user login
     * @return The new authentication token
     */
    private String buildTokenFor(String login) {
        long timestamp = System.currentTimeMillis();
        long validUntil = timestamp + securityTokenTTL * 1000;
        byte[] text = login.getBytes(Files.CHARSET);
        byte[] tokenData = Arrays.copyOf(text, text.length + 8);
        tokenData[text.length] = (byte) ((validUntil & 0xFF00000000000000L) >>> 56);
        tokenData[text.length + 1] = (byte) ((validUntil & 0x00FF000000000000L) >>> 48);
        tokenData[text.length + 2] = (byte) ((validUntil & 0x0000FF0000000000L) >>> 40);
        tokenData[text.length + 3] = (byte) ((validUntil & 0x000000FF00000000L) >>> 32);
        tokenData[text.length + 4] = (byte) ((validUntil & 0x00000000FF000000L) >>> 24);
        tokenData[text.length + 5] = (byte) ((validUntil & 0x0000000000FF0000L) >>> 16);
        tokenData[text.length + 6] = (byte) ((validUntil & 0x000000000000FF00L) >>> 8);
        tokenData[text.length + 7] = (byte) ((validUntil & 0x00000000000000FFL));

        synchronized (securityMAC) {
            try {
                securityMAC.init(securityKey);
                byte[] tokenHash = securityMAC.doFinal(tokenData);
                byte[] token = Arrays.copyOf(tokenData, tokenData.length + tokenHash.length);
                System.arraycopy(tokenHash, 0, token, tokenData.length, tokenHash.length);
                return org.xowl.infra.utils.Base64.encodeBase64(token);
            } catch (InvalidKeyException exception) {
                Logging.getDefault().error(exception);
                return null;
            }
        }
    }

    /**
     * Checks whether the token is valid
     *
     * @param token The authentication token to check
     * @return The protocol reply, or null if the token is invalid
     */
    private XSPReply checkToken(String token) {
        byte[] tokenBytes = org.xowl.infra.utils.Base64.decodeBase64(token);
        if (tokenBytes.length <= 32 + 8)
            return XSPReplyUnauthenticated.instance();
        byte[] tokenData = Arrays.copyOf(tokenBytes, tokenBytes.length - 32);
        byte[] hashProvided = new byte[32];
        System.arraycopy(tokenBytes, tokenBytes.length - 32, hashProvided, 0, 32);

        // checks the hash
        synchronized (securityMAC) {
            try {
                securityMAC.init(securityKey);
                byte[] computedHash = securityMAC.doFinal(tokenData);
                if (!Arrays.equals(hashProvided, computedHash))
                    // the token does not checks out ...
                    return XSPReplyUnauthenticated.instance();
            } catch (InvalidKeyException exception) {
                Logging.getDefault().error(exception);
                return new XSPReplyException(exception);
            }
        }

        byte b0 = tokenBytes[tokenBytes.length - 32 - 8];
        byte b1 = tokenBytes[tokenBytes.length - 32 - 7];
        byte b2 = tokenBytes[tokenBytes.length - 32 - 6];
        byte b3 = tokenBytes[tokenBytes.length - 32 - 5];
        byte b4 = tokenBytes[tokenBytes.length - 32 - 4];
        byte b5 = tokenBytes[tokenBytes.length - 32 - 3];
        byte b6 = tokenBytes[tokenBytes.length - 32 - 2];
        byte b7 = tokenBytes[tokenBytes.length - 32 - 1];
        long validUntil = ((long) b0 & 0xFFL) << 56
                | ((long) b1 & 0xFFL) << 48
                | ((long) b2 & 0xFFL) << 40
                | ((long) b3 & 0xFFL) << 32
                | ((long) b4 & 0xFFL) << 24
                | ((long) b5 & 0xFFL) << 16
                | ((long) b6 & 0xFFL) << 8
                | ((long) b7 & 0xFFL);
        if (System.currentTimeMillis() > validUntil)
            // the token expired
            return XSPReplyExpiredSession.instance();
        return new XSPReplyResult<>(new String(tokenBytes, 0, tokenBytes.length - 32 - 8, Files.CHARSET));
    }
}
