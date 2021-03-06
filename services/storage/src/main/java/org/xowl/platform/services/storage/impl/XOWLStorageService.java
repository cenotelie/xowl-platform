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

import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.*;
import fr.cenotelie.commons.utils.http.HttpConstants;
import fr.cenotelie.commons.utils.http.HttpResponse;
import fr.cenotelie.commons.utils.http.URIUtils;
import fr.cenotelie.commons.utils.ini.IniDocument;
import fr.cenotelie.commons.utils.logging.BufferedLogger;
import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.commons.utils.metrics.Metric;
import fr.cenotelie.commons.utils.metrics.MetricSnapshot;
import fr.cenotelie.commons.utils.metrics.MetricSnapshotInt;
import org.xowl.infra.server.ServerConfiguration;
import org.xowl.infra.server.api.XOWLDatabase;
import org.xowl.infra.server.api.XOWLReplyUtils;
import org.xowl.infra.server.api.XOWLServer;
import org.xowl.infra.server.embedded.EmbeddedServer;
import org.xowl.infra.server.remote.RemoteServer;
import org.xowl.infra.store.EntailmentRegime;
import org.xowl.infra.store.RDFUtils;
import org.xowl.infra.store.rdf.Changeset;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.sparql.ResultQuads;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSchema;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.storage.StorageService;
import org.xowl.platform.services.storage.TripleStore;
import org.xowl.platform.services.storage.jobs.DeleteArtifactJob;
import org.xowl.platform.services.storage.jobs.PullArtifactFromLiveJob;
import org.xowl.platform.services.storage.jobs.PushArtifactToLiveJob;

import java.io.IOException;
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
public class XOWLStorageService implements StorageService, HttpApiService, ManagedService {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLStorageService.class, "/org/xowl/platform/services/storage/api_service_storage.raml", "Storage Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLStorageService.class, "/org/xowl/platform/services/storage/api_service_storage.html", "Storage Service - Documentation", HttpApiResource.MIME_HTML);


    /**
     * The URI for the API services
     */
    private final String apiUri;
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
        ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
        IniDocument configuration = configurationService.getConfigFor(StorageService.class.getCanonicalName());
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/services/storage";
        this.server = resolveServer(configuration);
        this.storeLive = new XOWLFederationStore(configuration.get("databases", STORE_ID_LIVE)) {
            @Override
            protected XOWLDatabase resolveBackend() {
                XOWLDatabase database = XOWLStorageService.this.resolveRemote(this.getName());
                if (database != null) {
                    Reply reply = database.getEntailmentRegime();
                    if (!reply.isSuccess())
                        return database;
                    if (((ReplyResult<EntailmentRegime>) reply).getData() == EntailmentRegime.none)
                        database.setEntailmentRegime(EntailmentRegime.OWL2_RDF);
                    for (ArtifactSchema schema : Register.getComponents(ArtifactSchema.class)) {
                        if (schema.isDeployable()) {
                            reply = database.upload(schema.getDefinition(true));
                            if (!reply.isSuccess())
                                Logging.get().warning(reply.getMessage());
                        }
                    }
                }
                return database;
            }
        };
        this.storeLongTerm = new XOWLFederationStore(configuration.get("databases", STORE_ID_LONG_TERM)) {
            @Override
            protected XOWLDatabase resolveBackend() {
                return XOWLStorageService.this.resolveRemote(this.getName());
            }
        };
        this.storeService = new XOWLFederationStore(configuration.get("databases", STORE_ID_SERVICE)) {
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
    private XOWLServer resolveServer(IniDocument configuration) {
        String backendType = configuration.get("backend");
        if (backendType.equalsIgnoreCase("remote")) {
            String endpoint = configuration.get("remote", "endpoint");
            if (endpoint == null)
                return null;
            XOWLServer server = new RemoteServer(endpoint);
            Reply reply = server.login(configuration.get("remote", "login"), configuration.get("remote", "password"));
            if (!reply.isSuccess())
                return null;
            return server;
        } else {
            try {
                String location = (PlatformUtils.resolve(configuration.get("embedded", "location"))).getAbsolutePath();
                XOWLServer server = new EmbeddedServer(Logging.get(), new ServerConfiguration(location));
                Reply reply = server.getDatabase(configuration.get("databases", STORE_ID_LIVE));
                if (!reply.isSuccess()) {
                    // initialize
                    if (!server.createDatabase(configuration.get("databases", STORE_ID_LIVE)).isSuccess())
                        return null;
                    if (!server.createDatabase(configuration.get("databases", STORE_ID_LONG_TERM)).isSuccess())
                        return null;
                    if (!server.createDatabase(configuration.get("databases", STORE_ID_SERVICE)).isSuccess())
                        return null;
                }
                return server;
            } catch (Exception exception) {
                Logging.get().error(exception);
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
        Reply reply = server.getDatabase(name);
        if (!reply.isSuccess())
            return null;
        return ((ReplyResult<XOWLDatabase>) reply).getData();
    }

    @Override
    public String getIdentifier() {
        return XOWLStorageService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Storage Service";
    }

    @Override
    public int getLifecycleTier() {
        return TIER_IO;
    }

    @Override
    public void onLifecycleStart() {
        // do nothing
    }

    @Override
    public void onLifecycleStop() {
        try {
            server.close();
        } catch (IOException exception) {
            Logging.get().error(exception);
        }
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
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
    public Reply store(Artifact artifact) {
        return storeLongTerm.store(artifact);
    }

    @Override
    public Reply retrieve(String identifier) {
        return storeLongTerm.retrieve(identifier);
    }

    @Override
    public Reply retrieve(String base, String version) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_RETRIEVE_METADATA);
        if (!reply.isSuccess())
            return reply;
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
        reply = storeLongTerm.doSparql(writer.toString());
        if (!reply.isSuccess())
            return reply;
        Collection<Quad> metadata = ((ReplyResult<ResultQuads>) reply).getData().getQuads();
        if (metadata.isEmpty())
            return ReplyNotFound.instance();
        return new ReplyResult<>(storeLongTerm.buildArtifact(metadata));
    }

    @Override
    public Reply delete(String identifier) {
        Reply result = storeLive.delete(identifier);
        if (!result.isSuccess())
            return result;
        return storeLongTerm.delete(identifier);
    }

    @Override
    public Reply delete(Artifact artifact) {
        return delete(artifact.getIdentifier());
    }

    @Override
    public Reply getAllArtifacts() {
        return storeLongTerm.getArtifacts();
    }

    @Override
    public Reply getArtifactsForBase(String base) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_RETRIEVE_METADATA);
        if (!reply.isSuccess())
            return reply;
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
        reply = storeLongTerm.doSparql(writer.toString());
        if (!reply.isSuccess())
            return reply;
        Collection<Quad> metadata = ((ReplyResult<ResultQuads>) reply).getData().getQuads();
        return new ReplyResultCollection<>(storeLongTerm.buildArtifacts(metadata));
    }

