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
import org.xowl.infra.server.api.ApiV1;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyExpiredSession;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.security.SecurityService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

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
        // do not perform authentication for pre-flight requests
        if (httpServletRequest.getMethod().equals("OPTIONS"))
            return true;

        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null) {
            httpServletResponse.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
            return false;
        }

        Enumeration<String> values = httpServletRequest.getHeaders(HttpConstants.HEADER_COOKIE);
        if (values != null) {
            while (values.hasMoreElements()) {
                String content = values.nextElement();
                String[] parts = content.split(";");
                for (String cookie : parts) {
                    cookie = cookie.trim();
                    if (cookie.startsWith(ApiV1.AUTH_TOKEN + "=")) {
                        String token = cookie.substring(ApiV1.AUTH_TOKEN.length() + 1);
                        XSPReply reply = securityService.authenticate(httpServletRequest.getRemoteAddr(), token);
                        if (reply.isSuccess())
                            return true;
                        if (reply == XSPReplyExpiredSession.instance())
                            httpServletResponse.setStatus(HttpConstants.HTTP_SESSION_EXPIRED);
                        else
                            httpServletResponse.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                        return false;
                    }
                }
            }
        }
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
