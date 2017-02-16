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

package org.xowl.platform.connectors.semanticweb;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactBase;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.services.connection.ConnectorServiceBase;
import org.xowl.platform.services.connection.ConnectorServiceData;
import org.xowl.platform.services.connection.events.ConnectorReceivedDataEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Represents a connector for Semantic Web datasets
 *
 * @author Laurent Wouters
 */
public class SemanticWebConnector extends ConnectorServiceBase {
    /**
     * Initializes this connector
     *
     * @param specification The specification for the new connector
     */
    public SemanticWebConnector(ConnectorServiceData specification) {
        super(specification);
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        switch (request.getMethod()) {
            case HttpConstants.METHOD_GET:
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, serializedJSON());
            case HttpConstants.METHOD_POST:
                return onMessagePostQuads(request);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, POST");
    }

    /**
     * Responds to the post of a new artifact
     *
     * @param request The request to handle
     * @return The response
     */
    private HttpResponse onMessagePostQuads(HttpApiRequest request) {
        String name = request.getParameter("name");
        String base = request.getParameter("base");
        String version = request.getParameter("version");
        String archetype = request.getParameter("archetype");
        String superseded = request.getParameter("superseded");
        String contentType = request.getContentType();
        if (name == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
        if (base == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'base'"), null);
        if (version == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'version'"), null);
        if (archetype == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'archetype'"), null);
        if (contentType == null || contentType.isEmpty())
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_HEADER_CONTENT_TYPE), null);
        int index = contentType.indexOf(";");
        if (index != -1)
            contentType = contentType.substring(0, index);
        contentType = contentType.trim();

        String resource = ArtifactBase.newArtifactID();
        SemanticWebLoader loader = new SemanticWebLoader();
        XSPReply reply = loader.load(new InputStreamReader(new ByteArrayInputStream(request.getContent())), resource, contentType);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        Collection<Quad> quads = ((XSPReplyResultCollection<Quad>) reply).getData();
        Artifact artifact = new ArtifactSimple(
                resource, name, base, version, archetype, identifier, DateFormat.getDateTimeInstance().format(new Date()), superseded,
                quads);
        queueInput(artifact);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConnectorReceivedDataEvent(this, artifact));
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, artifact.serializedJSON());
    }
}
