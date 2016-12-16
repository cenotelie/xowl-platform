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

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * The main HTTP context
 *
 * @author Laurent Wouters
 */
public class XOWLMainHTTPContext implements HttpContext {
    /**
     * The delegate context for other than security
     */
    private final HttpContext delegate;

    /**
     * Initializes this context
     *
     * @param httpService The parent HTTP service
     */
    public XOWLMainHTTPContext(HttpService httpService) {
        this.delegate = httpService.createDefaultHttpContext();
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return true;
    }

    @Override
    public URL getResource(String resource) {
        return delegate.getResource(resource);
    }

    @Override
    public String getMimeType(String type) {
        return delegate.getMimeType(type);
    }
}
