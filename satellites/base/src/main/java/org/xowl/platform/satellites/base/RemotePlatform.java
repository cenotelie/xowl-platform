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

package org.xowl.platform.satellites.base;

import org.xowl.hime.redist.ASTNode;
import org.xowl.hime.redist.ParseResult;
import org.xowl.infra.store.http.HttpConnection;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.Logger;

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
        HttpResponse response = connection.request("connectors", "GET", null, null, HttpConstants.MIME_JSON);
        if (response == null)
            return result;
        if (response.getCode() != HttpURLConnection.HTTP_OK)
            return result;
        JSONLDLoader loader = new JSONLDLoader(new CachedNodes()) {
            @Override
            protected Reader getReaderFor(Logger logger, String iri) {
                return null;
            }
        };
        BufferedLogger logger = new BufferedLogger();
        ParseResult parseResult = loader.parse(logger, new StringReader(response.getBodyAsString()));
        if (parseResult == null || !parseResult.isSuccess() || !parseResult.getErrors().isEmpty() || !logger.getErrorMessages().isEmpty())
            return result;
        for (ASTNode element : parseResult.getRoot().getChildren()) {
            result.add(new RemoteConnector(element));
        }
        return result;
    }

    /**
     * Retrieves the info of a job on the platform
     *
     * @param identifier The job's identifier
     * @return The job, or null if it cannot be found
     */
    public RemoteJob getJob(String identifier) {
        HttpResponse response = connection.request("connectors", "GET", null, null, HttpConstants.MIME_JSON);
        if (response == null)
            return null;
        if (response.getCode() != HttpURLConnection.HTTP_OK)
            return null;
        JSONLDLoader loader = new JSONLDLoader(new CachedNodes()) {
            @Override
            protected Reader getReaderFor(Logger logger, String iri) {
                return null;
            }
        };
        BufferedLogger logger = new BufferedLogger();
        ParseResult parseResult = loader.parse(logger, new StringReader(response.getBodyAsString()));
        if (parseResult == null || !parseResult.isSuccess() || !parseResult.getErrors().isEmpty() || !logger.getErrorMessages().isEmpty())
            return null;
        return new RemoteJob(parseResult.getRoot(), factory);
    }
}
