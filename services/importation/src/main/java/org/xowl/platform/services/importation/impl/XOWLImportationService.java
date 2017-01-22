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
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.ApiError;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.importation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the importation service for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLImportationService implements ImportationService, HttpApiService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/services/importation";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLImportationService.class, "/org/xowl/platform/services/importation/api_service_importation.raml", "Importation Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLImportationService.class, "/org/xowl/platform/services/importation/api_service_importation.html", "Importation Service - Documentation", HttpApiResource.MIME_HTML);
    /**
     * The resource for the API's schema
     */
    private static final HttpApiResource RESOURCE_SCHEMA = new HttpApiResourceBase(XOWLImportationService.class, "/org/xowl/platform/services/importation/schema_platform_importation.json", "Importation Service - Schema", HttpConstants.MIME_JSON);


    /**
     * API error - The requested operation failed in storage
     */
    public static final ApiError ERROR_OPERATION_FAILED = new ApiError(0x00040001,
            "The requested operation failed in storage.",
            HttpApiService.ERROR_HELP_PREFIX + "0x00040001.html");

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
            ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
            Configuration configuration = configurationService != null ? configurationService.getConfigFor(ImportationService.class.getCanonicalName()) : null;
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
            Logging.getDefault().error("Failed to parse the document descriptor " + file);
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
        return "xOWL Collaboration Platform - Importation Service";
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/importers")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return onGetImporters();
        } else if (request.getUri().equals(URI_API + "/documents")) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET:
                    return onGetDocuments();
                case HttpConstants.METHOD_PUT:
                    return onPutDocument(request);
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        } else if (request.getUri().startsWith(URI_API + "/importers")) {
            String rest = request.getUri().substring(URI_API.length() + "/importers".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String importerId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                return onGetImporter(importerId);
            }
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        } else if (request.getUri().startsWith(URI_API + "/documents")) {
            String rest = request.getUri().substring(URI_API.length() + "/documents".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String documentId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                switch (request.getMethod()) {
                    case HttpConstants.METHOD_GET:
                        return onGetDocument(documentId);
                    case HttpConstants.METHOD_DELETE:
                        return onPostDropDocument(documentId);
                }
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, DELETE");
            } else {
                switch (rest.substring(index)) {
                    case "/preview":
                        return onGetPreview(documentId, request);
                    case "/import": {
                        return onBeginImport(documentId, request);
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
        return new HttpApiResource[]{RESOURCE_SCHEMA};
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
     * @param request The request to handle
     * @return The document
     */
    private HttpResponse onPutDocument(HttpApiRequest request) {
        String[] names = request.getParameter("name");
        String[] fileNames = request.getParameter("fileName");
        if (names == null || names.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
        if (fileNames == null || fileNames.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'fileName'"), null);
        Document document = upload(names[0], fileNames[0], request.getContent());
        if (document == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_OPERATION_FAILED), null);
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
     * @param documentId The identifier of the document
     * @param request    The request to handle
     * @return The HTTP response
     */
    private HttpResponse onGetPreview(String documentId, HttpApiRequest request) {
        String[] importerIds = request.getParameter("importer");
        if (importerIds == null || importerIds.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'importer'"), null);
        String configuration = new String(request.getContent(), Files.CHARSET);
        if (configuration.isEmpty())
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT, "Body is empty"), null);

        Document document = getDocument(documentId);
        if (document == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        Collection<Importer> importers = Register.getComponents(Importer.class);
        for (Importer importer : importers) {
            if (importer.getIdentifier().equals(importerIds[0])) {
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
        Collection<Importer> importers = Register.getComponents(Importer.class);
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
     * @param documentId The identifier of the document
     * @param request    The request to handle
     * @return The HTTP response
     */
    private HttpResponse onBeginImport(String documentId, HttpApiRequest request) {
        String[] importerIds = request.getParameter("importer");
        if (importerIds == null || importerIds.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'importer'"), null);
        String configuration = new String(request.getContent(), Files.CHARSET);
        if (configuration.isEmpty())
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT, "Body is empty"), null);

        Document document = getDocument(documentId);
        if (document == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        Collection<Importer> importers = Register.getComponents(Importer.class);
        for (Importer importer : importers) {
            if (importer.getIdentifier().equals(importerIds[0])) {
                Job job = beginImport(document, importer, importer.getConfiguration(configuration));
                if (job == null)
                    return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
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
    public Document upload(String name, String fileName, byte[] content) {
        onActivated();
        if (storage == null)
            return null;
        if (!storage.exists() && !storage.mkdirs())
            return null;
        Document document = new Document(name, fileName);
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
        EventService eventService = Register.getComponent(EventService.class);
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
        EventService eventService = Register.getComponent(EventService.class);
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
        return Register.getComponents(Importer.class);
    }

    @Override
    public Importer getImporter(String importerId) {
        Collection<Importer> importers = Register.getComponents(Importer.class);
        for (Importer importer : importers) {
            if (importer.getIdentifier().equals(importerId))
                return importer;
        }
        return null;
    }

    @Override
    public Job beginImport(Document document, Importer importer, ImporterConfiguration configuration) {
        Job job = importer.getImportJob(document, configuration);
        JobExecutionService executionService = Register.getComponent(JobExecutionService.class);
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
        return "document-" + document.getStorageId() + "-descriptor.json";
    }

    /**
     * Gets the content file name for a document
     *
     * @param document The document
     * @return The file name
     */
    private static String getDocContentFile(Document document) {
        return "document-" + document.getStorageId() + "-content";
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
