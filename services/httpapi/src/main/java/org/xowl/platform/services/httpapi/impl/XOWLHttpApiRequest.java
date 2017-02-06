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

import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.webapi.HttpApiRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Encapsulates a servlet request as an API request
 *
 * @author Laurent Wouters
 */
class XOWLHttpApiRequest implements HttpApiRequest {
    /**
     * The original servlet request
     */
    private final HttpServletRequest request;
    /**
     * The parameters for the request
     */
    private Collection<String> parameters;
    /**
     * The headers for the request
     */
    private Collection<String> headers;
    /**
     * The request's body
     */
    private byte[] content;

    /**
     * Initializes this request
     *
     * @param request The original servlet request
     */
    public XOWLHttpApiRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getClient() {
        return request.getRemoteAddr();
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getUri() {
        return request.getRequestURI();
    }

    @Override
    public Collection<String> getParameters() {
        if (parameters != null)
            return parameters;
        parameters = new ArrayList<>();
        Enumeration<String> values = request.getParameterNames();
        while (values.hasMoreElements())
            parameters.add(values.nextElement());
        parameters = Collections.unmodifiableCollection(parameters);
        return parameters;
    }

    @Override
    public String[] getParameter(String name) {
        return request.getParameterValues(name);
    }

    @Override
    public Collection<String> getHeaders() {
        if (headers != null)
            return headers;
        headers = new ArrayList<>();
        Enumeration<String> values = request.getParameterNames();
        while (values.hasMoreElements())
            headers.add(values.nextElement());
        headers = Collections.unmodifiableCollection(headers);
        return headers;
    }

    @Override
    public String[] getHeader(String name) {
        ArrayList<String> result = new ArrayList<>();
        Enumeration<String> values = request.getHeaders(name);
        while (values.hasMoreElements())
            result.add(values.nextElement());
        return result.toArray(new String[result.size()]);
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public byte[] getContent() {
        if (content != null)
            return content;
        if (request.getContentLength() > 0) {
            try (InputStream is = request.getInputStream()) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int read = is.read(buffer);
                while (read > 0) {
                    output.write(buffer, 0, read);
                    read = is.read(buffer);
                }
                content = output.toByteArray();
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
            }
        }
        return content;
    }
}
