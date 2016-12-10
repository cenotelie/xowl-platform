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

import org.xowl.infra.server.ServerConfiguration;
import org.xowl.infra.server.api.XOWLDatabase;
import org.xowl.infra.server.api.XOWLServer;
import org.xowl.infra.server.embedded.EmbeddedServer;
import org.xowl.infra.server.remote.RemoteServer;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.RDFUtils;
import org.xowl.infra.store.Repository;
import org.xowl.infra.store.rdf.Changeset;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultFailure;
import org.xowl.infra.store.sparql.ResultQuads;
import org.xowl.infra.store.writers.NQuadsSerializer;
import org.xowl.infra.store.writers.RDFSerializer;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.infra.utils.metrics.MetricSnapshotInt;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.storage.TripleStore;
import org.xowl.platform.services.storage.StorageService;
import org.xowl.platform.services.storage.jobs.DeleteArtifactJob;
import org.xowl.platform.services.storage.jobs.PullArtifactFromLiveJob;
import org.xowl.platform.services.storage.jobs.PushArtifactToLiveJob;

import java.io.Closeable;
import java.io.File;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Implements a triple store service that is backed by a xOWL Server
 *
 * @author Laurent Wouters
 */
public class XOWLStorageService implements StorageService, Closeable {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/services/storage";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLStorageService.class, "/org/xowl/platform/services/storage/api_service_storage.raml", "Storage Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLStorageService.class, "/org/xowl/platform/services/storage/api_service_storage.html", "Storage Service - Documentation", HttpApiResource.MIME_HTML);


    /**
     * The remote server
     */
    private final XOWLServer server;
    /**
     * The live store
     */
    private final XOWLFederationStore storeLive;
    /**
     * The long term store
     */
    private final XOWLFederationStore storeLongTerm;
    /**
     * The service store
     */
    private final XOWLFederationStore storeService;

    /**
     * Initializes this service
     */
    public XOWLStorageService() {
        ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
        Configuration configuration = configurationService.getConfigFor(this);
        this.server = resolveServer(configuration);
        this.storeLive = new XOWLFederationStore(configuration.get("databases", "live")) {
            @Override
            protected XOWLDatabase resolveBackend() {
                return XOWLStorageService.this.resolveRemote(this.getName());
            }
        };
        this.storeLongTerm = new XOWLFederationStore(configuration.get("databases", "longTerm")) {
            @Override
            protected XOWLDatabase resolveBackend() {
                return XOWLStorageService.this.resolveRemote(this.getName());
            }
        };
        this.storeService = new XOWLFederationStore(configuration.get("databases", "service")) {
            @Override
            protected XOWLDatabase resolveBackend() {
                return XOWLStorageService.this.resolveRemote(this.getName());
            }
        };
    }

    /**
     * Resolves the server
     *
     * @return The backing server
     */
    private XOWLServer resolveServer(Configuration configuration) {
        String backendType = configuration.get("backend");
        if (backendType.equalsIgnoreCase("remote")) {
            String endpoint = configuration.get("remote", "endpoint");
            if (endpoint == null)
                return null;
            XOWLServer server = new RemoteServer(endpoint);
            XSPReply reply = server.login(configuration.get("remote", "login"), configuration.get("remote", "password"));
            if (!reply.isSuccess())
                return null;
            return server;
        } else {
            try {
                String location = (new File(System.getenv(Env.ROOT), configuration.get("embedded", "location"))).getAbsolutePath();
                XOWLServer server = new EmbeddedServer(Logging.getDefault(), new ServerConfiguration(location));
                XSPReply reply = server.getDatabase(configuration.get("databases", "live"));
                if (!reply.isSuccess()) {
                    // initialize
                    if (!server.createDatabase(configuration.get("databases", "live")).isSuccess())
                        return null;
                    if (!server.createDatabase(configuration.get("databases", "longTerm")).isSuccess())
                        return null;
                    if (!server.createDatabase(configuration.get("databases", "service")).isSuccess())
                        return null;
                }
                return server;
            } catch (Exception exception) {
                Logging.getDefault().error(exception);
                return null;
            }
        }
    }

    /**
     * Resolves the remote for this store
     *
     * @param name The name of this store
     * @return The remote
     */
    private XOWLDatabase resolveRemote(String name) {
        if (server == null)
            return null;
        XSPReply reply = server.getDatabase(name);
        if (!reply.isSuccess())
            return null;
        return ((XSPReplyResult<XOWLDatabase>) reply).getData();
    }

    @Override
    public String getIdentifier() {
        return XOWLStorageService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Storage Service";
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
    public XSPReply store(Artifact artifact) {
        return storeLongTerm.store(artifact);
    }

    @Override
    public XSPReply retrieve(String identifier) {
        return storeLongTerm.retrieve(identifier);
    }

    @Override
    public XSPReply retrieve(String base, String version) {
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write(">. ?a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.BASE));
        writer.write("> <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(base));
        writer.write(">. ?a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.VERSION));
        writer.write("> \"");
        writer.write(TextUtils.escapeStringW3C(version));
        writer.write("\" } }");
        Result result = storeLongTerm.sparql(writer.toString());
        if (result.isFailure())
            return new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, ((ResultFailure) result).getMessage());
        Collection<Quad> metadata = ((ResultQuads) result).getQuads();
        if (metadata.isEmpty())
            return XSPReplyNotFound.instance();
        return new XSPReplyResult<>(storeLongTerm.buildArtifact(metadata));
    }

    @Override
    public XSPReply delete(String identifier) {
        XSPReply result = storeLive.delete(identifier);
        if (!result.isSuccess())
            return result;
        return storeLongTerm.delete(identifier);
    }

    @Override
    public XSPReply delete(Artifact artifact) {
        return delete(artifact.getIdentifier());
    }

    @Override
    public XSPReply getAllArtifacts() {
        return storeLongTerm.getArtifacts();
    }

    @Override
    public XSPReply getArtifactsForBase(String base) {
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write(">. ?a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.BASE));
        writer.write("> <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(base));
        writer.write("> } }");

        Result sparqlResult = storeLongTerm.sparql(writer.toString());
        if (sparqlResult.isFailure())
            return new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, ((ResultFailure) sparqlResult).getMessage());
        return new XSPReplyResultCollection<>(storeLongTerm.buildArtifacts(((ResultQuads) sparqlResult).getQuads()));
    }

    @Override
    public XSPReply getArtifactsForArchetype(String archetype) {
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write(">. ?a <");
        writer.write(TextUtils.escapeAbsoluteURIW3C(KernelSchema.ARCHETYPE));
        writer.write("> \"");
        writer.write(TextUtils.escapeAbsoluteURIW3C(archetype));
        writer.write("\" } }");

        Result sparqlResult = storeLongTerm.sparql(writer.toString());
        if (sparqlResult.isFailure())
            return new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, ((ResultFailure) sparqlResult).getMessage());
        return new XSPReplyResultCollection<>(storeLongTerm.buildArtifacts(((ResultQuads) sparqlResult).getQuads()));
    }

    @Override
    public XSPReply getLiveArtifacts() {
        return storeLive.getArtifacts();
    }

    @Override
    public XSPReply pushToLive(Artifact artifact) {
        return storeLive.store(artifact);
    }

    @Override
    public XSPReply pullFromLive(Artifact artifact) {
        return storeLive.delete(artifact.getIdentifier());
    }

    @Override
    public Collection<Metric> getMetrics() {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(METRIC_TOTAL_ARTIFACTS_COUNT);
        metrics.add(METRIC_LIVE_ARTIFACTS_COUNT);
        metrics.add(((XSPReplyResult<Metric>) storeService.getMetric()).getData());
        metrics.add(((XSPReplyResult<Metric>) storeLongTerm.getMetric()).getData());
        metrics.add(((XSPReplyResult<Metric>) storeLive.getMetric()).getData());
        return metrics;
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        if (metric == METRIC_LIVE_ARTIFACTS_COUNT) {
            return new MetricSnapshotInt(storeLive.getArtifactsCount());
        } else if (metric == METRIC_TOTAL_ARTIFACTS_COUNT) {
            return new MetricSnapshotInt(storeLongTerm.getArtifactsCount());
        } else if (metric == storeService.metricStatistics) {
            XSPReply reply = storeService.getMetricSnapshot();
            if (!reply.isSuccess())
                return null;
            return ((XSPReplyResult<MetricSnapshot>) reply).getData();
        } else if (metric == storeLongTerm.metricStatistics) {
            XSPReply reply = storeLongTerm.getMetricSnapshot();
            if (!reply.isSuccess())
                return null;
            return ((XSPReplyResult<MetricSnapshot>) reply).getData();
        } else if (metric == storeLive.metricStatistics) {
            XSPReply reply = storeLive.getMetricSnapshot();
            if (!reply.isSuccess())
                return null;
            return ((XSPReplyResult<MetricSnapshot>) reply).getData();
        }
        return null;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/sparql")) {
            return onMessageSPARQL(request);
        } else if (request.getUri().equals(URI_API + "/artifacts")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            // get artifacts
            String[] archetypes = request.getParameter("archetype");
            if (archetypes != null && archetypes.length > 0) {
                // get all artifacts for an archetype
                XSPReply reply = getArtifactsForArchetype(archetypes[0]);
                if (!reply.isSuccess())
                    return XSPReplyUtils.toHttpResponse(reply, null);
                boolean first = true;
                StringBuilder builder = new StringBuilder("[");
                for (Artifact artifact : ((XSPReplyResultCollection<Artifact>) reply).getData()) {
                    if (!first)
                        builder.append(", ");
                    first = false;
                    builder.append(artifact.serializedJSON());
                }
                builder.append("]");
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
            }
            String[] bases = request.getParameter("base");
            if (bases != null && bases.length > 0) {
                // get all artifacts for an base
                XSPReply reply = getArtifactsForBase(bases[0]);
                if (!reply.isSuccess())
                    return XSPReplyUtils.toHttpResponse(reply, null);
                boolean first = true;
                StringBuilder builder = new StringBuilder("[");
                for (Artifact artifact : ((XSPReplyResultCollection<Artifact>) reply).getData()) {
                    if (!first)
                        builder.append(", ");
                    first = false;
                    builder.append(artifact.serializedJSON());
                }
                builder.append("]");
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
            } else {
                XSPReply reply = getAllArtifacts();
                if (!reply.isSuccess())
                    return XSPReplyUtils.toHttpResponse(reply, null);
                boolean first = true;
                StringBuilder builder = new StringBuilder("[");
                for (Artifact artifact : ((XSPReplyResultCollection<Artifact>) reply).getData()) {
                    if (!first)
                        builder.append(", ");
                    first = false;
                    builder.append(artifact.serializedJSON());
                }
                builder.append("]");
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
            }
        } else if (request.getUri().equals(URI_API + "/artifacts/diff")) {
            if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
            // diff artifacts left and right
            String[] lefts = request.getParameter("left");
            String[] rights = request.getParameter("right");
            if (lefts == null || lefts.length == 0)
                return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'left'"), null);
            if (rights == null || rights.length == 0)
                return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'right'"), null);
            return onMessageDiffArtifacts(lefts[0], rights[0]);
        } else if (request.getUri().equals(URI_API + "/artifacts/live")) {
            XSPReply reply = getLiveArtifacts();
            if (!reply.isSuccess())
                return XSPReplyUtils.toHttpResponse(reply, null);
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (Artifact artifact : ((XSPReplyResultCollection<Artifact>) reply).getData()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(artifact.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else if (request.getUri().startsWith(URI_API + "/artifacts")) {
            String rest = request.getUri().substring(URI_API.length() + "/artifacts".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String artifactId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                switch (request.getMethod()) {
                    case HttpConstants.METHOD_GET: {
                        XSPReply reply = retrieve(artifactId);
                        if (!reply.isSuccess())
                            return XSPReplyUtils.toHttpResponse(reply, null);
                        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, ((XSPReplyResult<Artifact>) reply).getData().serializedJSON());
                    }
                    case HttpConstants.METHOD_DELETE:
                        return onMessageDeleteArtifact(artifactId);
                }
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, DELETE");
            } else {
                switch (rest.substring(index)) {
                    case "/metadata": {
                        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                        return onMessageGetArtifactMetadata(artifactId);
                    }
                    case "/content": {
                        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                        return onMessageGetArtifactContent(artifactId);
                    }
                    case "/activate": {
                        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                        return onMessagePushToLive(artifactId);
                    }
                    case "/deactivate": {
                        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                        return onMessagePullFromLive(artifactId);
                    }
                }
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Override
    public HttpApiResource getApiSpecification() {
        return RESOURCE_SPECIFICATION;
    }

    @Override
    public HttpApiResource getApiDocumentation() {
        return RESOURCE_DOCUMENTATION;
    }

    @Override
    public HttpApiResource[] getApiOtherResources() {
        return null;
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(HttpApiService.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"specification\": " +
                RESOURCE_SPECIFICATION.serializedJSON() +
                ", \"documentation\": " +
                RESOURCE_DOCUMENTATION.serializedJSON() +
                "}";
    }

    /**
     * Responds to a SPARQL command
     *
     * @param request The request to handle
     * @return The response
     */
    private HttpResponse onMessageSPARQL(HttpApiRequest request) {
        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
        if (request.getContent() == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
        String[] accept = request.getHeader(HttpConstants.HEADER_ACCEPT);
        String sparql = new String(request.getContent(), Files.CHARSET);
        Result result = storeLive.sparql(sparql);
        return XSPReplyUtils.toHttpResponse(new XSPReplyResult<>(result), Arrays.asList(accept));
    }

    /**
     * Responds to a request for the header of a specified artifact
     *
     * @param artifactId The identifier of an artifact
     * @return The response
     */
    private HttpResponse onMessageGetArtifactMetadata(String artifactId) {
        XSPReply reply = retrieve(artifactId);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        Artifact artifact = ((XSPReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, "Failed to retrieve the artifact"), null);
        BufferedLogger logger = new BufferedLogger();
        StringWriter writer = new StringWriter();
        RDFSerializer serializer = new NQuadsSerializer(writer);
        serializer.serialize(logger, artifact.getMetadata().iterator());
        if (!logger.getErrorMessages().isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, logger.getErrorsAsString());
        return new HttpResponse(HttpURLConnection.HTTP_OK, Repository.SYNTAX_NQUADS, writer.toString());
    }

    /**
     * Responds to a request for the content of a specified artifact
     *
     * @param artifactId The identifier of an artifact
     * @return The artifact
     */
    private HttpResponse onMessageGetArtifactContent(String artifactId) {
        XSPReply reply = retrieve(artifactId);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        Artifact artifact = ((XSPReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, "Failed to retrieve the artifact"), null);
        Collection<Quad> content = artifact.getContent();
        if (content == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, "Failed to retrieve the artifact's content"), null);
        BufferedLogger logger = new BufferedLogger();
        StringWriter writer = new StringWriter();
        RDFSerializer serializer = new NQuadsSerializer(writer);
        serializer.serialize(logger, content.iterator());
        if (!logger.getErrorMessages().isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, logger.getErrorsAsString());
        return new HttpResponse(HttpURLConnection.HTTP_OK, Repository.SYNTAX_NQUADS, writer.toString());
    }

    /**
     * Responds to a request to delete an artifact
     *
     * @param artifactId The identifier of an artifact
     * @return The response
     */
    private HttpResponse onMessageDeleteArtifact(String artifactId) {
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        Job job = new DeleteArtifactJob(artifactId);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }

    /**
     * Responds to a request for the computation of the diff between two artifacts
     *
     * @param artifactLeft  The identifier of the artifact on the left
     * @param artifactRight The identifier of the artifact on the right
     * @return The artifact
     */
    private HttpResponse onMessageDiffArtifacts(String artifactLeft, String artifactRight) {
        XSPReply reply = retrieve(artifactLeft);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        Artifact artifact = ((XSPReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, "Failed to retrieve the artifact"), null);
        Collection<Quad> contentLeft = artifact.getContent();
        if (contentLeft == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, "Failed to retrieve the content of the artifact"), null);

        reply = retrieve(artifactRight);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        artifact = ((XSPReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, "Failed to retrieve the artifact"), null);
        Collection<Quad> contentRight = artifact.getContent();
        if (contentRight == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(XOWLFederationStore.ERROR_OPERATION_FAILED, "Failed to retrieve the content of the artifact"), null);

        Changeset changeset = RDFUtils.diff(contentLeft, contentRight, true);
        BufferedLogger logger = new BufferedLogger();
        StringWriter writer = new StringWriter();
        RDFSerializer serializer = new NQuadsSerializer(writer);
        writer.write("--xowlQuads" + Files.LINE_SEPARATOR);
        serializer.serialize(logger, changeset.getAdded().iterator());
        writer.write("--xowlQuads" + Files.LINE_SEPARATOR);
        serializer.serialize(logger, changeset.getRemoved().iterator());
        return new HttpResponse(HttpURLConnection.HTTP_OK, Repository.SYNTAX_NQUADS, writer.toString());
    }

    /**
     * Responds to the request to pull an artifact from the live store
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param artifactId The identifier of an artifact
     * @return The response
     */
    private HttpResponse onMessagePullFromLive(String artifactId) {
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        Job job = new PullArtifactFromLiveJob(artifactId);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }

    /**
     * Responds to the request to push an artifact to the live store
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param artifactId The identifier of an artifact
     * @return The response
     */
    private HttpResponse onMessagePushToLive(String artifactId) {
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        Job job = new PushArtifactToLiveJob(artifactId);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }

    @Override
    public void close() {
        server.serverShutdown();
    }
}
