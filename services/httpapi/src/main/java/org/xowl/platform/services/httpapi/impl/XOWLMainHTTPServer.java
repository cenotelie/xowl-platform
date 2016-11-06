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

import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.services.httpapi.HTTPServerService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;

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
        return "xOWL Federation Platform - HTTP Server Service";
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        addCORSHeader(request, response);
        addCacheControlHeader(response);
        response.setStatus(HttpURLConnection.HTTP_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest("GET", request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest("POST", request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest("PUT", request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest("DELETE", request, response);
    }

    /**
     * Handles a request
     *
     * @param method   The HTTP method
     * @param request  The request
     * @param response The response
     */
    private void handleRequest(String method, HttpServletRequest request, HttpServletResponse response) {
        addCORSHeader(request, response);
        addCacheControlHeader(response);
        try {
            String uri = request.getRequestURI();
            uri = uri.substring(HttpAPIService.URI_API.length() + 1);
            Collection<HttpAPIService> services = ServiceUtils.getServices(HttpAPIService.class);
            for (HttpAPIService service : services) {
                if (service.getURIs().contains(uri)) {
                    doServedService(service, method, uri, request, response);
                    return;
                }
            }
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        } catch (Throwable exception) {
            Logging.getDefault().error(exception);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            SecurityService securityService = ServiceUtils.getService(SecurityService.class);
            if (securityService != null)
                securityService.onRequestEnd(request.getRemoteAddr());
        }
    }

    /**
     * Responds on a request for a served service
     *
     * @param service  The served service
     * @param uri      The service-specific part of the requested URI
     * @param method   The HTTP method
     * @param request  The request
     * @param response The response
     */
    private void doServedService(HttpAPIService service, String method, String uri, HttpServletRequest request, HttpServletResponse response) {
        byte[] content = null;
        if (request.getContentLength() > 0) {
            try (InputStream is = request.getInputStream()) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int read = is.read(buffer);
                while (read > 0) {
                    output.write(buffer, 0, read);
                    read = is.read(buffer);
                }
                content = output.toByteArray();
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
            }
        }
        HttpResponse serviceResponse = service.onMessage(method,
                uri,
                request.getParameterMap(),
                request.getContentType(),
                content,
                request.getHeader("Accept"));
        if (serviceResponse == null) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            return;
        }
        response.setStatus(serviceResponse.getCode());
        if (serviceResponse.getContentType() != null)
            response.setContentType(serviceResponse.getContentType());
        if (serviceResponse.getBodyAsBytes() != null) {
            response.setContentLength(serviceResponse.getBodyAsBytes().length);
            try (OutputStream os = response.getOutputStream()) {
                os.write(serviceResponse.getBodyAsBytes());
                os.flush();
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
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
