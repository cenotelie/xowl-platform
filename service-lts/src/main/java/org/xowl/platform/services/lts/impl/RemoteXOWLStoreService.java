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

package org.xowl.platform.services.lts.impl;

import org.xowl.platform.kernel.*;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.platform.utils.Utils;
import org.xowl.store.IOUtils;
import org.xowl.store.rdf.*;
import org.xowl.store.sparql.Result;
import org.xowl.store.sparql.ResultQuads;
import org.xowl.store.sparql.ResultSolutions;
import org.xowl.store.storage.cache.CachedNodes;
import org.xowl.store.writers.NTripleSerializer;
import org.xowl.utils.logging.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a triple store service that is backed by a remote store connected to via HTTP
 *
 * @author Laurent Wouters
 */
public class RemoteXOWLStoreService implements TripleStoreService, ArtifactStorageService, ServiceHttpServed {
    /**
     * The live store
     */
    private final RemoteXOWLStore storeLive;
    /**
     * The long term store
     */
    private final RemoteXOWLStore storeLongTerm;
    /**
     * The service store
     */
    private final RemoteXOWLStore storeService;

    /**
     * Initializes this service
     */
    public RemoteXOWLStoreService() {
        this.storeLive = new BasicRemoteXOWLStore(this, "live");
        this.storeLongTerm = new BasicRemoteXOWLStore(this, "longTerm");
        this.storeService = new BasicRemoteXOWLStore(this, "service");
    }

