/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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

package org.xowl.platform.services.httpapi.impl;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.xowl.platform.kernel.Security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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
    public boolean handleSecurity(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        String headerAuth = httpServletRequest.getHeader("Authorization");
        if (headerAuth == null) {
            httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=\"" + Security.getRealm() + "\"");
            httpServletResponse.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
            return false;
        }
        int index = headerAuth.indexOf(32);
        if (index != -1 && headerAuth.substring(0, index).equals("Basic")) {
            byte[] buffer = Base64.getDecoder().decode(headerAuth.substring(index + 1));
            String authToken = new String(buffer);
            int indexColon = authToken.indexOf(58);
            String login = authToken.substring(0, indexColon);
            String password = authToken.substring(indexColon + 1);
            if (Security.login(httpServletRequest.getRemoteAddr(), login, password.toCharArray())) {
                httpServletRequest.setAttribute(AUTHENTICATION_TYPE, "Basic");
                httpServletRequest.setAttribute(REMOTE_USER, login);
                return true;
            }
        }
        httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=\"" + Security.getRealm() + "\"");
        httpServletResponse.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
        return false;
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
