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
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformGroup;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.*;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
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
public class KernelSecurityService implements SecurityService, HttpApiService {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(KernelSecurityService.class, "/org/xowl/platform/kernel/impl/api_security.raml", "Security Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(KernelSecurityService.class, "/org/xowl/platform/kernel/impl/api_security.html", "Security Service - Documentation", HttpApiResource.MIME_HTML);


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
     * The URI for the API services
     */
    private final String apiUri;
    /**
     * The maximum number of login failure before ban
     */
    private final int maxLoginFailure;
    /**
     * The length of a ban in second
     */
    private final int banLength;
    /**
     * The configuration section for the security realm
     */
    private final Section realmConfiguration;
    /**
     * The configuration section for the security policy
     */
    private final Section policyConfiguration;
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
    private SecurityRealm realm;
    /**
     * The authorization policy
     */
    private SecurityPolicy policy;

    /**
     * Initializes this service
     *
     * @param configurationService The configuration service
     */
    public KernelSecurityService(ConfigurationService configurationService) {
        Configuration configuration = configurationService.getConfigFor(SecurityService.class.getCanonicalName());
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/kernel/security";
        this.maxLoginFailure = Integer.parseInt(configuration.get("maxLoginFailure"));
        this.banLength = Integer.parseInt(configuration.get("banLength"));
        this.realmConfiguration = configuration.getSection("realm");
        this.policyConfiguration = configuration.getSection("policy");
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
        return KernelSecurityService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Security Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (request.getUri().equals(apiUri + "/login"))
            return handleRequestLogin(request);
        if (request.getUri().equals(apiUri + "/logout"))
            return handleRequestLogout(request);
        if (request.getUri().equals(apiUri + "/me"))
            return handleRequestMe(request);
        if (request.getUri().startsWith(apiUri + "/policy"))
            return handleRequestPolicy(request);
        if (request.getUri().startsWith(apiUri + "/users"))
            return handleRequestUsers(request);
        if (request.getUri().startsWith(apiUri + "/groups"))
            return handleRequestGroups(request);
        if (request.getUri().startsWith(apiUri + "/roles"))
            return handleRequestRoles(request);
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Override
    public HttpApiResource getApiSpecification() {
        return RESOURCE_SPECIFICATION;
    }

    @Override
    public HttpApiResource getApiDocumentation() {
        return RESOURCE_DOCUMENTATION;
    }

    @Override
    public HttpApiResource[] getApiOtherResources() {
        return null;
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(HttpApiService.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"specification\": " +
                RESOURCE_SPECIFICATION.serializedJSON() +
                ", \"documentation\": " +
                RESOURCE_DOCUMENTATION.serializedJSON() +
                "}";
    }

    @Override
    public synchronized SecurityRealm getRealm() {
        if (realm != null)
            return realm;
        String identifier = realmConfiguration.get("type");
        for (SecurityRealmProvider provider : Register.getComponents(SecurityRealmProvider.class)) {
            realm = provider.newRealm(identifier, realmConfiguration);
            if (realm != null)
                return realm;
        }
        realm = new KernelSecurityNosecRealm();
        return realm;
    }

    @Override
    public synchronized SecurityPolicy getPolicy() {
        if (policy != null)
            return policy;
        String identifier = policyConfiguration.get("type");
        for (SecurityPolicyProvider provider : Register.getComponents(SecurityPolicyProvider.class)) {
            policy = provider.newPolicy(identifier, policyConfiguration);
            if (policy != null)
                return policy;
        }
        policy = new KernelSecurityPolicyAuthenticated();
        return policy;
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
        PlatformUser user = getRealm().getUser(((XSPReplyResult<String>) reply).getData());
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
    public XSPReply checkAction(SecuredAction action) {
        return getPolicy().checkAction(this, action);
    }

    @Override
    public XSPReply checkAction(SecuredAction action, Object data) {
        return getPolicy().checkAction(this, action, data);
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

        String login = request.getParameter("login");
        if (login == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'login'"), null);
        String password = new String(request.getContent(), Files.CHARSET);
        XSPReply reply = login(request.getClient(), login, password);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        String token = ((XSPReplyResult<String>) reply).getData();
        HttpResponse response = new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, getCurrentUser().serializedJSON());
        response.addHeader(HttpConstants.HEADER_SET_COOKIE, AUTH_TOKEN + "=" + token +
                "; Max-Age=" + Long.toString(securityTokenTTL) +
                "; Path=" + PlatformHttp.getUriPrefixApi() +
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
                "; Path=" + PlatformHttp.getUriPrefixApi() +
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
     * Responds to a request for the policy resource
     *
     * @param request The web API request to handle
     * @return The HTTP response
     */
    private HttpResponse handleRequestPolicy(HttpApiRequest request) {
        if (request.getUri().equals(apiUri + "/policy")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return XSPReplyUtils.toHttpResponse(getPolicy().getConfiguration(), null);
        }
        if (request.getUri().startsWith(apiUri + "/policy/actions/")) {
            String rest = request.getUri().substring(apiUri.length() + "/policy/actions/".length());
            String actionId = URIUtils.decodeComponent(rest);
            if (actionId.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            if (!HttpConstants.METHOD_PUT.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected PUT method");
            String definition = new String(request.getContent(), Files.CHARSET);
            return XSPReplyUtils.toHttpResponse(getPolicy().setPolicy(actionId, definition), null);
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Responds to a request for the users resource
     *
     * @param request The web API request to handle
     * @return The HTTP response
     */
    private HttpResponse handleRequestUsers(HttpApiRequest request) {
        if (request.getUri().equals(apiUri + "/users")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            Collection<PlatformUser> users = getRealm().getUsers();
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

        String rest = request.getUri().substring(apiUri.length() + "/users".length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String userId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);

        if (index < 0) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    PlatformUser user = getRealm().getUser(userId);
                    if (user == null)
                        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, user.serializedJSON());
                }
                case HttpConstants.METHOD_PUT: {
                    String displayName = request.getParameter("name");
                    if (displayName == null)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                    String password = new String(request.getContent(), Files.CHARSET);
                    return XSPReplyUtils.toHttpResponse(getRealm().createUser(userId, displayName, password), null);
                }
                case HttpConstants.METHOD_DELETE: {
                    return XSPReplyUtils.toHttpResponse(getRealm().deleteUser(userId), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
        }

        switch (rest.substring(index)) {
            case "/rename": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String displayName = request.getParameter("name");
                if (displayName == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().renameUser(userId, displayName), null);
            }
            case "/updateKey": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String oldKey = request.getParameter("oldKey");
                if (oldKey == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'oldKey'"), null);
                String password = new String(request.getContent(), Files.CHARSET);
                return XSPReplyUtils.toHttpResponse(getRealm().changeUserKey(userId, oldKey, password), null);
            }
            case "/resetKey": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String password = new String(request.getContent(), Files.CHARSET);
                return XSPReplyUtils.toHttpResponse(getRealm().resetUserKey(userId, password), null);
            }
            case "/assign": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String role = request.getParameter("role");
                if (role == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'role'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().assignRoleToUser(userId, role), null);
            }
            case "/unassign": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String role = request.getParameter("role");
                if (role == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'role'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().unassignRoleToUser(userId, role), null);
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
        if (request.getUri().equals(apiUri + "/groups")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            Collection<PlatformGroup> groups = getRealm().getGroups();
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

        String rest = request.getUri().substring(apiUri.length() + "/groups".length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String groupId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);

        if (index < 0) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    PlatformGroup group = getRealm().getGroup(groupId);
                    if (group == null)
                        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, group.serializedJSON());
                }
                case HttpConstants.METHOD_PUT: {
                    String displayName = request.getParameter("name");
                    if (displayName == null)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                    String admin = request.getParameter("admin");
                    if (admin == null)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'admin'"), null);
                    return XSPReplyUtils.toHttpResponse(getRealm().createGroup(groupId, displayName, admin), null);
                }
                case HttpConstants.METHOD_DELETE: {
                    return XSPReplyUtils.toHttpResponse(getRealm().deleteGroup(groupId), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
        }

        switch (rest.substring(index)) {
            case "/rename": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String displayName = request.getParameter("name");
                if (displayName == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().renameGroup(groupId, displayName), null);
            }
            case "/addMember": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String user = request.getParameter("user");
                if (user == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().addUserToGroup(user, groupId), null);
            }
            case "/removeMember": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String user = request.getParameter("user");
                if (user == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().removeUserFromGroup(user, groupId), null);
            }
            case "/addAdmin": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String user = request.getParameter("user");
                if (user == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().addAdminToGroup(user, groupId), null);
            }
            case "/removeAdmin": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String user = request.getParameter("user");
                if (user == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().removeAdminFromGroup(user, groupId), null);
            }
            case "/assign": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String role = request.getParameter("role");
                if (role == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'role'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().assignRoleToGroup(groupId, role), null);
            }
            case "/unassign": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String role = request.getParameter("role");
                if (role == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'role'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().unassignRoleToGroup(groupId, role), null);
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
        if (request.getUri().equals(apiUri + "/roles")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            Collection<PlatformRole> roles = getRealm().getRoles();
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

        String rest = request.getUri().substring(apiUri.length() + "/roles".length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String roleId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);

        if (index < 0) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    PlatformRole role = getRealm().getRole(roleId);
                    if (role == null)
                        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, role.serializedJSON());
                }
                case HttpConstants.METHOD_PUT: {
                    String displayName = request.getParameter("name");
                    if (displayName == null)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                    return XSPReplyUtils.toHttpResponse(getRealm().createRole(roleId, displayName), null);
                }
                case HttpConstants.METHOD_DELETE: {
                    return XSPReplyUtils.toHttpResponse(getRealm().deleteRole(roleId), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
        }

        switch (rest.substring(index)) {
            case "/rename": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String displayName = request.getParameter("name");
                if (displayName == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().renameRole(roleId, displayName), null);
            }
            case "/imply": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String target = request.getParameter("target");
                if (target == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'target'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().addRoleImplication(roleId, target), null);
            }
            case "/unimply": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String target = request.getParameter("target");
                if (target == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'target'"), null);
                return XSPReplyUtils.toHttpResponse(getRealm().removeRoleImplication(roleId, target), null);
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
