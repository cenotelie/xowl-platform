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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI();
        switch (path) {
            case "/connectors":
                doListConnectors(response);
                break;
            case "/artifacts":
                doListArtifacts(response);
                break;
            case "/workflow":
                doGetWorkflow(response);
                break;
            default:
                addCORSHeader(response);
                response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCORSPreflight(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI();
        switch (path) {
            case "/workflow":
                doPostWorkflowAction(request, response);
                break;
            case "/connector":
                doPostConnectorMessage(request, response);
                break;
            case "/sparql":
                doPostSPARQL(request, response);
                break;
            default:
                addCORSHeader(response);
                response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                break;
        }
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
     * Responds with a list of the connectors
     *
     * @param response The HTTP response
     */
    protected void doListConnectors(HttpServletResponse response) {
        response.setStatus(HttpURLConnection.HTTP_OK);
        response.setHeader("Content-Type", MIME_TYPE_JSON);
        addCORSHeader(response);
        try (PrintWriter out = response.getWriter()) {
            out.write("[");
            boolean first = true;
            for (DomainConnectorService connector : Activator.getConnectors()) {
                if (!first)
                    out.write(", ");
                first = false;
                out.write(connector.serializedJSON());
            }
            out.write("]");
            out.flush();
        } catch (IOException exception) {
            Logger.DEFAULT.error(exception);
        }
    }

    /**
     * Responds with a list of the artifacts
     *
     * @param response The HTTP response
     */
    protected void doListArtifacts(HttpServletResponse response) {
        response.setStatus(HttpURLConnection.HTTP_OK);
        response.setHeader("Content-Type", MIME_TYPE_JSON);
        addCORSHeader(response);
        try (PrintWriter out = response.getWriter()) {
            out.write("[");
            TripleStoreService lts = Utils.getService(TripleStoreService.class);
            if (lts != null) {
                Collection<Artifact> artifacts = lts.listArtifacts();
                boolean first = true;
                for (Artifact artifact : artifacts) {
                    if (!first)
                        out.write(", ");
                    first = false;
                    out.write(artifact.serializedJSON());
                }
            }
            out.write("]");
            out.flush();
        } catch (IOException exception) {
            Logger.DEFAULT.error(exception);
        }
    }

    /**
     * Responds with the definition of the workflow
     *
     * @param response The HTTP response
     */
    protected void doGetWorkflow(HttpServletResponse response) {
        response.setStatus(HttpURLConnection.HTTP_OK);
        response.setHeader("Content-Type", MIME_TYPE_JSON);
        addCORSHeader(response);
        BundleContext context = Activator.getContext();
        ServiceReference reference = context.getServiceReference(WorkflowService.class);
        if (reference != null) {
            WorkflowService workflowService = (WorkflowService) context.getService(reference);
            try (PrintWriter out = response.getWriter()) {
                out.print(workflowService.getCurrentWorkflow().serializedJSON());
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
        }
    }

    /**
     * Responds to a request for a workflow action
     *
     * @param request  The HTTP request
     * @param response The HTTP response
     */
    protected void doPostWorkflowAction(HttpServletRequest request, HttpServletResponse response) {
        addCORSHeader(response);
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("Expected action identifier in parameter named 'action'.");
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
            return;
        }
        WorkflowService workflowService = Utils.getService(WorkflowService.class);
        if (workflowService == null) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("Cannot resolve the workflow service.");
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
            return;
        }

        WorkflowActionReply reply = workflowService.execute(action, null);
        if (reply.isSuccess()) {
            response.setStatus(HttpURLConnection.HTTP_OK);
        } else {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        try (PrintWriter out = response.getWriter()) {
            out.print(reply.serializedJSON());
        } catch (IOException exception) {
            Logger.DEFAULT.error(exception);
        }
    }

    /**
     * Handles the routing a message to a connector
     *
     * @param request  The HTTP request
     * @param response The HTTP response
     */
    protected void doPostConnectorMessage(HttpServletRequest request, HttpServletResponse response) {
        addCORSHeader(response);
        String connectorId = request.getParameter("id");
        if (connectorId == null || connectorId.isEmpty()) {
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("Expected connector identifier in parameter named 'id'.");
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
            return;
        }
        DomainConnectorService connector = Utils.getService(DomainConnectorService.class, connectorId);
        if (connector == null) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("Cannot find connector " + connectorId);
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
            return;
        }

        String contentType = request.getContentType();
        String content = null;
        try (Reader reader = request.getReader()) {
            content = Files.read(reader);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        boolean success = connector.onMessage(content, contentType);
        response.setStatus(success ? HttpURLConnection.HTTP_OK : HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Responds to a SPARQL query
     *
     * @param request  The HTTP request
     * @param response The HTTP response
     */
    protected void doPostSPARQL(HttpServletRequest request, HttpServletResponse response) {
        addCORSHeader(response);
        TripleStoreService lts = Utils.getService(TripleStoreService.class);
        if (lts == null) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("Cannot find the triple store service.");
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
            return;
        }
        String content = null;
        try (Reader reader = request.getReader()) {
            content = Files.read(reader);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        Result result = lts.sparql(content);
        response.setStatus(result.isSuccess() ? HttpURLConnection.HTTP_OK : HttpURLConnection.HTTP_BAD_REQUEST);
        try (PrintWriter out = response.getWriter()) {
            result.print(out, Result.SYNTAX_JSON);
        } catch (IOException exception) {
            Logger.DEFAULT.error(exception);
        }
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
