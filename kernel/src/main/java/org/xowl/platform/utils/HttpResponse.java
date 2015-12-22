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

package org.xowl.platform.utils;

/**
 * The response to an HTTP request
 *
 * @author Laurent Wouters
 */
public class HttpResponse {
    /**
     * The response code
     */
    public final int code;
    /**
     * The response content type, if any
     */
    public final String contentType;
    /**
     * The response content, if any
     */
    public final byte[] content;

    /**
     * Initializes this response
     *
     * @param code The response code
     */
    public HttpResponse(int code) {
        this.code = code;
        this.contentType = null;
        this.content = null;
    }

    /**
     * Initializes this response
     *
     * @param code        The response code
     * @param contentType The response content type, if any
     * @param content     The response content, if any
     */
    public HttpResponse(int code, String contentType, byte[] content) {
        this.code = code;
        this.contentType = contentType;
        this.content = content;
    }
}