    @Override
    public String getIdentifier() {
        return RemoteXOWLStoreService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Triple Store Service";
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
    public TripleStore getLiveStore() {
        return storeLive;
    }

    @Override
    public TripleStore getLongTermStore() {
        return storeLongTerm;
    }

    @Override
    public TripleStore getServiceStore() {
        return storeService;
    }

    @Override
    public boolean store(Artifact artifact) {
        return store(artifact, storeLongTerm);
    }

    @Override
    public Artifact retrieve(String identifier) {
        Result result = storeLongTerm.sparql("DESCRIBE <" + IOUtils.escapeAbsoluteURIW3C(identifier) + ">");
        if (result.isFailure())
            return null;
        Collection<Quad> metadata = ((ResultQuads) result).getQuads();
        if (metadata.isEmpty())
            return null;
        return buildArtifact(metadata);
    }

    @Override
    public Artifact retrieve(String base, String version) {
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write(">. ?a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.BASE));
        writer.write("> <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(base));
        writer.write(">. ?a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.VERSION));
        writer.write("> \"");
        writer.write(IOUtils.escapeStringW3C(version));
        writer.write("\" } }");
        Result result = storeLongTerm.sparql(writer.toString());
        if (result.isFailure())
            return null;
        Collection<Quad> metadata = ((ResultQuads) result).getQuads();
        if (metadata.isEmpty())
            return null;
        return buildArtifact(metadata);
    }

    @Override
    public Collection<Artifact> list() {
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write("> } }");

        Result sparqlResult = storeLongTerm.sparql(writer.toString());
        if (sparqlResult.isFailure())
            return new ArrayList<>();
        return buildArtifacts(((ResultQuads) sparqlResult).getQuads());
    }

    @Override
    public Collection<Artifact> list(String base) {
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write(">. ?a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.BASE));
        writer.write("> <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(base));
        writer.write("> } }");

        Result sparqlResult = storeLongTerm.sparql(writer.toString());
        if (sparqlResult.isFailure())
            return new ArrayList<>();
        return buildArtifacts(((ResultQuads) sparqlResult).getQuads());
    }

    @Override
    public boolean isLive(Artifact artifact) {
        // TODO: implement this
        return false;
    }

    @Override
    public boolean pushToLive(Artifact artifact) {
        return store(artifact, storeLive);
    }

    @Override
    public boolean pullFromLive(Artifact artifact) {
        return delete(artifact, storeLive);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if ("/sparql".equals(uri))
            return onMessageSPARQL(content, accept);
        if ("/artifacts".equals(uri))
            return onMessageGetArtifacts();
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Responds to a SPARQL command
     *
     * @param content The SPARQL content
     * @param accept  The accept HTTP header
     * @return The response
     */
    private HttpResponse onMessageSPARQL(byte[] content, String accept) {
        if (content == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        String request = new String(content, Utils.DEFAULT_CHARSET);
        Result result = storeLive.sparql(request);
        String responseType = Result.SYNTAX_JSON;
        switch (accept) {
            case Result.SYNTAX_CSV:
            case Result.SYNTAX_TSV:
            case Result.SYNTAX_XML:
            case Result.SYNTAX_JSON:
                responseType = accept;
                break;
        }
        StringWriter writer = new StringWriter();
        try {
            result.print(writer, responseType);
        } catch (IOException exception) {
            // cannot happen
        }
        return new HttpResponse(HttpURLConnection.HTTP_OK, responseType, writer.toString());
    }

    /**
     * Responds to a request to list the artifacts
     *
     * @return The response
     */
    private HttpResponse onMessageGetArtifacts() {
        Collection<Artifact> artifacts = list();
        boolean first = true;
        StringBuilder builder = new StringBuilder("[");
        for (Artifact artifact : artifacts) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(artifact.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, builder.toString());
    }

    /**
     * Stores the specified artifact
     *
     * @param artifact The artifact to store
     * @param store    The target store
     * @return Whether the operation succeeded
     */
    private boolean store(Artifact artifact, TripleStore store) {
        StringWriter writer = new StringWriter();
        writer.write("INSERT DATA { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ");
        NTripleSerializer serializer = new NTripleSerializer(writer);
        serializer.serialize(Logger.DEFAULT, artifact.getMetadata().iterator());
        writer.write(" } }; INSERT DATA { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(artifact.getIdentifier()));
        writer.write("> {");
        serializer.serialize(Logger.DEFAULT, artifact.getContent().iterator());
        writer.write(" } }");
        Result result = store.sparql(writer.toString());
        return result.isSuccess();
    }

    /**
     * Deletes the specified artifact
     *
     * @param artifact The artifact to delete
     * @param store    The target store
     * @return Whether the operation succeeded
     */
    private boolean delete(Artifact artifact, TripleStore store) {
        StringWriter writer = new StringWriter();
        writer.write("DELETE WHERE { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(artifact.getIdentifier()));
        writer.write("> ?p ?o } }; DROP SILENT GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(artifact.getIdentifier()));
        writer.write(">");
        Result result = store.sparql(writer.toString());
        return result.isSuccess();
    }

    /**
     * Builds the default artifacts from the specified metadata
     *
     * @param quads The metadata of multiple artifacts
     * @return The artifacts
     */
    private Collection<Artifact> buildArtifacts(Collection<Quad> quads) {
        Collection<Artifact> result = new ArrayList<>();
        Map<IRINode, Collection<Quad>> data = new HashMap<>();
        for (Quad quad : quads) {
            if (quad.getSubject().getNodeType() == Node.TYPE_IRI) {
                IRINode subject = (IRINode) quad.getSubject();
                Collection<Quad> metadata = data.get(subject);
                if (metadata == null) {
                    metadata = new ArrayList<>();
                    data.put(subject, metadata);
                }
                metadata.add(quad);
            }
        }
        for (Map.Entry<IRINode, Collection<Quad>> entry : data.entrySet()) {
            result.add(buildArtifact(entry.getValue()));
        }
        return result;
    }

    /**
     * Builds the default artifact from the specified metadata
     *
     * @param metadata The metadata
     * @return The artifact
     */
    private Artifact buildArtifact(Collection<Quad> metadata) {
        return new ArtifactDeferred(metadata) {
            @Override
            protected Collection<Quad> load() {
                // TODO: change for a CONSTRUCT query
                Result result = storeLongTerm.sparql("SELECT ?s ?p ?o WHERE  { GRAPH <" + IOUtils.escapeAbsoluteURIW3C(identifier) + "> { ?s ?p ?o } }");
                if (result.isFailure())
                    return null;
                ResultSolutions solutions = ((ResultSolutions) result);
                Collection<Quad> quads = new ArrayList<>(solutions.getSolutions().size());
                CachedNodes nodes = new CachedNodes();
                IRINode graph = nodes.getIRINode(identifier);
                for (QuerySolution solution : solutions.getSolutions()) {
                    quads.add(new Quad(graph,
                            (SubjectNode) solution.get("s"),
                            (Property) solution.get("p"),
                            solution.get("o")));
                }
                return quads;
            }
        };
    }
}
