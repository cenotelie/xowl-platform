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
import org.xowl.platform.kernel.ArtifactStorageService;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.platform.utils.Utils;
import org.xowl.store.IOUtils;
import org.xowl.store.rdf.Quad;
import org.xowl.store.sparql.Result;
import org.xowl.store.sparql.ResultQuads;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Implements a triple store service that is backed by a remote store connected to via HTTP
 *
 * @author Laurent Wouters
 */
public class RemoteXOWLStoreService implements TripleStoreService, ArtifactStorageService, HttpAPIService {
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
        return storeLongTerm.store(artifact);
    }

    @Override
    public Artifact retrieve(String identifier) {
        return storeLongTerm.retrieve(identifier);
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
        return storeLongTerm.buildArtifact(metadata);
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
    public boolean pushToLive(Artifact artifact) {
        return storeLive.store(artifact);
    }

    @Override
    public boolean pullFromLive(Artifact artifact) {
        return storeLive.delete(artifact.getIdentifier());
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
}
