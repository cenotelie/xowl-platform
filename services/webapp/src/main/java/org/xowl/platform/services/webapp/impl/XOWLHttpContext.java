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
import org.xowl.platform.services.webapp.Activator;
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
        return defaultContext.handleSecurity(httpServletRequest, httpServletResponse);
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

    @Override
    public String getMimeType(String name) {
        return defaultContext.getMimeType(name);
    }
}
