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

package org.xowl.platform.kernel.webapi;

import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.api.ApiError;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.security.SecurityService;

/**
 * Interface for services that offer an HTTP API interface
 *
 * @author Laurent Wouters
 */
public interface HttpApiService extends Service, Serializable {
    /**
     * The response when this service cannot handle a request
     */
    int CANNOT_HANDLE = -1;

    /**
     * The lowest priority for HTTP services
     */
    int PRIORITY_LOWEST = 0;
    /**
     * A priority lower than normal for HTTP services
     */
    int PRIORITY_LOWER = 25;
    /**
     * The default priority for services
     */
    int PRIORITY_NORMAL = 50;
    /**
     * A priority higher than normal for HTTP services
     */
    int PRIORITY_HIGH = 75;
    /**
     * The highest priority for HTTP services
     */
    int PRIORITY_HIGHEST = 100;

    /**
     * API error - Failed to parse the content of the request.
     */
    ApiError ERROR_CONTENT_PARSING_FAILED = new ApiError(0x00000041,
            "Failed to parse the content of the request.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000041.html");
    /**
     * API error - Expected query parameters.
     */
    ApiError ERROR_EXPECTED_QUERY_PARAMETERS = new ApiError(0x00000042,
            "Expected query parameters.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000042.html");
    /**
     * API error - Failed to read the content of the request
     */
    ApiError ERROR_FAILED_TO_READ_CONTENT = new ApiError(0x00000043,
            "Failed to read the content of the request.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000043.html");
    /**
     * API error - A query parameter is not in the expected range
     */
    ApiError ERROR_PARAMETER_RANGE = new ApiError(0x00000044,
            "A query parameter is not in the expected range.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000044.html");
    /**
     * API error - Expected a Content-Type header
     */
    ApiError ERROR_EXPECTED_HEADER_CONTENT_TYPE = new ApiError(0x00000045,
            "Expected a Content-Type header.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000045.html");

    /**
     * Gets whether this server can handle the specified request, and if so with which priority
     *
     * @param request The request to handle
     * @return A negative number if this service cannot handle the request; otherwise a positive number indicating the priority of this service (greater number indicate greater priority)
     */
    int canHandle(HttpApiRequest request);

    /**
     * Checks whether the request to be handled requires the user to be authenticated
     *
     * @param request The request to handle
     * @return Whether the request requires the user to be authenticated
     */
    boolean requireAuth(HttpApiRequest request);

    /**
     * Handles an HTTP API request
     *
     * @param securityService The current security service
     * @param request         The request to handle
     * @return The HTTP response
     */
    HttpResponse handle(SecurityService securityService, HttpApiRequest request);

    /**
     * Gets the resource that contains the specification for this service
     *
     * @return The resource that contains the specification for this service
     */
    HttpApiResource getApiSpecification();

    /**
     * Gets the resource that contains the documentation for this service
     *
     * @return The resource that contains the documentation for this service
     */
    HttpApiResource getApiDocumentation();

    /**
     * Gets the other resources provided by this API service
     *
     * @return The other resources provided by this API service
     */
    HttpApiResource[] getApiOtherResources();
}
