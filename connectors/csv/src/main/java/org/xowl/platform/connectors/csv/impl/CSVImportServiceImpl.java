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

import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.Serializable;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.platform.connectors.csv.CSVImportDocument;
import org.xowl.platform.connectors.csv.CSVImportMapping;
import org.xowl.platform.connectors.csv.CSVImportService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;

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
    public XSPReply getFirstLines(String documentId, char separator, char textMarker) {
        CSVImportDocument document = documents.get(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();
        Serializable data = document.getFirstLines(separator, textMarker);
        return new XSPReplyResult<>(data);
    }

    @Override
    public XSPReply upload(String name, String base, String[] supersede, String version, String archetype, byte[] content) {
        CSVImportDocument document = new CSVImportDocument(name, base, supersede, version, archetype, content);
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
    public XSPReply importDocument(String documentId, CSVImportMapping mapping, char separator, char textMarker, boolean skipFirstRow) {
        CSVImportDocument document = documents.get(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();
        ArtifactStorageService storageService = ServiceUtils.getService(ArtifactStorageService.class);
        if (storageService == null)
            return XSPReplyServiceUnavailable.instance();
        Artifact artifact = document.buildArtifact(getIdentifier(), mapping, separator, textMarker, skipFirstRow);
        XSPReply reply = storageService.store(artifact);
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(artifact);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        return null;
    }
}
