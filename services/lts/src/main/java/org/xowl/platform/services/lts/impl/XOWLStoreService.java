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

import org.xowl.infra.server.api.XOWLDatabase;
import org.xowl.infra.server.api.XOWLServer;
import org.xowl.infra.server.api.remote.RemoteServer;
import org.xowl.infra.server.base.ServerConfiguration;
import org.xowl.infra.server.embedded.EmbeddedServer;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.AbstractRepository;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.RDFUtils;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.store.rdf.Changeset;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultFailure;
import org.xowl.infra.store.sparql.ResultQuads;
import org.xowl.infra.store.sparql.ResultUtils;
import org.xowl.infra.store.writers.NQuadsSerializer;
import org.xowl.infra.store.writers.RDFSerializer;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;
import org.xowl.platform.services.lts.jobs.DeleteArtifactJob;
import org.xowl.platform.services.lts.jobs.PullArtifactFromLiveJob;
import org.xowl.platform.services.lts.jobs.PushArtifactToLiveJob;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Implements a triple store service that is backed by a xOWL Server
 *
 * @author Laurent Wouters
 */
public class XOWLStoreService implements TripleStoreService, ArtifactStorageService, HttpAPIService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/core/sparql",
            "services/core/artifacts"
    };


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
    public XOWLStoreService() {
        ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
        Configuration configuration = configurationService.getConfigFor(this);
        this.server = resolveServer(configuration);
        this.storeLive = new XOWLFederationStore(configuration.get("databases", "live")) {
            @Override
            protected XOWLDatabase resolveBackend() {
                return XOWLStoreService.this.resolveRemote(this.getName());
            }
        };
        this.storeLongTerm = new XOWLFederationStore(configuration.get("databases", "longTerm")) {
            @Override
            protected XOWLDatabase resolveBackend() {
                return XOWLStoreService.this.resolveRemote(this.getName());
            }
        };
        this.storeService = new XOWLFederationStore(configuration.get("databases", "service")) {
            @Override
            protected XOWLDatabase resolveBackend() {
                return XOWLStoreService.this.resolveRemote(this.getName());
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
                XOWLServer server = new EmbeddedServer(new ServerConfiguration(location));
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
                return null;
            } catch (IOException exception) {
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
        return XOWLStoreService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Triple Store Service";
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
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
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
            return new XSPReplyFailure(((ResultFailure) sparqlResult).getMessage());
        return new XSPReplyResultCollection<>(storeLongTerm.buildArtifacts(((ResultQuads) sparqlResult).getQuads()));
    }

    @Override
    public XSPReply getArtifactsForArchetype(String archetype) {
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write(">. ?a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARCHETYPE));
        writer.write("> \"");
        writer.write(IOUtils.escapeAbsoluteURIW3C(archetype));
        writer.write("\" } }");

        Result sparqlResult = storeLongTerm.sparql(writer.toString());
        if (sparqlResult.isFailure())
            return new XSPReplyFailure(((ResultFailure) sparqlResult).getMessage());
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
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (uri.equals("services/core/sparql"))
            return onMessageSPARQL(content, accept);
        if (uri.equals("services/core/artifacts")) {
            // is it an action
            String[] actions = parameters.get("action");
            String action = actions != null && actions.length >= 1 ? actions[0] : null;
            if (action != null && action.equals("delete"))
                return onMessageDeleteArtifact(parameters);
            if (action != null && action.equals("pull"))
                return onMessagePullFromLive(parameters);
            if (action != null && action.equals("push"))
                return onMessagePushToLive(parameters);
            // not an action, is it a specific artifact?
            String[] ids = parameters.get("id");
            String id = (ids != null && ids.length > 0) ? ids[0] : null;
            if (id != null) {
                // yes, request the content of the just the header?
                String[] quads = parameters.get("quads");
                if (quads == null || quads.length == 0)
                    return XSPReplyUtils.toHttpResponse(retrieve(id), null);
                if (quads[0].equals("metadata"))
                    return onMessageGetArtifactMetadata(id);
                else if (quads[0].equals("content"))
                    return onMessageGetArtifactContent(id);
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            }
            // no, is it a diff?
            String[] diffLefts = parameters.get("diffLeft");
            String[] diffRights = parameters.get("diffRight");
            if (diffLefts != null && diffRights != null && diffLefts.length > 0 && diffRights.length > 0)
                return onMessageDiffArtifacts(diffLefts[0], diffRights[0]);
            // no, request a set of artifacts
            String[] lives = parameters.get("live");
            String[] bases = parameters.get("base");
            String[] archetypes = parameters.get("archetype");
            if (lives != null && lives.length > 0)
                return XSPReplyUtils.toHttpResponse(getLiveArtifacts(), null);
            else if (bases != null && bases.length > 0)
                return XSPReplyUtils.toHttpResponse(getArtifactsForBase(bases[0]), null);
            else if (archetypes != null && archetypes.length > 0)
                return XSPReplyUtils.toHttpResponse(getArtifactsForArchetype(archetypes[0]), null);
            else
                return XSPReplyUtils.toHttpResponse(getAllArtifacts(), null);
        }
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
        String request = new String(content, Files.CHARSET);
        Result result = storeLive.sparql(request);
        String resultType = ResultUtils.coerceContentType(result, accept != null ? IOUtils.httpNegotiateContentType(Collections.singletonList(accept)) : AbstractRepository.SYNTAX_NQUADS);
        StringWriter writer = new StringWriter();
        try {
            result.print(writer, resultType);
        } catch (IOException exception) {
            // cannot happen
        }
        return new HttpResponse(result.isSuccess() ? HttpURLConnection.HTTP_OK : HttpConstants.HTTP_UNKNOWN_ERROR, resultType, writer.toString());
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
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, "Failed to retrieve the artifact");
        BufferedLogger logger = new BufferedLogger();
        StringWriter writer = new StringWriter();
        RDFSerializer serializer = new NQuadsSerializer(writer);
        serializer.serialize(logger, artifact.getMetadata().iterator());
        if (!logger.getErrorMessages().isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, logger.getErrorsAsString());
        return new HttpResponse(HttpURLConnection.HTTP_OK, AbstractRepository.SYNTAX_NQUADS, writer.toString());
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
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, "Failed to retrieve the artifact");
        Collection<Quad> content = artifact.getContent();
        if (content == null)
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, "Failed to retrieve the content of the artifact");
        BufferedLogger logger = new BufferedLogger();
        StringWriter writer = new StringWriter();
        RDFSerializer serializer = new NQuadsSerializer(writer);
        serializer.serialize(logger, content.iterator());
        if (!logger.getErrorMessages().isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, logger.getErrorsAsString());
        return new HttpResponse(HttpURLConnection.HTTP_OK, AbstractRepository.SYNTAX_NQUADS, writer.toString());
    }

    /**
     * Responds to a request to delete an artifact
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onMessageDeleteArtifact(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected an id parameter");
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        Job job = new DeleteArtifactJob(ids[0]);
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
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, "Failed to retrieve the artifact");
        Collection<Quad> contentLeft = artifact.getContent();
        if (contentLeft == null)
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, "Failed to retrieve the content of the artifact");

        reply = retrieve(artifactRight);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        artifact = ((XSPReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, "Failed to retrieve the artifact");
        Collection<Quad> contentRight = artifact.getContent();
        if (contentRight == null)
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, "Failed to retrieve the content of the artifact");

        Changeset changeset = RDFUtils.diff(contentLeft, contentRight, true);
        BufferedLogger logger = new BufferedLogger();
        StringWriter writer = new StringWriter();
        RDFSerializer serializer = new NQuadsSerializer(writer);
        writer.write("--xowlQuads" + Files.LINE_SEPARATOR);
        serializer.serialize(logger, changeset.getAdded().iterator());
        writer.write("--xowlQuads" + Files.LINE_SEPARATOR);
        serializer.serialize(logger, changeset.getRemoved().iterator());
        if (!logger.getErrorMessages().isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, logger.getErrorsAsString());
        return new HttpResponse(HttpURLConnection.HTTP_OK, AbstractRepository.SYNTAX_NQUADS, writer.toString());
    }

    /**
     * Responds to the request to pull an artifact from the live store
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onMessagePullFromLive(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected an id parameter");
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        Job job = new PullArtifactFromLiveJob(ids[0]);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }

    /**
     * Responds to the request to push an artifact to the live store
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onMessagePushToLive(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected an id parameter");
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        Job job = new PushArtifactToLiveJob(ids[0]);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }
}
