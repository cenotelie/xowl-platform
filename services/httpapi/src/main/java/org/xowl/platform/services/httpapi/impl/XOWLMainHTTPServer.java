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

package org.xowl.platform.services.httpapi.impl;

import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.commons.utils.api.ReplyException;
import fr.cenotelie.commons.utils.api.ReplyExpiredSession;
import fr.cenotelie.commons.utils.api.ReplyUtils;
import fr.cenotelie.commons.utils.collections.Couple;
import fr.cenotelie.commons.utils.http.HttpConstants;
import fr.cenotelie.commons.utils.http.HttpResponse;
import fr.cenotelie.commons.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.httpapi.HTTPServerService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Enumeration;

/**
 * The main server for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLMainHTTPServer extends HttpServlet implements HTTPServerService {
    @Override
    public String getIdentifier() {
        return XOWLMainHTTPServer.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - HTTP Server Service";
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        addCORSHeader(request, response);
        addCacheControlHeader(response);
        response.setStatus(HttpURLConnection.HTTP_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    /**
     * Handles a request
     *
     * @param servletRequest  The request
     * @param servletResponse The response
     */
    private void handleRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        addCORSHeader(servletRequest, servletResponse);
        addCacheControlHeader(servletResponse);

        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null) {
            servletResponse.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
            return;
        }

        HttpApiRequest apiRequest = new XOWLHttpApiRequest(servletRequest);
        try {
            Collection<HttpApiService> services = Register.getComponents(HttpApiService.class);
            HttpApiService service = null;
            int priority = HttpApiService.CANNOT_HANDLE;
            for (HttpApiService candidate : services) {
                int result = candidate.canHandle(apiRequest);
                if (result > priority) {
                    service = candidate;
                    priority = result;
                }
            }
            if (service == null) {
                servletResponse.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                return;
            }
            if (service.requireAuth(apiRequest) && !checkAuthentication(securityService, servletRequest, servletResponse)) {
                servletResponse.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                return;
            }
            doResponse(servletResponse, service.handle(securityService, apiRequest));
        } catch (Throwable exception) {
            Logging.get().error(exception);
            doResponse(servletResponse, ReplyUtils.toHttpResponse(new ReplyException(exception)));
        } finally {
            securityService.logout();
        }
    }

    /**
     * Checks the authentication of the request
     *
     * @param securityService The current security service
     * @param request         The request
     * @param response        The response
     * @return Whether a user is authenticated for the request
     */
    private boolean checkAuthentication(SecurityService securityService, HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> values = request.getHeaders(HttpConstants.HEADER_COOKIE);
        if (values != null) {
            while (values.hasMoreElements()) {
                String content = values.nextElement();
                String[] parts = content.split(";");
                for (String cookie : parts) {
                    cookie = cookie.trim();
                    if (cookie.startsWith(securityService.getTokens().getTokenName() + "=")) {
                        String token = cookie.substring(securityService.getTokens().getTokenName().length() + 1);
                        Reply reply = securityService.authenticate(request.getRemoteAddr(), token);
                        if (reply.isSuccess())
                            return true;
                        if (reply == ReplyExpiredSession.instance())
                            response.setStatus(HttpConstants.HTTP_SESSION_EXPIRED);
                        else
                            response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                        return false;
                    }
                }
            }
        }
        response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
        return false;
    }

    /**
     * Outputs an API response to the servlet response
     *
     * @param servletResponse The servlet response to fill
     * @param apiResponse     The API response
     */
    private void doResponse(HttpServletResponse servletResponse, HttpResponse apiResponse) {
        servletResponse.setStatus(apiResponse.getCode());
        for (Couple<String, String> header : apiResponse.getHeaders()) {
            servletResponse.addHeader(header.x, header.y);
        }
        if (apiResponse.getContentType() != null)
            servletResponse.setContentType(apiResponse.getContentType());
        if (apiResponse.getBodyAsBytes() != null) {
            servletResponse.setContentLength(apiResponse.getBodyAsBytes().length);
            try (OutputStream os = servletResponse.getOutputStream()) {
                os.write(apiResponse.getBodyAsBytes());
                os.flush();
            } catch (IOException exception) {
                Logging.get().error(exception);
            }
        }
    }

    /**
     * Adds the cache control headers a response
     *
     * @param response The response to add headers to
     */
    private void addCacheControlHeader(HttpServletResponse response) {
        response.setHeader("Cache-Control", "private, no-cache, no-store, no-transform, must-revalidate");
    }

    /**
     * Adds the headers required in a response for the support of Cross-Origin Resource Sharing
     *
     * @param request  The request
     * @param response The response to add headers to
     */
    private void addCORSHeader(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin == null) {
            // the request is from the same host
            origin = request.getHeader("Host");
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type, Authorization, Cache-Control");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
