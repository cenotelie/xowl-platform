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

package org.xowl.platform.kernel;

import org.xowl.infra.utils.http.HttpResponse;

/**
 * Interface for services that offer an HTTP API interface
 *
 * @author Laurent Wouters
 */
public interface HttpApiService extends Service {
    /**
     * The URI prefix for API connections
     */
    String URI_API = "/api";

    /**
     * The response when this service cannot handle a request
     */
    int CANNOT_HANDLE = -1;

    /**
     * Gets whether this server can handle the specified request, and if so with which priority
     *
     * @param request The request to handle
     * @return A negative number if this service cannot handle the request; otherwise a positive number indicating the priority of this service (greater number indicate greater priority)
     */
    int canHandle(HttpApiRequest request);

    /**
     * Handles an HTTP API request
     *
     * @param request The request to handle
     * @return The HTTP response
     */
    HttpResponse handle(HttpApiRequest request);
}
