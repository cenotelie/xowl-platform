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

package org.xowl.platform.connectors.csv;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.services.connection.ConnectorServiceBase;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a CSV connector
 *
 * @author Laurent Wouters
 */
public class CSVConnector extends ConnectorServiceBase {
    /**
     * The identifier for this connector
     */
    private final String identifier;
    /**
     * The name for this connector
     */
    private final String name;
    /**
     * The API URIs for this connector
     */
    private final String[] uris;

    /**
     * Initializes this connector
     *
     * @param identifier The identifier for this connector
     * @param name       The name for this connector
     * @param uris       The API URIs for this connector
     */
    public CSVConnector(String identifier, String name, String[] uris) {
        this.identifier = identifier;
        this.name = name;
        this.uris = uris;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public XSPReply pushToClient(Artifact data) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(uris);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD);
    }
}
