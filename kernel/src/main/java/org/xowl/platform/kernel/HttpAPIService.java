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

package org.xowl.platform.kernel;

import org.xowl.platform.utils.HttpResponse;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for services that offer an HTTP API interface
 *
 * @author Laurent Wouters
 */
public interface HttpAPIService extends Service {
    /**
     * The URI prefix for API connections
     */
    String URI_API = "/api";

    /**
     * Gets the URIs for accessing the service
     *
     * @return The URIs for accessing the service
     */
    Collection<String> getURIs();

    /**
     * Responds to a message
     *
     * @param method      The HTTP method
     * @param uri         The requested URI
     * @param parameters  The request parameters
     * @param contentType The content type, if any
     * @param content     The content, if any
     * @param accept      The Accept header, if any
     * @return The response
     */
    HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept);
}
