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
import org.xowl.platform.services.lts.jobs.PullArtifactFromLiveJob;
import org.xowl.platform.services.lts.jobs.PushArtifactToLiveJob;
import org.xowl.platform.utils.Utils;
import org.xowl.store.AbstractRepository;
import org.xowl.store.IOUtils;
import org.xowl.store.RDFUtils;
import org.xowl.store.rdf.Changeset;
import org.xowl.store.rdf.Quad;
import org.xowl.store.sparql.Result;
import org.xowl.store.sparql.ResultFailure;
import org.xowl.store.sparql.ResultQuads;
import org.xowl.store.sparql.ResultUtils;
import org.xowl.store.writers.NQuadsSerializer;
import org.xowl.store.writers.RDFSerializer;
import org.xowl.store.xsp.XSPReply;
import org.xowl.store.xsp.XSPReplyFailure;
import org.xowl.store.xsp.XSPReplyResult;
import org.xowl.store.xsp.XSPReplyUtils;
import org.xowl.utils.Files;
import org.xowl.utils.logging.BufferedLogger;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements a triple store service that is backed by a remote store connected to via HTTP
 *
 * @author Laurent Wouters
 */
public class RemoteXOWLStoreService implements TripleStoreService, ArtifactStorageService, HttpAPIService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "sparql",
            "artifacts"
    };


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
            return new XSPReplyFailure("No matching artifact");
        return new XSPReplyResult<>(storeLongTerm.buildArtifact(metadata));
    }

    @Override
    public Collection<Artifact> list() {
        return storeLongTerm.getArtifacts();
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
        return storeLongTerm.buildArtifacts(((ResultQuads) sparqlResult).getQuads());
    }

    @Override
    public Collection<Artifact> getAllLive() {
        StringWriter writer = new StringWriter();
        writer.write("DESCRIBE ?a WHERE { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ?a a <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT));
        writer.write("> } }");

        Result sparqlResult = storeLive.sparql(writer.toString());
        if (sparqlResult.isFailure())
            return new ArrayList<>();
        return storeLive.buildArtifacts(((ResultQuads) sparqlResult).getQuads());
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
    public IOUtils.HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (uri.equals(URI_API + "/sparql"))
            return onMessageSPARQL(content, accept);
        if (uri.equals(URI_API + "/artifacts")) {
            // is it an action
            String[] actions = parameters.get("action");
            String action = actions != null && actions.length >= 1 ? actions[0] : null;
            if (action != null && action.equals("pull"))
                return onMessagePullFromLive(parameters);
            if (action != null && action.equals("push"))
                return onMessagePushToLive(parameters);
            // not an action, is it a specific artifact?
            String[] ids = parameters.get("id");
            String id = (ids != null && ids.length > 0) ? ids[0] : null;
            if (id != null) {
                // yes, request the content of the just the header?
                String[] contents = parameters.get("content");
                boolean isContent = (contents != null && contents.length > 0 && contents[0].equalsIgnoreCase("true"));
                if (isContent)
                    return onMessageGetArtifactContent(id);
                return onMessageGetArtifactMetadata(id);
            }
            // no, is it a diff?
            String[] diffLefts = parameters.get("diffLeft");
            String[] diffRights = parameters.get("diffRight");
            if (diffLefts != null && diffRights != null && diffLefts.length > 0 && diffRights.length > 0)
                return onMessageDiffArtifacts(diffLefts[0], diffRights[0]);
            // no, request a set of artifacts
            String[] lives = parameters.get("live");
            String[] bases = parameters.get("base");
            boolean live = (lives != null && lives.length > 0 && lives[0].equalsIgnoreCase("true"));
            String base = (bases != null && bases.length > 0) ? bases[0] : null;
            if (base != null)
                return onMessageGetArtifacts(base);
            return live ? onMessageGetLiveArtifacts() : onMessageGetArtifacts();
        }
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Responds to a SPARQL command
     *
     * @param content The SPARQL content
     * @param accept  The accept HTTP header
     * @return The response
     */
    private IOUtils.HttpResponse onMessageSPARQL(byte[] content, String accept) {
        if (content == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        String request = new String(content, Utils.DEFAULT_CHARSET);
        Result result = storeLive.sparql(request);
        String resultType = ResultUtils.coerceContentType(result, accept != null ? IOUtils.httpNegotiateContentType(Collections.singletonList(accept)) : AbstractRepository.SYNTAX_NQUADS);
        StringWriter writer = new StringWriter();
        try {
            result.print(writer, resultType);
        } catch (IOException exception) {
            // cannot happen
        }
        return new IOUtils.HttpResponse(result.isSuccess() ? HttpURLConnection.HTTP_OK : IOUtils.HTTP_UNKNOWN_ERROR, resultType, writer.toString());
    }

    /**
     * Responds to a request to list the artifacts
     *
     * @return The response
     */
    private IOUtils.HttpResponse onMessageGetArtifacts() {
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
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request to list the artifacts for a specified base
     *
     * @param base The base to look for
     * @return The response
     */
    private IOUtils.HttpResponse onMessageGetArtifacts(String base) {
        Collection<Artifact> artifacts = list(base);
        boolean first = true;
        StringBuilder builder = new StringBuilder("[");
        for (Artifact artifact : artifacts) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(artifact.serializedJSON());
        }
        builder.append("]");
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request to list the live artifacts
     *
     * @return The response
     */
    private IOUtils.HttpResponse onMessageGetLiveArtifacts() {
        Collection<Artifact> artifacts = getAllLive();
        boolean first = true;
        StringBuilder builder = new StringBuilder("[");
        for (Artifact artifact : artifacts) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(artifact.serializedJSON());
        }
        builder.append("]");
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for the header of a specified artifact
     *
     * @param artifactId The identifier of an artifact
     * @return The response
     */
    private IOUtils.HttpResponse onMessageGetArtifactMetadata(String artifactId) {
        XSPReply reply = retrieve(artifactId);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        Artifact artifact = ((XSPReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, "Failed to retrieve the artifact");
        BufferedLogger logger = new BufferedLogger();
        StringWriter writer = new StringWriter();
        RDFSerializer serializer = new NQuadsSerializer(writer);
        serializer.serialize(logger, artifact.getMetadata().iterator());
        if (!logger.getErrorMessages().isEmpty())
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, Utils.getLog(logger));
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_OK, AbstractRepository.SYNTAX_NQUADS, writer.toString());
    }

    /**
     * Responds to a request for the content of a specified artifact
     *
     * @param artifactId The identifier of an artifact
     * @return The artifact
     */
    private IOUtils.HttpResponse onMessageGetArtifactContent(String artifactId) {
        XSPReply reply = retrieve(artifactId);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        Artifact artifact = ((XSPReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, "Failed to retrieve the artifact");
        Collection<Quad> content = artifact.getContent();
        if (content == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, "Failed to retrieve the content of the artifact");
        BufferedLogger logger = new BufferedLogger();
        StringWriter writer = new StringWriter();
        RDFSerializer serializer = new NQuadsSerializer(writer);
        serializer.serialize(logger, content.iterator());
        if (!logger.getErrorMessages().isEmpty())
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, Utils.getLog(logger));
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_OK, AbstractRepository.SYNTAX_NQUADS, writer.toString());
    }

    /**
     * Responds to a request for the computation of the diff between two artifacts
     *
     * @param artifactLeft  The identifier of the artifact on the left
     * @param artifactRight The identifier of the artifact on the right
     * @return The artifact
     */
    private IOUtils.HttpResponse onMessageDiffArtifacts(String artifactLeft, String artifactRight) {
        XSPReply reply = retrieve(artifactLeft);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        Artifact artifact = ((XSPReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, "Failed to retrieve the artifact");
        Collection<Quad> contentLeft = artifact.getContent();
        if (contentLeft == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, "Failed to retrieve the content of the artifact");

        reply = retrieve(artifactRight);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        artifact = ((XSPReplyResult<Artifact>) reply).getData();
        if (artifact == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, "Failed to retrieve the artifact");
        Collection<Quad> contentRight = artifact.getContent();
        if (contentRight == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, "Failed to retrieve the content of the artifact");

        Changeset changeset = RDFUtils.diff(contentLeft, contentRight, true);
        BufferedLogger logger = new BufferedLogger();
        StringWriter writer = new StringWriter();
        RDFSerializer serializer = new NQuadsSerializer(writer);
        writer.write("--xowlQuads" + Files.LINE_SEPARATOR);
        serializer.serialize(logger, changeset.getAdded().iterator());
        writer.write("--xowlQuads" + Files.LINE_SEPARATOR);
        serializer.serialize(logger, changeset.getRemoved().iterator());
        if (!logger.getErrorMessages().isEmpty())
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, Utils.getLog(logger));
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_OK, AbstractRepository.SYNTAX_NQUADS, writer.toString());
    }

    /**
     * Responds to the request to pull an artifact from the live store
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param parameters The request parameters
     * @return The response
     */
    private IOUtils.HttpResponse onMessagePullFromLive(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Expected an id parameter");
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Could not find the job execution service");
        Job job = new PullArtifactFromLiveJob(ids[0]);
        executor.schedule(job);
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, job.serializedJSON());
    }

    /**
     * Responds to the request to push an artifact to the live store
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param parameters The request parameters
     * @return The response
     */
    private IOUtils.HttpResponse onMessagePushToLive(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Expected an id parameter");
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Could not find the job execution service");
        Job job = new PushArtifactToLiveJob(ids[0]);
        executor.schedule(job);
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, job.serializedJSON());
    }
}