/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General
 * Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors:
 * Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.kernel.impl;

import org.apache.shiro.SecurityUtils;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.platform.kernel.HttpAPIService;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Implements Http API for handling the security
 *
 * @author Laurent Wouters
 */
public class XOWLHttpSecurityService implements HttpAPIService {
    /**
     * The URIs for this service
     */
    private static final String[] URIS = new String[]{
            "security"
    };

    @Override
    public String getIdentifier() {
        return FSConfigurationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - HTTP Security Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIS);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        Object principal = SecurityUtils.getSubject().getPrincipal();
        if (principal == null)
            return new HttpResponse(HttpURLConnection.HTTP_FORBIDDEN);
        return new HttpResponse(HttpURLConnection.HTTP_OK, principal.toString(), HttpConstants.MIME_TEXT_PLAIN);
    }
}
