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

package org.xowl.platform.services.webapp.impl;

import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.utils.logging.Logger;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.services.webapp.WebModuleDirectory;
import org.xowl.platform.services.webapp.WebModuleService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a module directory
 *
 * @author Laurent Wouters
 */
public class XOWLModuleDirectory extends HttpServlet implements WebModuleDirectory {
    /**
     * The directory
     */
    private final Map<String, WebModuleService> directory;

    public XOWLModuleDirectory() {
        this.directory = new HashMap<>();
    }

    /**
     * Registers a module
     *
     * @param service The module to register
     */
    public void register(WebModuleService service) {
        directory.put(service.getURI(), service);
    }

    /**
     * Unregisters a module
     *
     * @param service the module to unregister
     */
    public void unregister(WebModuleService service) {
        directory.remove(service.getURI());
    }

    @Override
    public WebModuleService getServiceFor(String uri) {
        return directory.get(uri);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        addCORSHeader(response);
        response.setStatus(HttpURLConnection.HTTP_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean first = true;
        for (WebModuleService service : directory.values()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(service.serializedJSON());
        }
        builder.append("]");
        byte[] content = builder.toString().getBytes(PlatformUtils.DEFAULT_CHARSET);

        addCORSHeader(response);
        response.setStatus(HttpURLConnection.HTTP_OK);
        response.setContentType(HttpConstants.MIME_JSON);
        response.setContentLength(content.length);
        try (OutputStream os = response.getOutputStream()) {
            os.write(content);
            os.flush();
        } catch (IOException exception) {
            Logger.DEFAULT.error(exception);
        }
    }

    /**
     * Adds the headers required in a response for the support of Cross-Origin Resource Sharing
     *
     * @param response The response to add headers to
     */
    private void addCORSHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type, Cache-Control");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "false");
        response.setHeader("Cache-Control", "no-cache");
    }
}
