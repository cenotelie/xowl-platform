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

package org.xowl.platform.services.domain.impl;

import org.xowl.platform.kernel.ServiceHttpServed;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.domain.DomainConnectorService;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.platform.utils.Utils;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

/**
 * Implements a directory service for the domain connectors
 *
 * @author Laurent Wouters
 */
public class DomainDirectoryService implements ServiceHttpServed {
    @Override
    public String getIdentifier() {
        return DomainDirectoryService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Domain Directory Service";
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
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        Collection<DomainConnectorService> connectors = ServiceUtils.getServices(DomainConnectorService.class);
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (DomainConnectorService connector : connectors) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(connector.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, Utils.MIME_JSON, builder.toString().getBytes(Charset.forName("UTF-8")));
    }
}
