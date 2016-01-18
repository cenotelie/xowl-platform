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

package org.xowl.platform.services.webapp;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implements a default HTTP context
 *
 * @author Laurent Wouters
 */
class HttpDefaultContext implements HttpContext {
    /**
     * The reference default context
     */
    private final HttpContext defaultContext;

    /**
     * Initialize this context
     *
     * @param httpService The HTTP servoce
     */
    public HttpDefaultContext(HttpService httpService) {
        this.defaultContext = httpService.createDefaultHttpContext();
    }

    @Override
    public boolean handleSecurity(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        return defaultContext.handleSecurity(httpServletRequest, httpServletResponse);
    }

    @Override
    public URL getResource(String name) {
        URL result = defaultContext.getResource(name);
        if (name.endsWith("/")) {
            try {
                result = new URL(result, "index.html");
            } catch (MalformedURLException exception) {
                // ignore this;
            }
        }
        return result;
    }

    @Override
    public String getMimeType(String name) {
        return defaultContext.getMimeType(name);
    }
}