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

package org.xowl.platform.satellites.base;

import org.xowl.infra.server.api.XOWLFactory;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.utils.http.HttpConnection;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.platform.kernel.Deserializer;
import org.xowl.platform.kernel.platform.PlatformUser;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The base API for accessing a remote platform
 *
 * @author Laurent Wouters
 */
public class RemotePlatform {
    /**
     * The connection to the platform
     */
    private final HttpConnection connection;
    /**
     * The factory of remote objects
     */
    private final Collection<XOWLFactory> factories;
    /**
     * The deserializer to use
     */
    private final Deserializer deserializer;
    /**
     * The currently logged-in user
     */
    private PlatformUser currentUser;
    /**
     * The login for the current user
     */
    private String currentLogin;
    /**
     * The password for the current user
     */
    private String currentPassword;

    /**
     * Initializes this platform connection
     *
     * @param endpoint     The API endpoint (https://something:port/api)
     * @param deserializer The deserializer to use
     */
    public RemotePlatform(String endpoint, Deserializer deserializer) {
        this.connection = new HttpConnection(endpoint);
        this.factories = new ArrayList<>();
        this.deserializer = deserializer;
    }

    /**
     * Gets whether a user is logged-in
     *
     * @return Whether a user is logged-in
     */
    public boolean isLoggedIn() {
        return (currentUser != null);
    }

    /**
     * Gets the currently logged-in user, if any
     *
     * @return The currently logged-in user, if any
     */
    public PlatformUser getLoggedInUser() {
        return currentUser;
    }

    /**
     * Login a user
     *
     * @param login    The user to log in
     * @param password The user password
     * @return The protocol reply, or null if the client is banned
     */
    public XSPReply login(String login, String password) {
        HttpResponse response = connection.request("/kernel/security/login" +
                        "?login=" + URIUtils.encodeComponent(login),
                HttpConstants.METHOD_POST,
                password,
                HttpConstants.MIME_TEXT_PLAIN,
                HttpConstants.MIME_TEXT_PLAIN
        );
        XSPReply reply = XSPReplyUtils.fromHttpResponse(response, deserializer);
        if (reply.isSuccess()) {
            currentUser = ((XSPReplyResult<PlatformUser>) reply).getData();
            currentLogin = login;
            currentPassword = password;
        } else {
            currentUser = null;
            currentLogin = null;
            currentPassword = null;
        }
        return reply;
    }

    /**
     * Logout the current user
     *
     * @return The protocol reply
     */
    public XSPReply logout() {
        if (currentUser == null)
            return XSPReplyNetworkError.instance();
        HttpResponse response = connection.request("/kernel/security/logout",
                HttpConstants.METHOD_POST,
                HttpConstants.MIME_TEXT_PLAIN
        );
        XSPReply reply = XSPReplyUtils.fromHttpResponse(response, deserializer);
        currentUser = null;
        currentLogin = null;
        currentPassword = null;
        return reply;
    }


    /**
     * Sends an HTTP request to the endpoint, completed with an URI complement
     *
     * @param uriComplement The URI complement to append to the original endpoint URI, if any
     * @param method        The HTTP method to use, if any
     * @param body          The request body, if any
     * @param contentType   The request body content type, if any
     * @param compressed    Whether the body is compressed with gzip
     * @param accept        The MIME type to accept for the response, if any
     * @return The response, or null if the request failed before reaching the server
     */
    public XSPReply doRequest(String uriComplement, String method, byte[] body, String contentType, boolean compressed, String accept) {
        // not logged in
        if (currentUser == null)
            return XSPReplyNetworkError.instance();
        HttpResponse response = connection.request(uriComplement,
                method,
                body,
                contentType,
                compressed,
                accept
        );
        XSPReply reply = XSPReplyUtils.fromHttpResponse(response, deserializer);
        if (reply != XSPReplyExpiredSession.instance())
            // not an authentication problem => return this reply
            return reply;
        // try to re-login
        reply = login(currentLogin, currentPassword);
        if (!reply.isSuccess())
            // failed => unauthenticated
            return XSPReplyUnauthenticated.instance();
        // now that we are logged-in, retry
        response = connection.request(uriComplement,
                method,
                body,
                contentType,
                compressed,
                accept
        );
        return XSPReplyUtils.fromHttpResponse(response, deserializer);
    }
}
