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

package org.xowl.platform.services.webapp.impl;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.services.webapp.ContributionDirectory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * Implements a default HTTP context
 *
 * @author Laurent Wouters
 */
public class XOWLHttpContext implements HttpContext {
    /**
     * The reference default context
     */
    private final HttpContext defaultContext;
    /**
     * The directory for the web contributions
     */
    private final ContributionDirectory directory;

    /**
     * Initialize this context
     *
     * @param httpService The HTTP service
     */
    public XOWLHttpContext(HttpService httpService, ContributionDirectory directory) {
        this.defaultContext = httpService.createDefaultHttpContext();
        this.directory = directory;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        //use this to insert the cache-control header
        addCORSHeader(httpServletRequest, httpServletResponse);
        httpServletResponse.addHeader(HttpConstants.HEADER_CACHE_CONTROL, "public, max-age=31536000, immutable");
        httpServletResponse.addHeader(HttpConstants.HEADER_STRICT_TRANSPORT_SECURITY, "max-age=31536000");
        httpServletResponse.addHeader(HttpConstants.HEADER_X_FRAME_OPTIONS, "deny");
        httpServletResponse.addHeader(HttpConstants.HEADER_X_XSS_PROTECTION, "1; mode=block");
        httpServletResponse.addHeader(HttpConstants.HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
        return true;
    }

    @Override
    public URL getResource(String name) {
        URL result = doGetResource(name);
        if (result != null)
            return result;
        return defaultContext.getResource(XOWLMainContribution.RESOURCES + "/404.html");
    }

    /**
     * Gets the resource URL for the requested name
     *
     * @param name The name of a resource
     * @return The corresponding URL, or null if there is none
     */
    private URL doGetResource(String name) {
        if (name.endsWith("/"))
            name += "index.html";
        return directory.resolveResource(name);
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

    @Override
    public String getMimeType(String name) {
        if (name.endsWith("/") || name.endsWith(".html"))
            return "text/html";
        if (name.endsWith(".css"))
            return "text/css";
        if (name.endsWith(".js"))
            return "application/javascript";
        if (name.endsWith(".txt"))
            return "text/plain";
        if (name.endsWith(".eot"))
            return "application/octet-stream";
        if (name.endsWith(".ttf"))
            return "application/octet-stream";
        if (name.endsWith(".woff"))
            return "application/font-woff";
        if (name.endsWith(".woff2"))
            return "application/font-woff";
        if (name.endsWith(".svg"))
            return "image/svg+xml";
        if (name.endsWith(".png"))
            return "image/png";
        if (name.endsWith(".gif"))
            return "image/gif";
        if (name.endsWith(".raml"))
            return HttpApiResource.MIME_RAML;
        if (name.endsWith(".json"))
            return HttpConstants.MIME_JSON;
        return defaultContext.getMimeType(name);
    }
}
