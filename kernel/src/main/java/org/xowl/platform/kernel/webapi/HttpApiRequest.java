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

import java.util.Collection;

/**
 * Represents a request for the web API
 *
 * @author Laurent Wouters
 */
public interface HttpApiRequest {
    /**
     * Gets the remote client address
     *
     * @return The remote client address
     */
    String getClient();

    /**
     * Gets the used HTTP method
     *
     * @return The HTTP method
     */
    String getMethod();

    /**
     * Gets the URI for the request
     *
     * @return The URI for the request
     */
    String getUri();

    /**
     * Gets the parameters for the request
     *
     * @return The parameters for the request
     */
    Collection<String> getParameters();

    /**
     * Gets the values for the specified parameter
     *
     * @param name The name of a parameter
     * @return The associated values
     */
    String[] getParameter(String name);

    /**
     * Gets the headers for the request
     *
     * @return The headers for the request
     */
    Collection<String> getHeaders();

    /**
     * Gets the values for the specified header
     *
     * @param name The name of a header
     * @return The associated values
     */
    String[] getHeader(String name);

    /**
     * Gets the MIME type for the request's body
     *
     * @return The MIME type for the request's body
     */
    String getContentType();

    /**
     * Gets the request's body
     *
     * @return The request's body
     */
    byte[] getContent();
}
