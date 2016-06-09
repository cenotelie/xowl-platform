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

package org.xowl.platform.connectors.csv.impl;

import org.xowl.hime.redist.ParseError;
import org.xowl.hime.redist.ParseResult;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.Serializable;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.DispatchLogger;
import org.xowl.infra.utils.logging.Logger;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.connectors.csv.CSVImportDocument;
import org.xowl.platform.connectors.csv.CSVImportMapping;
import org.xowl.platform.connectors.csv.CSVImportService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.artifacts.FreeArtifactArchetype;
import org.xowl.platform.services.connection.ConnectorServiceBase;

import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements of the CSV import service
 *
 * @author Laurent Wouters
 */
public class CSVImportServiceImpl implements CSVImportService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/import/csv"
    };

    /**
     * The uploaded documents pending importation
     */
    private final Map<String, CSVImportDocument> documents;

    /**
     * Initializes this service
     */
    public CSVImportServiceImpl() {
        this.documents = new HashMap<>();
    }

    @Override
    public String getIdentifier() {
        return CSVImportServiceImpl.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Import Service - CSV";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public XSPReply getDocuments() {
        return new XSPReplyResultCollection<>(documents.values());
    }

    @Override
    public XSPReply getDocument(String documentId) {
        CSVImportDocument document = documents.get(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();
        return new XSPReplyResult<>(document);
    }

    @Override
    public XSPReply getFirstLines(String documentId, char separator, char textMarker, int rowCount) {
        CSVImportDocument document = documents.get(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();
        Serializable data = document.getFirstLines(separator, textMarker, rowCount);
        return new XSPReplyResult<>(data);
    }

    @Override
    public XSPReply upload(String name, byte[] content) {
        CSVImportDocument document = new CSVImportDocument(name, content);
        documents.put(document.getIdentifier(), document);
        return new XSPReplyResult<>(document);
    }

    @Override
    public XSPReply drop(String documentId) {
        CSVImportDocument document = documents.get(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply importDocument(String documentId, CSVImportMapping mapping, char separator, char textMarker, boolean skipFirstRow, String base, String[] supersede, String version, String archetype) {
        CSVImportDocument document = documents.get(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();
        ArtifactStorageService storageService = ServiceUtils.getService(ArtifactStorageService.class);
        if (storageService == null)
            return XSPReplyServiceUnavailable.instance();
        Collection<Quad> quads = document.map(mapping, separator, textMarker, skipFirstRow);
        Collection<Quad> metadata = ConnectorServiceBase.buildMetadata(documentId, base, supersede, document.getName(), version, archetype, getIdentifier());
        Artifact artifact = new ArtifactSimple(metadata, quads);
        XSPReply reply = storageService.store(artifact);
        if (!reply.isSuccess())
            return reply;
        documents.remove(documentId);
        return new XSPReplyResult<>(artifact);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        switch (method) {
            case "GET": {
                String[] docIds = parameters.get("document");
                if (docIds == null)
                    return XSPReplyUtils.toHttpResponse(getDocuments(), null);
                String[] separators = parameters.get("separator");
                String[] textMarkers = parameters.get("textMarker");
                String[] rowCounts = parameters.get("rowCount");
                if (separators != null && textMarkers != null && rowCounts != null && separators.length > 0 && textMarkers.length > 0 && rowCounts.length > 0)
                    return XSPReplyUtils.toHttpResponse(getFirstLines(docIds[0], separators[0].charAt(0), textMarkers[0].charAt(0), Integer.parseInt(rowCounts[0])), null);
                return XSPReplyUtils.toHttpResponse(getDocument(docIds[0]), null);
            }
            case "PUT": {
                String[] names = parameters.get("name");
                if (names != null && names.length > 0)
                    return XSPReplyUtils.toHttpResponse(upload(names[0], content), null);
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            }
            case "POST": {
                String[] drops = parameters.get("drop");
                if (drops != null && drops.length > 0)
                    return XSPReplyUtils.toHttpResponse(drop(drops[0]), null);

                String[] imports = parameters.get("import");
                String[] separators = parameters.get("separator");
                String[] textMarkers = parameters.get("textMarker");
                String[] skipFirsts = parameters.get("skipFirst");
                String[] bases = parameters.get("base");
                String[] supersedes = parameters.get("supersede");
                String[] versions = parameters.get("version");
                String[] archetypes = parameters.get("archetype");
                if (archetypes == null || archetypes.length <= 0)
                    archetypes = new String[]{FreeArtifactArchetype.INSTANCE.getIdentifier()};

                NodeManager nodeManager = new CachedNodes();
                JSONLDLoader loader = new JSONLDLoader(nodeManager) {
                    @Override
                    protected Reader getReaderFor(Logger logger, String iri) {
                        return null;
                    }
                };
                BufferedLogger bufferedLogger = new BufferedLogger();
                DispatchLogger dispatchLogger = new DispatchLogger(Logging.getDefault(), bufferedLogger);
                ParseResult parseResult = loader.parse(dispatchLogger, new StringReader(new String(content, Files.CHARSET)));
                if (parseResult == null || !parseResult.isSuccess()) {
                    dispatchLogger.error("Failed to parse the response");
                    if (parseResult != null) {
                        for (ParseError error : parseResult.getErrors()) {
                            dispatchLogger.error(error);
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    for (Object error : bufferedLogger.getErrorMessages()) {
                        builder.append(error.toString());
                        builder.append("\n");
                    }
                    return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_JSON, builder.toString());
                }
                CSVImportMapping mapping = new CSVImportMapping(parseResult.getRoot());
                XSPReply reply = importDocument(imports[0], mapping, separators[0].charAt(0), textMarkers[0].charAt(0), skipFirsts[0].equalsIgnoreCase("true"), bases[0], supersedes, versions[0], archetypes[0]);
                return XSPReplyUtils.toHttpResponse(reply, null);
            }
            default:
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD);
        }
    }
}
