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
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.webapp.Activator;
import org.xowl.platform.services.webapp.BrandingService;
import org.xowl.platform.services.webapp.WebModuleDirectory;
import org.xowl.platform.services.webapp.WebModuleService;

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
     * The current branding service
     */
    private BrandingService brandingService;
    /**
     * The directory of web module services
     */
    private final WebModuleDirectory moduleDirectory;

    /**
     * Initialize this context
     *
     * @param httpService The HTTP service
     */
    public XOWLHttpContext(HttpService httpService, WebModuleDirectory moduleDirectory) {
        this.defaultContext = httpService.createDefaultHttpContext();
        this.moduleDirectory = moduleDirectory;
    }

    /**
     * Gets the branding service
     *
     * @return The branding service
     */
    private BrandingService getBrandingService() {
        if (brandingService == null) {
            brandingService = ServiceUtils.getService(BrandingService.class);
            if (brandingService == null)
                brandingService = new XOWLBrandingService();
        }
        return brandingService;
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
        return defaultContext.getResource(Activator.WEBAPP_RESOURCE_ROOT + "/404.html");
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
        if (name.startsWith(Activator.WEBAPP_RESOURCE_ROOT + BrandingService.BRANDING)) {
            String localName = name.substring(Activator.WEBAPP_RESOURCE_ROOT.length() + BrandingService.BRANDING.length());
            return getBrandingService().getResource(localName);
        } else if (name.startsWith(Activator.WEBAPP_RESOURCE_ROOT + WebModuleService.MODULES)) {
            String rest = name.substring(Activator.WEBAPP_RESOURCE_ROOT.length() + WebModuleService.MODULES.length());
            int index = rest.indexOf("/");
            if (index != -1) {
                String moduleName = rest.substring(0, index);
                return serveModule(moduleName, rest.substring(index + 1));
            }
        }
        return defaultContext.getResource(name);
    }

    /**
     * Serves a resource for a module
     *
     * @param moduleURI The URI part of the module
     * @param resource  The module's resource
     * @return The URL of the requested resource
     */
    private URL serveModule(String moduleURI, String resource) {
        WebModuleService service = moduleDirectory.getServiceFor(moduleURI);
        if (service == null)
            return null;
        return service.getResource(resource);
    }

    @Override
    public String getMimeType(String name) {
        return defaultContext.getMimeType(name);
    }
}
