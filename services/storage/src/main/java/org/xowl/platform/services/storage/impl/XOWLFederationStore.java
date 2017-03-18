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
import org.xowl.infra.server.api.XOWLStoredProcedure;
import org.xowl.infra.server.api.XOWLStoredProcedureContext;
import org.xowl.infra.server.base.BaseDatabase;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.EntailmentRegime;
import org.xowl.infra.store.rdf.LiteralNode;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.rdf.RDFPatternSolution;
import org.xowl.infra.store.rdf.SubjectNode;
import org.xowl.infra.store.sparql.*;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricComposite;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactDeferred;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.services.storage.StorageService;
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
    public XSPReply getMetric() {
        if (metricStatistics != null)
            return new XSPReplyResult<>(metricStatistics);
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        metricStatistics = new MetricComposite(
                ((XSPReplyResult<MetricComposite>) connection.getMetric()).getData(),
                "Storage Service - Database statistics for " + identifier);
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
    public XSPReply sparql(String sparql, List<String> defaultIRIs, List<String> namedIRIs) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_QUERY);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.sparql(sparql, defaultIRIs, namedIRIs);
    }

    @Override
    public XSPReply sparql(Command sparql) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_QUERY);
        if (!reply.isSuccess())
            return reply;
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
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_SET_ENTAILMENT);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.setEntailmentRegime(regime);
    }

    @Override
    public XSPReply getRules() {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getRules();
    }

    @Override
    public XSPReply getRule(String name) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getRule(name);
    }

    @Override
    public XSPReply addRule(String content, boolean activate) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_CREATE_RULE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.addRule(content, activate);
    }

    @Override
    public XSPReply removeRule(XOWLRule rule) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_DELETE_RULE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.removeRule(rule);
    }

    @Override
    public XSPReply removeRule(String rule) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_DELETE_RULE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.removeRule(rule);
    }

    @Override
    public XSPReply activateRule(XOWLRule rule) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_ACTIVATE_RULE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.activateRule(rule);
    }

    @Override
    public XSPReply activateRule(String rule) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_ACTIVATE_RULE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.activateRule(rule);
    }

    @Override
    public XSPReply deactivateRule(XOWLRule rule) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_DEACTIVATE_RULE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.deactivateRule(rule);
    }

    @Override
    public XSPReply deactivateRule(String rule) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_DEACTIVATE_RULE);
        if (!reply.isSuccess())
            return reply;
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
    public XSPReply getRuleStatus(String rule) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getRuleStatus(rule);
    }

    @Override
    public XSPReply getStoredProcedures() {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getStoredProcedures();
    }

    @Override
    public XSPReply getStoreProcedure(String iri) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.getStoreProcedure(iri);
    }

    @Override
    public XSPReply addStoredProcedure(String iri, String sparql, Collection<String> parameters) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_CREATE_PROCEDURE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.addStoredProcedure(iri, sparql, parameters);
    }

    @Override
    public XSPReply removeStoredProcedure(XOWLStoredProcedure procedure) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_DELETE_PROCEDURE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.removeStoredProcedure(procedure);
    }

    @Override
    public XSPReply removeStoredProcedure(String procedure) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_DELETE_PROCEDURE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.removeStoredProcedure(procedure);
    }

    @Override
    public XSPReply executeStoredProcedure(XOWLStoredProcedure procedure, XOWLStoredProcedureContext context) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_EXECUTE_PROCEDURE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.executeStoredProcedure(procedure, context);
    }

    @Override
    public XSPReply executeStoredProcedure(String procedure, XOWLStoredProcedureContext context) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_EXECUTE_PROCEDURE);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.executeStoredProcedure(procedure, context);
    }

    @Override
    public XSPReply upload(String syntax, String content) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_UPLOAD_RAW);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.upload(syntax, content);
    }

    @Override
    public XSPReply upload(Collection<Quad> quads) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(StorageService.ACTION_UPLOAD_RAW);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.upload(quads);
    }

    @Override
    public XSPReply getArtifacts() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ArtifactStorageService.ACTION_RETRIEVE_METADATA);
        if (!reply.isSuccess())
            return reply;
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write("> } }");
        reply = connection.sparql(writer.toString(), null, null);
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
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ArtifactStorageService.ACTION_STORE);
        if (!reply.isSuccess())
            return reply;
        Collection<Quad> metadata = artifact.getMetadata();
        if (metadata == null || metadata.isEmpty())
            return new XSPReplyApiError(ArtifactStorageService.ERROR_INVALID_ARTIFACT, "Empty metadata.");
        Collection<Quad> content = artifact.getContent();
        if (content == null)
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to fetch the artifact's content.");
        reply = upload(metadata);
        if (!reply.isSuccess())
            return reply;
        return upload(content);
    }

    @Override
    public XSPReply retrieve(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ArtifactStorageService.ACTION_RETRIEVE_METADATA);
        if (!reply.isSuccess())
            return reply;
        reply = sparql("DESCRIBE <" + TextUtils.escapeAbsoluteURIW3C(identifier) + ">", null, null);
        if (!reply.isSuccess())
            return reply;
        Result result = ((XSPReplyResult<Result>) reply).getData();
        if (result.isFailure())
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, ((ResultFailure) result).getMessage());
        Collection<Quad> metadata = ((ResultQuads) result).getQuads();
        if (metadata.isEmpty())
            return XSPReplyNotFound.instance();
        return new XSPReplyResult<>(buildArtifact(metadata));
    }

    @Override
    public XSPReply delete(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ArtifactStorageService.ACTION_DELETE);
        if (!reply.isSuccess())
            return reply;
        StringWriter writer = new StringWriter();
        writer.write("DELETE WHERE { GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(identifier));
        writer.write("> ?p ?o } }; DROP SILENT GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(identifier));
        writer.write(">");
        reply = sparql(writer.toString(), null, null);
        if (!reply.isSuccess())
            return reply;
        Result result = ((XSPReplyResult<Result>) reply).getData();
        if (result.isSuccess())
            return XSPReplySuccess.instance();
        return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, ((ResultFailure) result).getMessage());
    }

    /**
     * Builds the default artifacts from the specified metadata
     *
     * @param quads The metadata of multiple artifacts
     * @return The artifacts
     */
    public Collection<Artifact> buildArtifacts(Collection<Quad> quads) {
        Collection<Artifact> result = new ArrayList<>();
        Map<SubjectNode, Collection<Quad>> data = PlatformUtils.mapBySubject(quads);
        for (Map.Entry<SubjectNode, Collection<Quad>> entry : data.entrySet()) {
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
                SecurityService securityService = Register.getComponent(SecurityService.class);
                if (securityService == null)
                    return Collections.emptyList();
                XSPReply reply = securityService.checkAction(ArtifactStorageService.ACTION_RETRIEVE_CONTENT);
                if (!reply.isSuccess())
                    return Collections.emptyList();
                XOWLDatabase connection = getBackend();
                if (connection == null)
                    return Collections.emptyList();
                reply = connection.sparql("CONSTRUCT FROM NAMED <" + TextUtils.escapeAbsoluteURIW3C(identifier) + "> WHERE { ?s ?p ?o }", null, null);
                if (!reply.isSuccess())
                    return Collections.emptyList();
                Result result = ((XSPReplyResult<Result>) reply).getData();
                return ((ResultQuads) result).getQuads();
            }
        };
    }

    /**
     * Executes a SPARQL query without security checks
     *
     * @param query The SPARQL query to execute
     * @return The result
     */
    public XSPReply doSparql(String query) {
        XOWLDatabase connection = getBackend();
        if (connection == null)
            return XSPReplyNetworkError.instance();
        return connection.sparql(query, null, null);
    }
}
