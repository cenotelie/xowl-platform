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
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactBase;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.kernel.artifacts.FreeArtifactArchetype;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.services.connection.ConnectorServiceBase;
import org.xowl.platform.services.connection.events.ConnectorReceivedDataEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a connector for Semantic Web datasets
 *
 * @author Laurent Wouters
 */
public class SemanticWebConnector extends ConnectorServiceBase {
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
     * The node manager when loading quads
     */
    private final NodeManager nodeManager;

    /**
     * Initializes this connector
     *
     * @param identifier The identifier for this connector
     * @param name       The name for this connector
     * @param uris       The API URIs for this connector
     */
    public SemanticWebConnector(String identifier, String name, String[] uris) {
        this.identifier = identifier;
        this.name = name;
        this.uris = uris;
        this.nodeManager = new CachedNodes();
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
        if (method.equals("GET"))
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, serializedJSON());
        if (method.equals("POST"))
            return onMessagePostQuads(parameters, contentType, content);
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected GET or POST request");
    }

    /**
     * Responds to the post of a new artifact
     *
     * @param parameters  The request parameters
     * @param contentType The content type
     * @param content     The content
     * @return The response
     */
    private HttpResponse onMessagePostQuads(Map<String, String[]> parameters, String contentType, byte[] content) {
        String[] names = parameters.get("name");
        String[] bases = parameters.get("base");
        String[] supersedes = parameters.get("supersede");
        String[] versions = parameters.get("version");
        String[] archetypes = parameters.get("archetype");
        if (names == null || names.length <= 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected name parameter");
        if (bases == null || bases.length <= 0)
            bases = new String[]{null};
        if (versions == null || versions.length <= 0)
            versions = new String[]{null};
        if (archetypes == null || archetypes.length <= 0)
            archetypes = new String[]{FreeArtifactArchetype.INSTANCE.getIdentifier()};
        if (contentType == null || contentType.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected content type");
        int index = contentType.indexOf(";");
        if (index != -1)
            contentType = contentType.substring(0, index);
        contentType = contentType.trim();

        String resource = ArtifactBase.newArtifactID(KernelSchema.GRAPH_ARTIFACTS);
        SemanticWebLoader loader = new SemanticWebLoader();
        XSPReply reply = loader.load(new InputStreamReader(new ByteArrayInputStream(content)), resource, contentType);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        Collection<Quad> quads = ((XSPReplyResultCollection<Quad>) reply).getData();
        Collection<Quad> metadata = ConnectorServiceBase.buildMetadata(resource, bases[0], supersedes, names[0], versions[0], archetypes[0], identifier);
        Artifact artifact = new ArtifactSimple(metadata, quads);
        queueInput(artifact);
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConnectorReceivedDataEvent(this, artifact));
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, artifact.serializedJSON());
    }
}
