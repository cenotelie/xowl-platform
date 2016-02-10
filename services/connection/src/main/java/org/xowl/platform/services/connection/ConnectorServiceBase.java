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

package org.xowl.platform.services.connection;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.URIUtils;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.artifacts.Artifact;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Base implementation of a domain connector
 *
 * @author Laurent Wouters
 */
public abstract class ConnectorServiceBase implements ConnectorService {
    /**
     * The maximum number of queued data packages for input toward the platform
     */
    protected static final int INPUT_QUEUE_MAX_CAPACITY = 16;

    /**
     * The queue iof input data packages, i.e. packages toward the platform
     */
    private final BlockingQueue<Artifact> input;

    /**
     * Initializes this connector
     */
    protected ConnectorServiceBase() {
        this.input = new ArrayBlockingQueue<>(INPUT_QUEUE_MAX_CAPACITY);
    }

    /**
     * Queues a data package as input toward the platform
     *
     * @param artifact The data package to queue
     * @return Whether the operation succeeded
     */
    protected boolean queueInput(Artifact artifact) {
        try {
            input.put(artifact);
            return true;
        } catch (InterruptedException exception) {
            return false;
        }
    }

    @Override
    public boolean canPullInput() {
        return false;
    }

    @Override
    public List<Artifact> getQueuedInputs() {
        List<Artifact> result = new ArrayList<>(INPUT_QUEUE_MAX_CAPACITY);
        for (Artifact artifact : input) {
            result.add(artifact);
        }
        return result;
    }

    @Override
    public int getQueuedLength() {
        return input.size();
    }

    @Override
    public boolean hasQueuedInput() {
        return !input.isEmpty();
    }

    @Override
    public XSPReply getNextInput(boolean block) {
        Artifact artifact = null;
        if (block) {
            try {
                artifact = input.take();
            } catch (InterruptedException exception) {
                // do nothing
            }
        } else {
            artifact = input.poll();
        }
        if (artifact == null)
            return new XSPReplyFailure("No queued artifact");
        return new XSPReplyResult<>(artifact);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, serializedJSON());
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(IOUtils.escapeStringJSON(ConnectorService.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(IOUtils.escapeStringJSON(getIdentifier()));
        builder.append("\", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(getName()));
        builder.append("\", \"uris\": [");
        Collection<String> uris = getURIs();
        boolean first = true;
        for (String uri : uris) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(IOUtils.escapeStringJSON(uri));
            builder.append("\"");
        }
        builder.append("], \"canPullInput\": ");
        builder.append(canPullInput());
        builder.append(", \"queue\": [");
        first = true;
        for (Artifact artifact : getQueuedInputs()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(artifact.serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }

    /**
     * Builds the metadata of an artifact
     *
     * @param artifactURI The URI of the artifact
     * @param baseURI     The URI of the artifact family
     * @param superseded  The URI of the artifacts superseded by this one
     * @param name        The artifact's name
     * @param version     The artifact's version string
     * @param from        The identifier of the connector that produced the artifact
     * @return The metadata, or null if some data were invalid
     */
    public static Collection<Quad> buildMetadata(String artifactURI, String baseURI, String[] superseded, String name, String version, String from) {
        if (artifactURI == null || artifactURI.isEmpty() || !URIUtils.isAbsolute(artifactURI))
            return null;
        NodeManager nodeManager = new CachedNodes();
        Date artifactCreation = new Date();
        IRINode artifactNode = nodeManager.getIRINode(artifactURI);
        IRINode registry = nodeManager.getIRINode(KernelSchema.GRAPH_ARTIFACTS);
        List<Quad> metadata = new ArrayList<>();
        metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(Vocabulary.rdfType), nodeManager.getIRINode(KernelSchema.ARTIFACT)));
        if (name != null && !name.isEmpty())
            metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(KernelSchema.NAME), nodeManager.getLiteralNode(name, Vocabulary.xsdString, null)));
        if (baseURI != null && !baseURI.isEmpty() && URIUtils.isAbsolute(baseURI))
            metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(KernelSchema.BASE), nodeManager.getIRINode(baseURI)));
        if (superseded != null) {
            for (int i = 0; i != superseded.length; i++) {
                if (superseded[i] != null && !superseded[i].isEmpty() && URIUtils.isAbsolute(superseded[i]))
                    metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(KernelSchema.SUPERSEDE), nodeManager.getIRINode(superseded[0])));
            }
        }
        if (version != null && !version.isEmpty())
            metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(KernelSchema.VERSION), nodeManager.getLiteralNode(version, Vocabulary.xsdString, null)));
        if (from != null && !from.isEmpty())
            metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(KernelSchema.FROM), nodeManager.getLiteralNode(from, Vocabulary.xsdString, null)));
        metadata.add(new Quad(registry, artifactNode, nodeManager.getIRINode(KernelSchema.CREATED), nodeManager.getLiteralNode(DateFormat.getDateTimeInstance().format(artifactCreation), Vocabulary.xsdDateTime, null)));
        return metadata;
    }
}
