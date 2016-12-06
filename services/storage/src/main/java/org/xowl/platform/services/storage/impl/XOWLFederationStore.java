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

package org.xowl.platform.services.storage.impl;

import org.xowl.infra.server.api.XOWLDatabase;
import org.xowl.infra.server.api.XOWLRule;
import org.xowl.infra.server.base.BaseDatabase;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.EntailmentRegime;
import org.xowl.infra.store.rdf.*;
import org.xowl.infra.store.sparql.*;
import org.xowl.infra.utils.ApiError;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricComposite;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactDeferred;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.storage.TripleStore;

import java.io.StringWriter;
import java.util.*;

/**
 * Represents a federation store
 *
 * @author Laurent Wouters
 */
abstract class XOWLFederationStore extends BaseDatabase implements TripleStore {
    /**
     * API error - The requested operation failed in storage
     */
    public static final ApiError ERROR_OPERATION_FAILED = new ApiError(0x00020001,
            "The requested operation failed in storage.",
            HttpApiService.ERROR_HELP_PREFIX + "0x00020001.html");
    /**
     * API error - The artifact is invalid
     */
    public static final ApiError ERROR_INVALID_ARTIFACT = new ApiError(0x00020002,
            "The artifact is invalid.",
            HttpApiService.ERROR_HELP_PREFIX + "0x00020002.html");

    /**
     * The metric for the statistics of this database
     */
    protected Metric metricStatistics;
    /**
     * The remote backend
     */
    private XOWLDatabase backend;

    /**
     * Initializes this database
     *
     * @param name The database's name
     */
    public XOWLFederationStore(String name) {
        super(name);
    }

    /**
     * Gets the connection for this store
     *
     * @return The connection for this store
     */
    private XOWLDatabase getBackend() {
        if (backend == null)
            backend = resolveBackend();
        return backend;
    }

    /**
     * Resolves the backend for this store
     *
     * @return The backend
     */
    protected abstract XOWLDatabase resolveBackend();

    @Override
    public XSPReply sparql(String sparql, List<String> defaultIRIs, List<String> namedIRIs) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.sparql(sparql, defaultIRIs, namedIRIs);
    }

    @Override
    public XSPReply sparql(Command sparql) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.sparql(sparql);
    }

    @Override
    public XSPReply getEntailmentRegime() {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getEntailmentRegime();
    }

    @Override
    public XSPReply setEntailmentRegime(EntailmentRegime regime) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.setEntailmentRegime(regime);
    }

    @Override
    public XSPReply getRule(String name) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getRule(name);
    }

    @Override
    public XSPReply getRules() {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getRules();
    }

    @Override
    public XSPReply addRule(String content, boolean activate) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.addRule(content, activate);
    }

    @Override
    public XSPReply removeRule(XOWLRule rule) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.removeRule(rule);
    }

    @Override
    public XSPReply activateRule(XOWLRule rule) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.activateRule(rule);
    }

    @Override
    public XSPReply deactivateRule(XOWLRule rule) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.deactivateRule(rule);
    }

    @Override
    public XSPReply getRuleStatus(XOWLRule rule) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getRuleStatus(rule);
    }

    @Override
    public XSPReply upload(String syntax, String content) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.upload(syntax, content);
    }

    @Override
    public XSPReply upload(Collection<Quad> quads) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.upload(quads);
    }

    @Override
    public XSPReply getMetric() {
        if (metricStatistics != null)
            return new XSPReplyResult<>(metricStatistics);
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        metricStatistics = new MetricComposite(
                ((XSPReplyResult<MetricComposite>) connection.getMetric()).getData(),
                "Storage Service - Database statistics for " + name);
        return new XSPReplyResult<>(metricStatistics);
    }

    @Override
    public XSPReply getMetricSnapshot() {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getMetricSnapshot();
    }

    @Override
    public Result sparql(String query) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return new ResultFailure("The connection to the remote host is not configured");
        XSPReply reply = connection.sparql(query, null, null);
        if (!reply.isSuccess())
            return new ResultFailure(reply.getMessage());
        return ((XSPReplyResult<Result>) reply).getData();
    }

    @Override
    public XSPReply getArtifacts() {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write("> } }");
        XSPReply reply = connection.sparql(writer.toString(), null, null);
        if (!reply.isSuccess())
            return reply;
        ResultQuads sparqlResult = ((XSPReplyResult<ResultQuads>) reply).getData();
        return new XSPReplyResultCollection<>(buildArtifacts(sparqlResult.getQuads()));
    }

    /**
     * Gets the number of artifacts in this store
     *
     * @return The number of artifacts
     */
    public int getArtifactsCount() {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return -1;
        StringWriter writer = new StringWriter();
        writer.write("SELECT (COUNT(?a) AS ?c) WHERE { GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write("> } }");
        XSPReply reply = connection.sparql(writer.toString(), null, null);
        if (!reply.isSuccess())
            return -1;
        ResultSolutions sparqlResult = ((XSPReplyResult<ResultSolutions>) reply).getData();
        RDFPatternSolution solution = sparqlResult.getSolutions().iterator().next();
        return Integer.parseInt(((LiteralNode) solution.get("c")).getLexicalValue());
    }

    @Override
    public XSPReply store(Artifact artifact) {
        Collection<Quad> metadata = artifact.getMetadata();
        if (metadata == null || metadata.isEmpty())
            return new XSPReplyApiError(ERROR_INVALID_ARTIFACT, "Empty metadata.");
        Collection<Quad> content = artifact.getContent();
        if (content == null)
            return new XSPReplyApiError(ERROR_OPERATION_FAILED, "Failed to fetch the artifact's content.");
        XSPReply reply = upload(metadata);
        if (!reply.isSuccess())
            return reply;
        return upload(content);
    }

    @Override
    public XSPReply retrieve(String identifier) {
        Result result = sparql("DESCRIBE <" + TextUtils.escapeAbsoluteURIW3C(identifier) + ">");
        if (result.isFailure())
            return new XSPReplyApiError(ERROR_OPERATION_FAILED, ((ResultFailure)result).getMessage());
        Collection<Quad> metadata = ((ResultQuads) result).getQuads();
        if (metadata.isEmpty())
            return XSPReplyNotFound.instance();
        return new XSPReplyResult<>(buildArtifact(metadata));
    }

    @Override
    public XSPReply delete(String identifier) {
        StringWriter writer = new StringWriter();
        writer.write("DELETE WHERE { GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(identifier));
        writer.write("> ?p ?o } }; DROP SILENT GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(identifier));
        writer.write(">");
        Result result = sparql(writer.toString());
        if (result.isSuccess())
            return XSPReplySuccess.instance();
        return new XSPReplyApiError(ERROR_OPERATION_FAILED, ((ResultFailure)result).getMessage());
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
                Result result = sparql("CONSTRUCT FROM NAMED <" + TextUtils.escapeAbsoluteURIW3C(identifier) + "> WHERE { ?s ?p ?o }");
                if (result.isFailure())
                    return null;
                return ((ResultQuads) result).getQuads();
            }
        };
    }
}