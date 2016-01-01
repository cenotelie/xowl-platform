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

package org.xowl.platform.services.domain.impl;

import org.xowl.platform.kernel.Artifact;
import org.xowl.platform.kernel.ArtifactBase;
import org.xowl.platform.kernel.ArtifactSimple;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.services.domain.BaseDomainConnector;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.platform.utils.Utils;
import org.xowl.store.AbstractRepository;
import org.xowl.store.IOUtils;
import org.xowl.store.Vocabulary;
import org.xowl.store.loaders.*;
import org.xowl.store.rdf.IRINode;
import org.xowl.store.rdf.Quad;
import org.xowl.store.storage.NodeManager;
import org.xowl.store.storage.cache.CachedNodes;
import org.xowl.utils.logging.BufferedLogger;
import org.xowl.utils.logging.Logger;

import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a domain connector that can be configured and deployed at runtime
 *
 * @author Laurent Wouters
 */
class ParametricDomainConnector extends BaseDomainConnector {
    /**
     * The identifier for this connector
     */
    private final String identifier;
    /**
     * The name of this connector
     */
    private final String name;
    /**
     * The URI to access this connector
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
     * @param name       The name of this connector
     * @param uris       The URIs to access this connector
     */
    public ParametricDomainConnector(String identifier, String name, String[] uris) {
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
    public boolean pushToClient(Artifact data) {
        // cannot push to clients
        return false;
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (method.equals("GET"))
            return new HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, serializedJSON());
        if (method.equals("POST"))
            return onMessagePostQuads(parameters, contentType, content);
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Expected GET or POST request");
    }

    @Override
    public String[] getURIs() {
        return uris;
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
        Loader loader = null;
        switch (contentType) {
            case AbstractRepository.SYNTAX_NTRIPLES:
                loader = new NTriplesLoader(nodeManager);
                break;
            case AbstractRepository.SYNTAX_NQUADS:
                loader = new NQuadsLoader(nodeManager);
                break;
            case AbstractRepository.SYNTAX_TURTLE:
                loader = new TurtleLoader(nodeManager);
                break;
            case AbstractRepository.SYNTAX_TRIG:
                loader = new TriGLoader(nodeManager);
                break;
            case AbstractRepository.SYNTAX_RDFXML:
                loader = new RDFXMLLoader(nodeManager);
                break;
            case AbstractRepository.SYNTAX_JSON_LD:
                loader = new JSONLDLoader(nodeManager) {
                    @Override
                    protected Reader getReaderFor(Logger logger, String iri) {
                        return null;
                    }
                };
                break;
        }
        if (loader == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Unsupported content type: " + contentType);
        BufferedLogger logger = new BufferedLogger();
        String contentString = new String(content, Utils.DEFAULT_CHARSET);
        String resource = ArtifactBase.newArtifactID(KernelSchema.GRAPH_ARTIFACTS);
        RDFLoaderResult result = loader.loadRDF(logger, new StringReader(contentString), resource, null);
        if (!logger.getErrorMessages().isEmpty()) {
            logger.error("Failed to parse the content");
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, Utils.getLog(logger));
        }
        Date artifactCreation = new Date();
        IRINode artifactNode = nodeManager.getIRINode(resource);
        IRINode registry = nodeManager.getIRINode(KernelSchema.GRAPH_ARTIFACTS);
        List<Quad> metadata = new ArrayList<>();
        metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(Vocabulary.rdfType), nodeManager.getIRINode(KernelSchema.ARTIFACT)));
        if (names != null && names.length >= 1)
            metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(KernelSchema.NAME), nodeManager.getLiteralNode(names[0], Vocabulary.xsdString, null)));
        metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(KernelSchema.FROM), nodeManager.getLiteralNode(identifier, Vocabulary.xsdString, null)));
        metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(KernelSchema.CREATED), nodeManager.getLiteralNode(DateFormat.getDateTimeInstance().format(artifactCreation), Vocabulary.xsdDateTime, null)));
        Artifact artifact = new ArtifactSimple(metadata, result.getQuads());

        queueInput(artifact);
        return new HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, artifact.serializedJSON());
    }
}
