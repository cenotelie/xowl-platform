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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.BufferedLogger;
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

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     * The configuration section for the token service
     */
    private final Section tokenServiceConfiguration;
    /**
     * The configuration section for the descriptors of secured resources
     */
    private final Section descriptorsConfiguration;
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
     * The token service
     */
    private SecurityTokenService tokenService;
    /**
     * The manager of secured resources
     */
    private SecuredResourceManager resourceManager;

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
        this.tokenServiceConfiguration = configuration.getSection("tokens");
        this.descriptorsConfiguration = configuration.getSection("descriptors");
        this.securityTokenTTL = Integer.parseInt(tokenServiceConfiguration.get("tokenTTL"));
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
        if (request.getUri().startsWith(apiUri + "/resources"))
            return handleRequestResources(request);
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
    public synchronized SecuredResourceManager getSecuredResources() {
        if (resourceManager != null)
            return resourceManager;
        resourceManager = new KernelSecuredResourceManager(descriptorsConfiguration);
        return resourceManager;
    }

    @Override
    public XSPReply login(String client, String login, String password) {
        if (isBanned(client))
            return XSPReplyUnauthenticated.instance();
        if (login == null || login.isEmpty() || password == null || password.length() == 0) {
            onLoginFailure(client);
            Logging.get().info("Authentication failure from " + client + " on initial login with " + login);
            return XSPReplyUnauthenticated.instance();
        }
        PlatformUser user = getRealm().authenticate(login, password);
        if (user != null) {
            CONTEXT.set(user);
            return new XSPReplyResult<>(getTokenService().newTokenFor(login));
        }
        onLoginFailure(client);
        Logging.get().info("Authentication failure from " + client + " on initial login with " + login);
        return XSPReplyUnauthenticated.instance();
    }

    @Override
    public XSPReply logout() {
        CONTEXT.remove();
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply authenticate(String client, String token) {
        if (isBanned(client))
            return XSPReplyUnauthenticated.instance();
        XSPReply reply = getTokenService().checkToken(token);
        if (reply == XSPReplyUnauthenticated.instance()) {
            // the token is invalid
            onLoginFailure(client);
            Logging.get().info("Authentication failure from " + client + " with invalid token");
            return reply;
        }
        if (!reply.isSuccess()) {
            Logging.get().info("Authentication failure from " + client + " with invalid token");
            return reply;
        }
        PlatformUser user = getRealm().getUser(((XSPReplyResult<String>) reply).getData());
        CONTEXT.set(user);
        return new XSPReplyResult<>(user);
    }

    @Override
    public XSPReply authenticate(PlatformUser user) {
        PlatformUser previous = CONTEXT.get();
        if (previous == null) {
            // not authenticated yet
            CONTEXT.set(user);
            return XSPReplySuccess.instance();
        }
        XSPReply reply = checkAction(ACTION_CHANGE_ID);
        if (!reply.isSuccess())
            return reply;
        CONTEXT.set(user);
        return XSPReplySuccess.instance();
    }

    @Override
    public PlatformUser getCurrentUser() {
        return CONTEXT.get();
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
     * Gets the token service
     *
     * @return The token service
     */
    private synchronized SecurityTokenService getTokenService() {
        if (tokenService != null)
            return tokenService;
        String identifier = tokenServiceConfiguration.get("type");
        for (SecurityTokenServiceProvider provider : Register.getComponents(SecurityTokenServiceProvider.class)) {
            tokenService = provider.newService(identifier, policyConfiguration);
            if (tokenService != null)
                return tokenService;
        }
        tokenService = new KernelSecurityTokenService(tokenServiceConfiguration);
        return tokenService;
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
                Logging.get().info("Client " + client + " is no longer banned");
                return false;
            }
        }
    }

    /**
     * Handles a login failure from a client
     *
     * @param client The client trying to login
     */
    private void onLoginFailure(String client) {
        synchronized (clients) {
            ClientLogin cl = clients.get(client);
            if (cl == null) {
                cl = new ClientLogin();
                clients.put(client, cl);
            }
            cl.failedAttempt++;
            if (InetAddress.getLoopbackAddress().getHostAddress().equals(client)) {
                // the loopback client cannot be banned
                return;
            }
            if (cl.failedAttempt >= maxLoginFailure) {
                // too much failure, ban this client for a while
                Logging.get().info("Banned client " + client + " for " + banLength + " seconds");
                cl.banTimeStamp = Calendar.getInstance().getTime().getTime();
            }
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
        String password = new String(request.getContent(), IOUtils.CHARSET);
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
        XSPReply reply = logout();
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
            String definition = new String(request.getContent(), IOUtils.CHARSET);
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
                    String password = new String(request.getContent(), IOUtils.CHARSET);
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
                String password = new String(request.getContent(), IOUtils.CHARSET);
                return XSPReplyUtils.toHttpResponse(getRealm().changeUserKey(userId, oldKey, password), null);
            }
            case "/resetKey": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String password = new String(request.getContent(), IOUtils.CHARSET);
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
     * Responds to a request for the resources resource
     *
     * @param request The web API request to handle
     * @return The HTTP response
     */
    private HttpResponse handleRequestResources(HttpApiRequest request) {
        if (request.getUri().equals(apiUri + "/resources"))
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);

        String rest = request.getUri().substring(apiUri.length() + "/resources".length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String resourceId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);

        if (index < 0) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return XSPReplyUtils.toHttpResponse(getSecuredResources().getDescriptorFor(resourceId), null);
        }

        switch (rest.substring(index)) {
            case "/addOwner": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String user = request.getParameter("user");
                if (user == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(getSecuredResources().addOwner(resourceId, user), null);
            }
            case "/removeOwner": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String user = request.getParameter("user");
                if (user == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'user'"), null);
                return XSPReplyUtils.toHttpResponse(getSecuredResources().removeOwner(resourceId, user), null);
            }
            case "/addSharing": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String content = new String(request.getContent(), IOUtils.CHARSET);
                if (content.isEmpty())
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                BufferedLogger logger = new BufferedLogger();
                ASTNode root = JsonLoader.parseJson(logger, content);
                if (root == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
                SecuredResourceSharing sharing = SecuredResourceDescriptorBase.loadSharing(root);
                if (sharing == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, "JSON object is not a sharing definition"), null);
                return XSPReplyUtils.toHttpResponse(getSecuredResources().addSharing(resourceId, sharing), null);
            }
            case "/removeSharing": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                String content = new String(request.getContent(), IOUtils.CHARSET);
                if (content.isEmpty())
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                BufferedLogger logger = new BufferedLogger();
                ASTNode root = JsonLoader.parseJson(logger, content);
                if (root == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
                SecuredResourceSharing sharing = SecuredResourceDescriptorBase.loadSharing(root);
                if (sharing == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, "JSON object is not a sharing definition"), null);
                return XSPReplyUtils.toHttpResponse(getSecuredResources().removeSharing(resourceId, sharing), null);
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }
}
