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

import fr.cenotelie.commons.utils.http.HttpConstants;
import fr.cenotelie.commons.utils.http.URIUtils;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ui.WebUIContribution;
import org.xowl.platform.kernel.webapi.HttpApiResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Implements a default HTTP context
 *
 * @author Laurent Wouters
 */
public class XOWLHttpContext implements HttpContext {
    /**
     * The comparator for contributions
     */
    private static class ContributionComparator implements Comparator<WebUIContribution> {
        @Override
        public int compare(WebUIContribution c1, WebUIContribution c2) {
            return Integer.compare(c2.getPriority(), c1.getPriority());
        }
    }

    /**
     * The comparator instance of contributions
     */
    private static final ContributionComparator COMPARATOR = new ContributionComparator();

    /**
     * Represents a tree of contributions
     */
    private static class Folder {
        /**
         * The sub trees
         */
        public final Map<String, Folder> subs;
        /**
         * The contributions at this stage
         */
        public final List<WebUIContribution> contributions;

        /**
         * Initializes this folder
         */
        public Folder() {
            this.subs = new HashMap<>();
            this.contributions = new ArrayList<>();
        }

        /**
         * Prepares this folder
         */
        public void prepare() {
            Collections.sort(contributions, COMPARATOR);
            for (Folder child : subs.values())
                child.prepare();
        }
    }

    /**
     * The reference default context
     */
    private final HttpContext defaultContext;
    /**
     * The root of the tree of contributions
     */
    private final Folder root;
    /**
     * The registered contributions
     */
    private Collection<WebUIContribution> contributions;

    /**
     * Initialize this context
     *
     * @param httpService The HTTP service
     */
    public XOWLHttpContext(HttpService httpService) {
        this.defaultContext = httpService.createDefaultHttpContext();
        this.root = new Folder();
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
     * Gets the resource URL for the requested uri
     *
     * @param uri The uri of a resource
     * @return The corresponding URL, or null if there is none
     */
    private URL doGetResource(String uri) {
        if (uri.endsWith("/"))
            uri += "index.html";
        List<String> segments = URIUtils.getSegments(uri);
        if (contributions == null) {
            synchronized (this) {
                if (contributions == null) {
                    contributions = Register.getComponents(WebUIContribution.class);
                    for (WebUIContribution contribution : contributions)
                        register(contribution);
                    root.prepare();
                }
            }
        }
        return doGetResource(root, segments, 0, uri);
    }

    /**
     * Resolves a resource
     *
     * @param folder   The current folder
     * @param segments The segments of the requested URI
     * @param index    The index of the next segment
     * @param uri      The requested URI
     * @return The resolved URL, or null of there is none
     */
    private URL doGetResource(Folder folder, List<String> segments, int index, String uri) {
        if (index < segments.size()) {
            String segment = segments.get(index);
            Folder child = folder.subs.get(segment);
            if (child != null) {
                URL result = doGetResource(child, segments, index + 1, uri);
                if (result != null)
                    return result;
            }
        }
        for (int i = 0; i != folder.contributions.size(); i++) {
            WebUIContribution contribution = folder.contributions.get(i);
            URL result = contribution.getResource(uri);
            if (result != null)
                return result;
        }
        return null;
    }

    /**
     * Registers a web contribution
     *
     * @param contribution The web contribution to register
     */
    private void register(WebUIContribution contribution) {
        List<String> segments = URIUtils.getSegments(contribution.getPrefix());
        Folder current = root;
        for (int i = 0; i != segments.size(); i++) {
            String name = segments.get(i);
            if (name.isEmpty() && i == segments.size() - 1)
                // last segment is empty, break here
                break;
            Folder child = current.subs.get(name);
            if (child == null) {
                child = new Folder();
                current.subs.put(name, child);
            }
            current = child;
        }
        current.contributions.add(contribution);
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
