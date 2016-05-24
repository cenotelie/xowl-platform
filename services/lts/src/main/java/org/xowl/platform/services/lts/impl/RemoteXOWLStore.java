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

package org.xowl.platform.services.lts.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.api.XOWLDatabase;
import org.xowl.infra.server.api.XOWLRule;
import org.xowl.infra.server.api.base.BaseDatabase;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.EntailmentRegime;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.Node;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultFailure;
import org.xowl.infra.store.sparql.ResultQuads;
import org.xowl.infra.store.writers.NTripleSerializer;
import org.xowl.infra.utils.logging.Logger;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactDeferred;
import org.xowl.platform.services.lts.TripleStore;

import java.io.StringWriter;
import java.util.*;

/**
 * Represents a remote xOWL store for this platform
 *
 * @author Laurent Wouters
 */
abstract class RemoteXOWLStore extends BaseDatabase implements TripleStore {
    /**
     * The remote backend
     */
    private XOWLDatabase remote;

    /**
     * Initializes this database
     *
     * @param name The database's name
     */
    public RemoteXOWLStore(String name) {
        super(name);
    }

    /**
     * Initializes this database
     *
     * @param root The database's definition
     */
    public RemoteXOWLStore(ASTNode root) {
        super(root);
    }

    /**
     * Gets the connection for this store
     *
     * @return The connection for this store
     */
    private XOWLDatabase getRemote() {
        if (remote == null)
            remote = resolveRemote();
        return remote;
    }

    /**
     * Resolves the remote for this store
     *
     * @return The remote
     */
    protected abstract XOWLDatabase resolveRemote();

    @Override
    public XSPReply sparql(String sparql, List<String> defaultIRIs, List<String> namedIRIs) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.sparql(sparql, defaultIRIs, namedIRIs);
    }

    @Override
    public XSPReply getEntailmentRegime() {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getEntailmentRegime();
    }

    @Override
    public XSPReply setEntailmentRegime(EntailmentRegime regime) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.setEntailmentRegime(regime);
    }

    @Override
    public XSPReply getRule(String name) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getRule(name);
    }

    @Override
    public XSPReply getRules() {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getRules();
    }

    @Override
    public XSPReply addRule(String content, boolean activate) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.addRule(content, activate);
    }

    @Override
    public XSPReply removeRule(XOWLRule rule) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.removeRule(rule);
    }

    @Override
    public XSPReply activateRule(XOWLRule rule) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.activateRule(rule);
    }

    @Override
    public XSPReply deactivateRule(XOWLRule rule) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.deactivateRule(rule);
    }

    @Override
    public XSPReply getRuleStatus(XOWLRule rule) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getRuleStatus(rule);
    }

    @Override
    public XSPReply upload(String syntax, String content) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.upload(syntax, content);
    }

    @Override
    public Result sparql(String query) {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return new ResultFailure("The connection to the remote host is not configured");
        XSPReply reply = connection.sparql(query, null, null);
        if (!reply.isSuccess())
            return new ResultFailure(reply.getMessage());
        return ((XSPReplyResult<Result>) reply).getData();
    }

    @Override
    public XSPReply getArtifacts() {
        XOWLDatabase connection = getRemote();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write("> } }");
        XSPReply reply = connection.sparql(writer.toString(), null, null);
        if (!reply.isSuccess())
            return reply;
        ResultQuads sparqlResult = ((XSPReplyResult<ResultQuads>) reply).getData();
        return new XSPReplyResultCollection<>(buildArtifacts(sparqlResult.getQuads()));
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
            return XSPReplyNotFound.instance();
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
