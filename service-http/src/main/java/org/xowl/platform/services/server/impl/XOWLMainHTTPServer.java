/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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

package org.xowl.platform.services.server.impl;

import org.xowl.platform.kernel.ServiceHttpServed;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.server.HTTPServerService;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.utils.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

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
    public String getProperty(String name) {
        if (name == null)
            return null;
        if ("identifier".equals(name))
            return getIdentifier();
        if ("name".equals(name))
            return getName();
        return null;
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCORSPreflight(response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest("GET", request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest("POST", request, response);
    }

    /**
     * Handles a request
     *
     * @param method   The HTTP method
     * @param request  The request
     * @param response The response
     */
    private void handleRequest(String method, HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/service/")) {
            ServiceHttpServed service = ServiceUtils.getService(ServiceHttpServed.class, "id", uri.substring("/service/".length()));
            if (service == null) {
                addCORSHeader(response);
                response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            } else {
                doServedService(service, method, request, response);
            }
        } else {
            uri = uri.substring(1);
            ServiceHttpServed service = ServiceUtils.getService(ServiceHttpServed.class, "uri", uri);
            if (service == null) {
                addCORSHeader(response);
                response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            } else {
                doServedService(service, method, request, response);
            }
        }
    }

    /**
     * Responds to a CORS pre-flight request
     *
     * @param response The HTTP response
     */
    private void doCORSPreflight(HttpServletResponse response) {
        addCORSHeader(response);
        response.setStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * Responds on a request for a served service
     *
     * @param service  The served service
     * @param method   The HTTP method
     * @param request  The request
     * @param response The response
     */
    private void doServedService(ServiceHttpServed service, String method, HttpServletRequest request, HttpServletResponse response) {
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
                Logger.DEFAULT.error(exception);
            }
        }
        HttpResponse serviceResponse = service.onMessage(method,
                request.getParameterMap(),
                request.getContentType(),
                content,
                request.getHeader("Accept"));
        if (serviceResponse == null) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            return;
        }
        response.setStatus(serviceResponse.code);
        if (serviceResponse.contentType != null)
            response.setContentType(serviceResponse.contentType);
        if (serviceResponse.content != null) {
            try (OutputStream os = response.getOutputStream()) {
                os.write(serviceResponse.content);
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
        }
    }

    /**
     * Adds the headers required in a response for the support of Cross-Origin Resource Sharing
     *
     * @param response The response to add headers to
     */
    private void addCORSHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type, Cache-Control");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "false");
        response.setHeader("Cache-Control", "no-cache");
    }
}