    @Override
    public Reply getArtifactsForArchetype(String archetype) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_RETRIEVE_METADATA);
        if (!reply.isSuccess())
            return reply;
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
        reply = storeLongTerm.doSparql(writer.toString());
        if (!reply.isSuccess())
            return reply;
        Collection<Quad> metadata = ((ReplyResult<ResultQuads>) reply).getData().getQuads();
        return new ReplyResultCollection<>(storeLongTerm.buildArtifacts(metadata));
    }

    @Override
    public Reply getLiveArtifacts() {
        return storeLive.getArtifacts();
    }

    @Override
    public Reply pushToLive(Artifact artifact) {
        return storeLive.store(artifact);
    }

    @Override
    public Reply pullFromLive(Artifact artifact) {
        return storeLive.delete(artifact.getIdentifier());
    }

    @Override
    public Collection<Metric> getMetrics() {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(METRIC_TOTAL_ARTIFACTS_COUNT);
        metrics.add(METRIC_LIVE_ARTIFACTS_COUNT);
        metrics.add(((ReplyResult<Metric>) storeService.getMetric()).getData());
        metrics.add(((ReplyResult<Metric>) storeLongTerm.getMetric()).getData());
        metrics.add(((ReplyResult<Metric>) storeLive.getMetric()).getData());
        return metrics;
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        if (metric == METRIC_LIVE_ARTIFACTS_COUNT) {
            return new MetricSnapshotInt(storeLive.getArtifactsCount());
        } else if (metric == METRIC_TOTAL_ARTIFACTS_COUNT) {
            return new MetricSnapshotInt(storeLongTerm.getArtifactsCount());
        } else if (metric == storeService.metricStatistics) {
            Reply reply = storeService.getMetricSnapshot();
            if (!reply.isSuccess())
                return null;
            return ((ReplyResult<MetricSnapshot>) reply).getData();
        } else if (metric == storeLongTerm.metricStatistics) {
            Reply reply = storeLongTerm.getMetricSnapshot();
            if (!reply.isSuccess())
                return null;
            return ((ReplyResult<MetricSnapshot>) reply).getData();
        } else if (metric == storeLive.metricStatistics) {
            Reply reply = storeLive.getMetricSnapshot();
            if (!reply.isSuccess())
                return null;
            return ((ReplyResult<MetricSnapshot>) reply).getData();
        }
        return null;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public boolean requireAuth(HttpApiRequest request) {
        return true;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (request.getUri().equals(apiUri + "/sparql")) {
            return onMessageSPARQL(request);
        } else if (request.getUri().equals(apiUri + "/artifacts")) {
            return handleArtifacts(request);
        } else if (request.getUri().equals(apiUri + "/artifacts/diff")) {
            return handleArtifactsDiff(request);
        } else if (request.getUri().equals(apiUri + "/artifacts/live")) {
            return handleArtifactsLive();
        } else if (request.getUri().startsWith(apiUri + "/artifacts")) {
            return handleArtifact(request);
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Handles a request for the /artifacts resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleArtifacts(HttpApiRequest request) {
        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
        // get artifacts
        String archetype = request.getParameter("archetype");
        if (archetype != null) {
            // get all artifacts for an archetype
            Reply reply = getArtifactsForArchetype(archetype);
            if (!reply.isSuccess())
                return ReplyUtils.toHttpResponse(reply);
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (Artifact artifact : ((ReplyResultCollection<Artifact>) reply).getData()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(artifact.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        }
        String base = request.getParameter("base");
        if (base != null) {
            // get all artifacts for an base
            Reply reply = getArtifactsForBase(base);
            if (!reply.isSuccess())
                return ReplyUtils.toHttpResponse(reply);
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (Artifact artifact : ((ReplyResultCollection<Artifact>) reply).getData()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(artifact.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else {
            Reply reply = getAllArtifacts();
            if (!reply.isSuccess())
                return ReplyUtils.toHttpResponse(reply);
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (Artifact artifact : ((ReplyResultCollection<Artifact>) reply).getData()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(artifact.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        }
    }

    /**
     * Handles the request for the /artifacts/live resource
     *
     * @return The response
     */
    private HttpResponse handleArtifactsLive() {
        Reply reply = getLiveArtifacts();
        if (!reply.isSuccess())
            return ReplyUtils.toHttpResponse(reply);
        boolean first = true;
        StringBuilder builder = new StringBuilder("[");
        for (Artifact artifact : ((ReplyResultCollection<Artifact>) reply).getData()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(artifact.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Handles the request for the /artifacts/diff resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleArtifactsDiff(HttpApiRequest request) {
        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
        // diff artifacts left and right
        String left = request.getParameter("left");
        String right = request.getParameter("right");
        if (left == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'left'"));
        if (right == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'right'"));
        return onMessageDiffArtifacts(left, right, request.getHeader(HttpConstants.HEADER_ACCEPT));
    }

    /**
     * Handles the request for a specific artifact /artifacts/{artifactId}
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleArtifact(HttpApiRequest request) {
        String rest = request.getUri().substring(apiUri.length() + "/artifacts".length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String artifactId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
        if (index < 0) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    Reply reply = retrieve(artifactId);
                    if (!reply.isSuccess())
                        return ReplyUtils.toHttpResponse(reply);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, ((ReplyResult<Artifact>) reply).getData().serializedJSON());
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
                    return onMessageGetArtifactMetadata(artifactId, request.getHeader(HttpConstants.HEADER_ACCEPT));
                }
                case "/content": {
                    if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                    return onMessageGetArtifactContent(artifactId, request.getHeader(HttpConstants.HEADER_ACCEPT));
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
        return null;
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
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT));
        String[] accept = request.getHeader(HttpConstants.HEADER_ACCEPT);
        String sparql = new String(request.getContent(), IOUtils.CHARSET);

        String storeId = request.getParameter("store");
        XOWLFederationStore store = storeLive;
        if (storeId != null) {
            if (STORE_ID_LIVE.equals(storeId)) {
                store = storeLive;
            } else if (STORE_ID_LONG_TERM.equals(storeId)) {
                store = storeLongTerm;
            } else if (STORE_ID_SERVICE.equals(storeId)) {
                store = storeService;
            }
        }

        Reply reply = store.sparql(sparql, null, null);
        return XOWLReplyUtils.toHttpResponse(reply, Arrays.asList(accept));
    }

    /**
     * Responds to a request for the header of a specified artifact
     *
     * @param artifactId The identifier of an artifact
     * @param accept     The request's accepted content types
     * @return The response
     */
    private HttpResponse onMessageGetArtifactMetadata(String artifactId, String[] accept) {
        Reply reply = retrieve(artifactId);
        if (!reply.isSuccess())
            return ReplyUtils.toHttpResponse(reply);
        Artifact artifact = ((ReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to retrieve the artifact"));

        BufferedLogger logger = new BufferedLogger();
        String contentType = RDFUtils.coerceContentTypeQuads(Arrays.asList(accept));
        String content = RDFUtils.serialize(logger, artifact.getMetadata().iterator(), contentType);
        if (!logger.getErrorMessages().isEmpty())
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, logger.getErrorsAsString()));
        return new HttpResponse(HttpURLConnection.HTTP_OK, contentType, content);
    }

    /**
     * Responds to a request for the content of a specified artifact
     *
     * @param artifactId The identifier of an artifact
     * @param accept     The request's accepted content types
     * @return The artifact
     */
    private HttpResponse onMessageGetArtifactContent(String artifactId, String[] accept) {
        Reply reply = retrieve(artifactId);
        if (!reply.isSuccess())
            return ReplyUtils.toHttpResponse(reply);
        Artifact artifact = ((ReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to retrieve the artifact"));
        Collection<Quad> quads = artifact.getContent();
        if (quads == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to retrieve the artifact's content"));

        BufferedLogger logger = new BufferedLogger();
        String contentType = RDFUtils.coerceContentTypeQuads(Arrays.asList(accept));
        String content = RDFUtils.serialize(logger, quads.iterator(), contentType);
        if (!logger.getErrorMessages().isEmpty())
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, logger.getErrorsAsString()));
        return new HttpResponse(HttpURLConnection.HTTP_OK, contentType, content);
    }

    /**
     * Responds to a request to delete an artifact
     *
     * @param artifactId The identifier of an artifact
     * @return The response
     */
    private HttpResponse onMessageDeleteArtifact(String artifactId) {
        JobExecutionService executor = Register.getComponent(JobExecutionService.class);
        if (executor == null)
            return ReplyUtils.toHttpResponse(ReplyServiceUnavailable.instance());
        Job job = new DeleteArtifactJob(artifactId);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }

    /**
     * Responds to a request for the computation of the diff between two artifacts
     *
     * @param artifactLeft  The identifier of the artifact on the left
     * @param artifactRight The identifier of the artifact on the right
     * @param accept        The request's accepted content types
     * @return The artifact
     */
    private HttpResponse onMessageDiffArtifacts(String artifactLeft, String artifactRight, String[] accept) {
        Reply reply = retrieve(artifactLeft);
        if (!reply.isSuccess())
            return ReplyUtils.toHttpResponse(reply);
        Artifact artifact = ((ReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to retrieve the artifact"));
        Collection<Quad> contentLeft = artifact.getContent();
        if (contentLeft == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to retrieve the content of the artifact"));

        reply = retrieve(artifactRight);
        if (!reply.isSuccess())
            return ReplyUtils.toHttpResponse(reply);
        artifact = ((ReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to retrieve the artifact"));
        Collection<Quad> contentRight = artifact.getContent();
        if (contentRight == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to retrieve the content of the artifact"));

        Changeset changeset = RDFUtils.diff(contentLeft, contentRight, true);

        BufferedLogger logger = new BufferedLogger();
        String contentType = RDFUtils.coerceContentTypeQuads(Arrays.asList(accept));
        StringWriter writer = new StringWriter();
        writer.write("--" + HttpConstants.MULTIPART_BOUNDARY + IOUtils.LINE_SEPARATOR);
        writer.write("Content-Type: " + contentType + IOUtils.LINE_SEPARATOR);
        RDFUtils.serialize(writer, logger, changeset.getAdded().iterator(), contentType);
        writer.write(IOUtils.LINE_SEPARATOR);
        writer.write("--" + HttpConstants.MULTIPART_BOUNDARY + IOUtils.LINE_SEPARATOR);
        writer.write("Content-Type: " + contentType + IOUtils.LINE_SEPARATOR);
        RDFUtils.serialize(writer, logger, changeset.getRemoved().iterator(), contentType);

        if (!logger.getErrorMessages().isEmpty())
            return ReplyUtils.toHttpResponse(new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, logger.getErrorsAsString()));
        return new HttpResponse(
                HttpURLConnection.HTTP_OK,
                HttpConstants.MIME_MULTIPART_MIXED + "; boundary=" + HttpConstants.MULTIPART_BOUNDARY,
                writer.toString());
    }

    /**
     * Responds to the request to pull an artifact from the live store
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param artifactId The identifier of an artifact
     * @return The response
     */
    private HttpResponse onMessagePullFromLive(String artifactId) {
        JobExecutionService executor = Register.getComponent(JobExecutionService.class);
        if (executor == null)
            return ReplyUtils.toHttpResponse(ReplyServiceUnavailable.instance());
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
        JobExecutionService executor = Register.getComponent(JobExecutionService.class);
        if (executor == null)
            return ReplyUtils.toHttpResponse(ReplyServiceUnavailable.instance());
        Job job = new PushArtifactToLiveJob(artifactId);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }
}
