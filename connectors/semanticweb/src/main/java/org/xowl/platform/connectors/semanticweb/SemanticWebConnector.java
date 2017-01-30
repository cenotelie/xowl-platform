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
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactBase;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.kernel.artifacts.ArtifactArchetypeFree;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.services.connection.ConnectorServiceBase;
import org.xowl.platform.services.connection.events.ConnectorReceivedDataEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Collection;

/**
 * Represents a connector for Semantic Web datasets
 *
 * @author Laurent Wouters
 */
public class SemanticWebConnector extends ConnectorServiceBase {
    /**
     * Initializes this connector
     *
     * @param identifier The identifier for this connector
     * @param name       The name for this connector
     * @param uris       The API URIs for this connector
     */
    public SemanticWebConnector(String identifier, String name, String[] uris) {
        super(identifier, name, uris);
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
        String[] names = request.getParameter("name");
        String[] bases = request.getParameter("base");
        String[] supersedes = request.getParameter("supersede");
        String[] versions = request.getParameter("version");
        String[] archetypes = request.getParameter("archetype");
        String contentType = request.getContentType();
        if (names == null || names.length <= 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
        if (bases == null || bases.length <= 0)
            bases = new String[]{null};
        if (versions == null || versions.length <= 0)
            versions = new String[]{null};
        if (archetypes == null || archetypes.length <= 0)
            archetypes = new String[]{ArtifactArchetypeFree.INSTANCE.getIdentifier()};
        if (contentType == null || contentType.isEmpty())
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_HEADER_CONTENT_TYPE), null);
        int index = contentType.indexOf(";");
        if (index != -1)
            contentType = contentType.substring(0, index);
        contentType = contentType.trim();

        String resource = ArtifactBase.newArtifactID(KernelSchema.GRAPH_ARTIFACTS);
        SemanticWebLoader loader = new SemanticWebLoader();
        XSPReply reply = loader.load(new InputStreamReader(new ByteArrayInputStream(request.getContent())), resource, contentType);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        Collection<Quad> quads = ((XSPReplyResultCollection<Quad>) reply).getData();
        Collection<Quad> metadata = ConnectorServiceBase.buildMetadata(resource, bases[0], supersedes, names[0], versions[0], archetypes[0], identifier);
        Artifact artifact = new ArtifactSimple(metadata, quads);
        queueInput(artifact);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConnectorReceivedDataEvent(this, artifact));
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, artifact.serializedJSON());
    }
}
