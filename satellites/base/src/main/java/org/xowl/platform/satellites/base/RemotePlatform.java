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

package org.xowl.platform.satellites.base;

import org.xowl.hime.redist.ASTNode;
import org.xowl.hime.redist.ParseError;
import org.xowl.hime.redist.ParseResult;
import org.xowl.infra.store.URIUtils;
import org.xowl.infra.store.http.HttpConnection;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.logging.Logger;
import org.xowl.infra.utils.logging.Logging;

import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The base API for accessing a remote platform
 *
 * @author Laurent Wouters
 */
public class RemotePlatform {
    /**
     * The connection to the platform
     */
    private final HttpConnection connection;
    /**
     * The factory of remote objects
     */
    private final RemoteFactory factory;

    /**
     * Initializes this platform connection
     *
     * @param endpoint The API endpoint (https://something:port/api/)
     * @param login    The login for connecting to the platform
     * @param password The password for connecting to the platform
     */
    public RemotePlatform(String endpoint, String login, String password) {
        this.connection = new HttpConnection(endpoint, login, password);
        this.factory = new RemoteFactory();
    }

    /**
     * Gets the connectors on the current platform
     *
     * @return The connectors
     */
    public Collection<RemoteConnector> getConnectors() {
        Collection<RemoteConnector> result = new ArrayList<>();
        HttpResponse response = connection.request("services/admin/connectors", "GET", "", null, HttpConstants.MIME_JSON);
        ASTNode root = parseJson(response);
        if (root == null)
            return result;
        for (ASTNode element : root.getChildren()) {
            result.add(new RemoteConnector(element));
        }
        return result;
    }

    /**
     * Pulls an artifact from a connector
     *
     * @param connectorId The connector to pull from
     * @return The triggered job, or null if an error occurred
     */
    public RemoteJob pullFromConnector(String connectorId) {
        HttpResponse response = connection.request("services/admin/connectors?action=pull&id=" + URIUtils.encodeComponent(connectorId), "POST", "", null, HttpConstants.MIME_JSON);
        ASTNode root = parseJson(response);
        if (root == null)
            return null;
        return new RemoteJob(root, factory);
    }

    /**
     * Pushes an artifact to a client through a connector
     *
     * @param connectorId The connector to push through
     * @param artifactId  The artifact to push
     * @return The triggered job, or null if an error occurred
     */
    public RemoteJob pushFromConnector(String connectorId, String artifactId) {
        HttpResponse response = connection.request("services/admin/connectors?action=push&id=" + URIUtils.encodeComponent(connectorId) + "&artifact=" + URIUtils.encodeComponent(artifactId), "POST", null, null, HttpConstants.MIME_JSON);
        ASTNode root = parseJson(response);
        if (root == null)
            return null;
        return new RemoteJob(root, factory);
    }

    /**
     * Retrieves the info of a job on the platform
     *
     * @param identifier The job's identifier
     * @return The job, or null if it cannot be found
     */
    public RemoteJob getJob(String identifier) {
        HttpResponse response = connection.request("services/admin/connectors", "GET", "", null, HttpConstants.MIME_JSON);
        ASTNode root = parseJson(response);
        if (root == null)
            return null;
        return new RemoteJob(root, factory);
    }

    /**
     * Wait for specified job to terminate
     *
     * @param job The job to wait for
     * @return The job, or null if an error occurred
     */
    public RemoteJob waitFor(RemoteJob job) {
        while (RemoteJob.STATUS_SCHEDULED.equals(job.getStatus()) || RemoteJob.STATUS_RUNNING.equals(job.getStatus())) {
            HttpResponse response = connection.request("services/admin/jobs?id=" + URIUtils.encodeComponent(job.getIdentifier()), "GET", "", null, HttpConstants.MIME_JSON);
            ASTNode root = parseJson(response);
            if (root == null)
                return null;
            job.update(root, factory);
        }
        return job;
    }

    /**
     * Parses a JSON response
     *
     * @param response The response
     * @return The AST root, or null if an error occurred
     */
    public static ASTNode parseJson(HttpResponse response) {
        if (response == null)
            return null;
        if (response.getCode() != HttpURLConnection.HTTP_OK)
            return null;
        return parseJson(Logging.getDefault(), response.getBodyAsString());
    }

    /**
     * Parses the JSON content
     *
     * @param logger  The logger to use
     * @param content The content to parse
     * @return The AST root node, or null of the parsing failed
     */
    public static ASTNode parseJson(Logger logger, String content) {
        JSONLDLoader loader = new JSONLDLoader(null) {
            @Override
            protected Reader getReaderFor(Logger logger, String iri) {
                return null;
            }
        };
        ParseResult result = loader.parse(logger, new StringReader(content));
        if (result == null)
            return null;
        if (!result.getErrors().isEmpty()) {
            for (ParseError error : result.getErrors())
                logger.error(error);
            return null;
        }
        return result.getRoot();
    }
}
