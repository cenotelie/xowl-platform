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

package org.xowl.platform.services.importation.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.services.importation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the importation service for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLImportationService implements ImportationService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/core/importation"
    };

    /**
     * The current documents pending importation
     */
    private final Map<String, Document> documents;
    /**
     * The directory for the persistent storage of the documents
     */
    private File storage;

    /**
     * Initializes this service
     */
    public XOWLImportationService() {
        this.documents = new HashMap<>();
    }

    /**
     * When the service is activated
     */
    private void onActivated() {
        if (storage == null) {
            ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
            Configuration configuration = configurationService != null ? configurationService.getConfigFor(this) : null;
            if (configuration != null) {
                String value = configuration.get("storage");
                if (value != null)
                    storage = new File(value);
                else
                    storage = new File(System.getProperty("user.dir"));
            }
            reloadDocuments();
        }
    }

    /**
     * Tries to reload the documents
     */
    private void reloadDocuments() {
        if (!storage.exists())
            return;
        File[] files = storage.listFiles();
        if (files != null) {
            for (int i = 0; i != files.length; i++) {
                if (isDocDescriptorFile(files[i].getName())) {
                    try (Reader reader = Files.getReader(files[i].getAbsolutePath())) {
                        String content = Files.read(reader);
                        reloadDocument(files[i].getAbsolutePath(), content);
                    } catch (IOException exception) {
                        Logging.getDefault().error(exception);
                    }
                }
            }
        }
    }

    /**
     * Tries to reload a document description
     *
     * @param file    The name of the file
     * @param content The descriptor content
     */
    private void reloadDocument(String file, String content) {
        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
        if (definition == null) {
            Logging.getDefault().error("Failed to parse the job " + file);
            return;
        }
        Document document = new Document(definition);
        documents.put(document.getIdentifier(), document);
    }


    @Override
    public String getIdentifier() {
        return XOWLImportationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Importation Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        switch (method) {
            case "GET": {
                String[] docIds = parameters.get("document");
                if (docIds != null && docIds.length > 0)
                    return onGetDocument(docIds[0]);
                String[] importerIds = parameters.get("importer");
                if (importerIds != null && importerIds.length > 0)
                    return onGetImporter(importerIds[0]);
                String[] whats = parameters.get("what");
                if (whats != null && whats.length > 0) {
                    if (whats[0].equals("document"))
                        return onGetDocuments();
                    else if (whats[0].equals("importer"))
                        return onGetImporters();
                }
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            }
            case "PUT": {
                String[] names = parameters.get("name");
                if (names != null && names.length > 0)
                    return onPutDocument(names[0], content);
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            }
            case "POST": {
                String[] drops = parameters.get("drop");
                if (drops != null && drops.length > 0)
                    return onPostDropDocument(drops[0]);
                String[] previews = parameters.get("preview");
                String[] importers = parameters.get("importer");
                if (previews != null && previews.length > 0 && importers != null && importers.length > 0)
                    return onGetPreview(previews[0], importers[0], new String(content, Files.CHARSET));
                String[] imports = parameters.get("import");
                if (imports != null && imports.length > 0 && importers != null && importers.length > 0)
                    return onBeginImport(imports[0], importers[0], new String(content, Files.CHARSET));
                return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
            }
            default:
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD);
        }
    }

    /**
     * When the documents are requested
     *
     * @return The HTTP response
     */
    private HttpResponse onGetDocuments() {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Document document : getDocuments()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(document.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * When a single document is requested
     *
     * @param documentId The identifier of the document
     * @return The HTTP response
     */
    private HttpResponse onGetDocument(String documentId) {
        Document document = getDocument(documentId);
        if (document == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, document.serializedJSON());
    }

    /**
     * When a new document is uploaded
     *
     * @param name    The document 's name
     * @param content The document's content
     * @return The document
     */
    private HttpResponse onPutDocument(String name, byte[] content) {
        Document document = upload(name, content);
        if (document == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, document.serializedJSON());
    }

    /**
     * When a drop request for a document is received
     *
     * @param documentId The identifier of the document
     * @return The HTTP response
     */
    private HttpResponse onPostDropDocument(String documentId) {
        Document document = getDocument(documentId);
        if (document == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        drop(document);
        return new HttpResponse(HttpURLConnection.HTTP_OK);
    }

    /**
     * When the preview of a document is requested
     *
     * @param documentId    The identifier of the document
     * @param importerId    The identifier of the importer to use
     * @param configuration The importer's configuration
     * @return The HTTP response
     */
    private HttpResponse onGetPreview(String documentId, String importerId, String configuration) {
        Document document = getDocument(documentId);
        if (document == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        Collection<Importer> importers = ServiceUtils.getServices(Importer.class);
        for (Importer importer : importers) {
            if (importer.getIdentifier().equals(importerId)) {
                DocumentPreview preview = getPreview(document, importer, importer.getConfiguration(configuration));
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, preview.serializedJSON());
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * When the registered importers are requested
     *
     * @return The HTTP response
     */
    private HttpResponse onGetImporters() {
        Collection<Importer> importers = ServiceUtils.getServices(Importer.class);
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Importer importer : importers) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(importer.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * When an importer is requested
     *
     * @param importerId The identifier of the requested importer
     * @return The HTTP response
     */
    private HttpResponse onGetImporter(String importerId) {
        Importer importer = getImporter(importerId);
        if (importer == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, importer.serializedJSON());
    }

    /**
     * When the importation of a document is requested
     *
     * @param documentId    The identifier of the document
     * @param importerId    The identifier of the importer to use
     * @param configuration The importer's configuration
     * @return The HTTP response
     */
    private HttpResponse onBeginImport(String documentId, String importerId, String configuration) {
        Document document = getDocument(documentId);
        if (document == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        Collection<Importer> importers = ServiceUtils.getServices(Importer.class);
        for (Importer importer : importers) {
            if (importer.getIdentifier().equals(importerId)) {
                Job job = beginImport(document, importer, importer.getConfiguration(configuration));
                if (job == null)
                    return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpConstants.MIME_TEXT_PLAIN, "Failed to import");
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Override
    public Collection<Document> getDocuments() {
        onActivated();
        return documents.values();
    }

    @Override
    public Document getDocument(String documentId) {
        onActivated();
        return documents.get(documentId);
    }

    @Override
    public Document upload(String name, byte[] content) {
        onActivated();
        if (storage == null)
            return null;
        if (!storage.exists() && !storage.mkdirs())
            return null;
        Document document = new Document(name);
        File fileDescriptor = new File(storage, getDocDescriptorFile(document));
        File fileContent = new File(storage, getDocContentFile(document));
        try (FileOutputStream stream = new FileOutputStream(fileDescriptor)) {
            OutputStreamWriter writer = new OutputStreamWriter(stream, Files.CHARSET);
            writer.write(document.serializedJSON());
            writer.flush();
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return null;
        }
        try (FileOutputStream stream = new FileOutputStream(fileContent)) {
            stream.write(content);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return null;
        }
        documents.put(document.getIdentifier(), document);
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new DocumentUploadedEvent(document, this));
        return document;
    }

    @Override
    public void drop(Document document) {
        onActivated();
        if (storage == null)
            return;
        Document document2 = documents.remove(document.getIdentifier());
        if (document2 == null)
            return;
        File fileDescriptor = new File(storage, getDocDescriptorFile(document));
        File fileContent = new File(storage, getDocContentFile(document));
        if (!fileDescriptor.delete())
            Logging.getDefault().error("Failed to delete " + fileDescriptor.getAbsolutePath());
        if (!fileContent.delete())
            Logging.getDefault().error("Failed to delete " + fileContent.getAbsolutePath());
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new DocumentDroppedEvent(document2, this));
    }

    @Override
    public InputStream getStreamFor(Document document) {
        onActivated();
        if (storage == null)
            return null;
        File fileContent = new File(storage, getDocContentFile(document));
        if (!fileContent.exists())
            return null;
        try {
            return new FileInputStream(fileContent);
        } catch (FileNotFoundException exception) {
            Logging.getDefault().error(exception);
            return null;
        }
    }

    @Override
    public DocumentPreview getPreview(Document document, Importer importer, ImporterConfiguration configuration) {
        return importer.getPreview(document, configuration);
    }

    @Override
    public Collection<Importer> getImporters() {
        return ServiceUtils.getServices(Importer.class);
    }

    @Override
    public Importer getImporter(String importerId) {
        Collection<Importer> importers = ServiceUtils.getServices(Importer.class);
        for (Importer importer : importers) {
            if (importer.getIdentifier().equals(importerId))
                return importer;
        }
        return null;
    }

    @Override
    public Job beginImport(Document document, Importer importer, ImporterConfiguration configuration) {
        Job job = importer.getImportJob(document, configuration);
        JobExecutionService executionService = ServiceUtils.getService(JobExecutionService.class);
        if (executionService == null)
            return null;
        executionService.schedule(job);
        return job;
    }

    /**
     * Gets the descriptor file name for a document
     *
     * @param document The document
     * @return The file name
     */
    private static String getDocDescriptorFile(Document document) {
        return "document-" + document.getFileName() + "-descriptor.json";
    }

    /**
     * Gets the content file name for a document
     *
     * @param document The document
     * @return The file name
     */
    private static String getDocContentFile(Document document) {
        return "document-" + document.getFileName() + "-content";
    }

    /**
     * Gets whether a file name is a serialized descriptor file
     *
     * @param name The name of a file
     * @return Whether this is a serialized descriptor file
     */
    private static boolean isDocDescriptorFile(String name) {
        return name.startsWith("document-") && name.endsWith("-descriptor.json");
    }
}
