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

package org.xowl.platform.services.server.impl;

import org.xowl.platform.services.server.ServerService;
import org.xowl.store.sparql.Result;
import org.xowl.utils.Files;
import org.xowl.utils.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;

/**
 * The main server for the platform
 * HTTP API outline:
 * GET:  /connectors            Get the list of the connectors
 * GET:  /artifacts             Get the list of the maintained artifacts
 * GET:  /workflow              Get the definition of the workflow
 * POST: /workflow?action=xx    Triggers the specified workflow activity
 * POST: /sparql                Executes a SPARQL command
 * POST: /connector?id=xx       Posts a message to a connector identified by the provided ID
 *
 * @author Laurent Wouters
 */
public class XOWLMainServer extends HttpServlet implements ServerService {
    /**
     * The JSON MIME type
     */
    private static final String MIME_TYPE_JSON = "application/json";

    @Override
    public String getIdentifier() {
        return XOWLMainServer.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Main Server";
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
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCORSPreflight(response);
    }

    /**
     * Responds to a CORS pre-flight request
     *
     * @param response The HTTP response
     */
    protected void doCORSPreflight(HttpServletResponse response) {
        addCORSHeader(response);
        response.setStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * Adds the headers required in a response for the support of Cross-Origin Resource Sharing
     *
     * @param response The response to add headers to
     */
    protected void addCORSHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type, Cache-Control");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "false");
        response.setHeader("Cache-Control", "no-cache");
    }
}
