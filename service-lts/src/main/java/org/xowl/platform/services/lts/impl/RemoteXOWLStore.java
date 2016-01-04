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

import org.xowl.platform.kernel.Artifact;
import org.xowl.platform.kernel.ArtifactDeferred;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.store.IOUtils;
import org.xowl.store.rdf.IRINode;
import org.xowl.store.rdf.Node;
import org.xowl.store.rdf.Quad;
import org.xowl.store.sparql.Result;
import org.xowl.store.sparql.ResultFailure;
import org.xowl.store.sparql.ResultQuads;
import org.xowl.store.storage.remote.HTTPConnection;
import org.xowl.store.writers.NTripleSerializer;
import org.xowl.store.xsp.*;
import org.xowl.utils.logging.Logger;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a remote xOWL store for this platform
 *
 * @author Laurent Wouters
 */
abstract class RemoteXOWLStore implements TripleStore {
    /**
     * Gets the connection for this store
     *
     * @return The connection for this store
     */
    protected abstract HTTPConnection getConnection();

    @Override
    public Result sparql(String query) {
        HTTPConnection connection = getConnection();
        if (connection == null)
            return new ResultFailure("The connection to the remote host is not configured");
        return connection.sparql(query);
    }

    @Override
    public XSPReply execute(String command) {
        HTTPConnection connection = getConnection();
        if (connection == null)
            return new XSPReplyNetworkError("The connection to the remote host is not configured");
        return connection.xsp(command);
    }

    @Override
    public Collection<Artifact> getArtifacts() {
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write("> } }");

        Result sparqlResult = sparql(writer.toString());
        if (sparqlResult.isFailure())
            return new ArrayList<>();
        return buildArtifacts(((ResultQuads) sparqlResult).getQuads());
    }

    @Override
    public XSPReply store(Artifact artifact) {
        Collection<Quad> metadata = artifact.getMetadata();
        if (metadata == null || metadata.isEmpty())
            return new XSPReplyFailure("Invalid artifact (empty metadata)");
        Collection<Quad> content = artifact.getContent();
        if (content == null)
            return new XSPReplyFailure("Failed to fetch the artifact's content");
        StringWriter writer = new StringWriter();
        writer.write("INSERT DATA { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ");
        NTripleSerializer serializer = new NTripleSerializer(writer);
        serializer.serialize(Logger.DEFAULT, metadata.iterator());
        writer.write(" } }; INSERT DATA { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(artifact.getIdentifier()));
        writer.write("> {");
        serializer.serialize(Logger.DEFAULT, content.iterator());
        writer.write(" } }");
        Result result = sparql(writer.toString());
        if (result.isSuccess())
            return XSPReplySuccess.instance();
        return new XSPReplyFailure(((ResultFailure) result).getMessage());
    }

    @Override
    public XSPReply retrieve(String identifier) {
        Result result = sparql("DESCRIBE <" + IOUtils.escapeAbsoluteURIW3C(identifier) + ">");
        if (result.isFailure())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        Collection<Quad> metadata = ((ResultQuads) result).getQuads();
        if (metadata.isEmpty())
            return new XSPReplyFailure("Unknown artifact");
        return new XSPReplyResult<>(buildArtifact(metadata));
    }

    @Override
    public XSPReply delete(String identifier) {
        StringWriter writer = new StringWriter();
        writer.write("DELETE WHERE { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(identifier));
        writer.write("> ?p ?o } }; DROP SILENT GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(identifier));
        writer.write(">");
        Result result = sparql(writer.toString());
        if (result.isSuccess())
            return XSPReplySuccess.instance();
        return new XSPReplyFailure(((ResultFailure) result).getMessage());
    }

    /**
     * Builds the default artifacts from the specified metadata
     *
     * @param quads The metadata of multiple artifacts
     * @return The artifacts
     */
    public Collection<Artifact> buildArtifacts(Collection<Quad> quads) {
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
    public Artifact buildArtifact(Collection<Quad> metadata) {
        return new ArtifactDeferred(metadata) {
            @Override
            protected Collection<Quad> load() {
                Result result = sparql("CONSTRUCT FROM NAMED <" + IOUtils.escapeAbsoluteURIW3C(identifier) + "> WHERE { ?s ?p ?o }");
                if (result.isFailure())
                    return null;
                return ((ResultQuads) result).getQuads();
            }
        };
    }
}
