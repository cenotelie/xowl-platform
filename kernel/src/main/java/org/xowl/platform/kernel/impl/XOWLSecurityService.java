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
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.platform.PlatformGroup;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.Realm;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiService;

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
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/kernel/security";

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
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/login"))
            return handleRequestLogin(request);
        if (request.getUri().equals(URI_API + "/logout"))
            return handleRequestLogout(request);
        if (request.getUri().equals(URI_API + "/me"))
            return handleRequestMe(request);
        if (request.getUri().startsWith(URI_API + "/users"))
            return handleRequestUsers(request);
        if (request.getUri().startsWith(URI_API + "/groups"))
            return handleRequestGroups(request);
        if (request.getUri().startsWith(URI_API + "/roles"))
            return handleRequestRoles(request);
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
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
    public XSPReply logout(String client) {
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
     * Responds to a request for the login resource
     *
     * @param request The web API request to handle
     * @return The HTTP response
     */
    private HttpResponse handleRequestLogin(HttpApiRequest request) {
        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");

        String[] logins = request.getParameter("login");
        if (logins == null || logins.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'login'"), null);
        String password = new String(request.getContent(), Files.CHARSET);
        XSPReply reply = login(request.getClient(), logins[0], password);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        String token = ((XSPReplyResult<String>) reply).getData();
        HttpResponse response = new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, getCurrentUser().serializedJSON());
        response.addHeader(HttpConstants.HEADER_SET_COOKIE, AUTH_TOKEN + "=" + token +
                "; Max-Age=" + Long.toString(securityTokenTTL) +
                "; Path=" + HttpApiService.URI_API +
                "; Secure" +
                "; HttpOnly");
        return response;
    }

    /**
     * Responds to a request for the logout resource
     *
     * @param request The web API request to handle
     * @return The HTTP response
     */
    private HttpResponse handleRequestLogout(HttpApiRequest request) {
        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
        XSPReply reply = logout(request.getClient());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        HttpResponse response = new HttpResponse(HttpURLConnection.HTTP_OK);
        response.addHeader(HttpConstants.HEADER_SET_COOKIE, AUTH_TOKEN + "= " +
                "; Max-Age=0" +
                "; Path=" + HttpApiService.URI_API +
                "; Secure" +
                "; HttpOnly");
        return response;
    }

    /**
     * Responds to a request for the me resource
     *
     * @param request The web API request to handle
     * @return The HTTP response
     */
    private HttpResponse handleRequestMe(HttpApiRequest request) {
        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, getCurrentUser().serializedJSON());
    }

    /**
     * Responds to a request for the usets resource
     *
     * @param request The web API request to handle
     * @return The HTTP response
     */
    private HttpResponse handleRequestUsers(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/users")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
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
        }

        String rest = request.getUri().substring(URI_API.length() + "/users".length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String userId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);

        if (index < 0) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    PlatformUser user = realm.getUser(userId);
                    if (user == null)
                        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, user.serializedJSON());
                }
                case HttpConstants.METHOD_PUT: {
                    String[] displayNames = request.getParameter("name");
                    if (displayNames == null || displayNames.length == 0)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                    String password = new String(request.getContent(), Files.CHARSET);
                    return XSPReplyUtils.toHttpResponse(realm.createUser(userId, displayNames[0], password), null);
                }
                case HttpConstants.METHOD_DELETE: {
                    return XSPReplyUtils.toHttpResponse(realm.deleteUser(userId), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
        }

        switch (rest.substring(index)) {
            case "/rename": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] displayNames = request.getParameter("name");
                if (displayNames == null || displayNames.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                return XSPReplyUtils.toHttpResponse(realm.renameUser(userId, displayNames[0]), null);
            }
            case "/updateKey": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] oldKeys = request.getParameter("oldKey");
                if (oldKeys == null || oldKeys.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'oldKey'"), null);
                String password = new String(request.getContent(), Files.CHARSET);
                return XSPReplyUtils.toHttpResponse(realm.changeUserKey(userId, oldKeys[0], password), null);
            }
            case "/resetKey": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String password = new String(request.getContent(), Files.CHARSET);
                return XSPReplyUtils.toHttpResponse(realm.resetUserKey(userId, password), null);
            }
            case "/assign": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] roles = request.getParameter("role");
                if (roles == null || roles.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'role'"), null);
                return XSPReplyUtils.toHttpResponse(realm.assignRoleToUser(userId, roles[0]), null);
            }
            case "/unassign": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] roles = request.getParameter("role");
                if (roles == null || roles.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'role'"), null);
                return XSPReplyUtils.toHttpResponse(realm.unassignRoleToUser(userId, roles[0]), null);
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Responds to a request for the groups resource
     *
     * @param request The web API request to handle
     * @return The HTTP response
     */
    private HttpResponse handleRequestGroups(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/groups")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
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
        }

        String rest = request.getUri().substring(URI_API.length() + "/groups".length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String groupId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);

        if (index < 0) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    PlatformGroup group = realm.getGroup(groupId);
                    if (group == null)
                        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, group.serializedJSON());
                }
                case HttpConstants.METHOD_PUT: {
                    String[] displayNames = request.getParameter("name");
                    if (displayNames == null || displayNames.length == 0)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                    String[] admins = request.getParameter("admin");
                    if (admins == null || admins.length == 0)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'admin'"), null);
                    return XSPReplyUtils.toHttpResponse(realm.createGroup(groupId, displayNames[0], admins[0]), null);
                }
                case HttpConstants.METHOD_DELETE: {
                    return XSPReplyUtils.toHttpResponse(realm.deleteGroup(groupId), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
        }

        switch (rest.substring(index)) {
            case "/rename": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] displayNames = request.getParameter("name");
                if (displayNames == null || displayNames.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                return XSPReplyUtils.toHttpResponse(realm.renameGroup(groupId, displayNames[0]), null);
            }
            case "/addMember": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] users = request.getParameter("user");
                if (users == null || users.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(realm.addUserToGroup(users[0], groupId), null);
            }
            case "/removeMember": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] users = request.getParameter("user");
                if (users == null || users.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(realm.removeUserFromGroup(users[0], groupId), null);
            }
            case "/addAdmin": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] users = request.getParameter("user");
                if (users == null || users.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(realm.addAdminToGroup(users[0], groupId), null);
            }
            case "/removeAdmin": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] users = request.getParameter("user");
                if (users == null || users.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(realm.removeAdminFromGroup(users[0], groupId), null);
            }
            case "/assign": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] roles = request.getParameter("role");
                if (roles == null || roles.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'role'"), null);
                return XSPReplyUtils.toHttpResponse(realm.assignRoleToGroup(groupId, roles[0]), null);
            }
            case "/unassign": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] roles = request.getParameter("role");
                if (roles == null || roles.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'role'"), null);
                return XSPReplyUtils.toHttpResponse(realm.unassignRoleToGroup(groupId, roles[0]), null);
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Responds to a request for the roles resource
     *
     * @param request The web API request to handle
     * @return The HTTP response
     */
    private HttpResponse handleRequestRoles(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/roles")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
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
        }

        String rest = request.getUri().substring(URI_API.length() + "/roles".length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String roleId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);

        if (index < 0) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    PlatformRole role = realm.getRole(roleId);
                    if (role == null)
                        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, role.serializedJSON());
                }
                case HttpConstants.METHOD_PUT: {
                    String[] displayNames = request.getParameter("name");
                    if (displayNames == null || displayNames.length == 0)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                    return XSPReplyUtils.toHttpResponse(realm.createRole(roleId, displayNames[0]), null);
                }
                case HttpConstants.METHOD_DELETE: {
                    return XSPReplyUtils.toHttpResponse(realm.deleteRole(roleId), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
        }

        switch (rest.substring(index)) {
            case "/rename": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] displayNames = request.getParameter("name");
                if (displayNames == null || displayNames.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                return XSPReplyUtils.toHttpResponse(realm.renameRole(roleId, displayNames[0]), null);
            }
            case "/imply": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] targets = request.getParameter("target");
                if (targets == null || targets.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'target'"), null);
                return XSPReplyUtils.toHttpResponse(realm.addRoleImplication(roleId, targets[0]), null);
            }
            case "/unimply": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String[] targets = request.getParameter("target");
                if (targets == null || targets.length == 0)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'target'"), null);
                return XSPReplyUtils.toHttpResponse(realm.removeRoleImplication(roleId, targets[0]), null);
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
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
