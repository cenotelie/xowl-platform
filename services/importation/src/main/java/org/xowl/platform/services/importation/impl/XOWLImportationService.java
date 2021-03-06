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

import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.*;
import fr.cenotelie.commons.utils.http.HttpConstants;
import fr.cenotelie.commons.utils.http.HttpResponse;
import fr.cenotelie.commons.utils.http.URIUtils;
import fr.cenotelie.commons.utils.ini.IniDocument;
import fr.cenotelie.commons.utils.json.Json;
import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactFuture;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.importation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements the importation service for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLImportationService implements ImportationService, HttpApiService {
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
     * The URI for the API services
     */
    private final String apiUri;
    /**
     * The current documents pending importation
     */
    private final Map<String, Document> documents;
    /**
     * The stored configurations
     */
    private final Map<String, ImporterConfiguration> configurations;
    /**
     * The directory for the persistent storage of the documents
     */
    private File storage;

    /**
     * Initializes this service
     */
    public XOWLImportationService() {
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/services/importation";
        this.documents = new HashMap<>();
        this.configurations = new HashMap<>();
    }

    /**
     * When the service is activated
     */
    private void onActivated() {
        if (storage == null) {
            ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
            IniDocument configuration = configurationService.getConfigFor(ImportationService.class.getCanonicalName());
            storage = PlatformUtils.resolve(configuration.get("storage"));
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
                    try (Reader reader = IOUtils.getReader(files[i].getAbsolutePath())) {
                        String content = IOUtils.read(reader);
                        reloadDocument(files[i].getAbsolutePath(), content);
                    } catch (IOException exception) {
                        Logging.get().error(exception);
                    }
                } else if (isConfigurationFile(files[i].getName())) {
                    try (Reader reader = IOUtils.getReader(files[i].getAbsolutePath())) {
                        String content = IOUtils.read(reader);
                        reloadConfiguration(content);
                    } catch (IOException exception) {
                        Logging.get().error(exception);
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
        ASTNode definition = Json.parse(Logging.get(), content);
        if (definition == null) {
            Logging.get().error("Failed to parse the document descriptor " + file);
            return;
        }
        Document document = new Document(definition);
        documents.put(document.getIdentifier(), document);
    }

    /**
     * Tries to reload a stored configuration
     *
     * @param content The file content
     */
    private void reloadConfiguration(String content) {
        ImporterConfiguration configuration = loadConfiguration(content);
        if (configuration != null)
            configurations.put(configuration.getIdentifier(), configuration);
    }

    /**
     * Loads an importer configuration from the specified serialized definition
     *
     * @param content The serialized definition of a configuration
     * @return The configuration
     */
    private ImporterConfiguration loadConfiguration(String content) {
        ASTNode definition = Json.parse(Logging.get(), content);
        if (definition == null)
            return null;
        ImporterConfiguration configuration = new ImporterConfiguration(definition);
        Importer importer = getImporter(configuration.getImporter());
        if (importer == null)
            return null;
        return importer.getConfiguration(definition);
    }

    @Override
    public String getIdentifier() {
        return XOWLImportationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Importation Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
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
        if (request.getUri().equals(apiUri + "/importers")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return onGetImporters();
        } else if (request.getUri().equals(apiUri + "/documents")) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET:
                    return onGetDocuments();
                case HttpConstants.METHOD_PUT:
                    return onPutDocument(request);
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        } else if (request.getUri().equals(apiUri + "/configurations")) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET:
                    return onGetConfigurations();
                case HttpConstants.METHOD_PUT:
                    return onPutConfiguration(request);
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        } else if (request.getUri().startsWith(apiUri + "/importers")) {
            String rest = request.getUri().substring(apiUri.length() + "/importers".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String importerId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                return onGetImporter(importerId);
            } else {
                if ("/configurations".equals(rest.substring(index)))
                    return onGetConfigurationsFor(importerId);
            }
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        } else if (request.getUri().startsWith(apiUri + "/documents")) {
            String rest = request.getUri().substring(apiUri.length() + "/documents".length() + 1);
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
                    case "/import":
                        return onBeginImport(documentId, request);
                }
            }
        } else if (request.getUri().startsWith(apiUri + "/configurations")) {
            String rest = request.getUri().substring(apiUri.length() + "/configurations".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String configurationId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                switch (request.getMethod()) {
                    case HttpConstants.METHOD_GET:
                        return onGetConfiguration(configurationId);
                    case HttpConstants.METHOD_DELETE:
                        return onPostDeleteConfiguration(configurationId);
                }
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, DELETE");
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
        return ReplyUtils.toHttpResponse(getDocuments());
    }

    /**
     * When a single document is requested
     *
     * @param documentId The identifier of the document
     * @return The HTTP response
     */
    private HttpResponse onGetDocument(String documentId) {
        return ReplyUtils.toHttpResponse(getDocument(documentId));
    }

    /**
     * When a new document is uploaded
     *
     * @param request The request to handle
     * @return The document
     */
    private HttpResponse onPutDocument(HttpApiRequest request) {
        String name = request.getParameter("name");
        String fileName = request.getParameter("fileName");
        if (name == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"));
        if (fileName == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'fileName'"));
        Reply reply = upload(name, fileName, request.getContent());
        return ReplyUtils.toHttpResponse(reply);
    }

    /**
     * When a drop request for a document is received
     *
     * @param documentId The identifier of the document
     * @return The HTTP response
     */
    private HttpResponse onPostDropDocument(String documentId) {
        return ReplyUtils.toHttpResponse(drop(documentId));
    }

    /**
     * When the preview of a document is requested
     *
     * @param documentId The identifier of the document
     * @param request    The request to handle
     * @return The HTTP response
     */
    private HttpResponse onGetPreview(String documentId, HttpApiRequest request) {
        // should use a stored configuration?
        String configurationId = request.getParameter("configuration");
        if (configurationId != null)
            return ReplyUtils.toHttpResponse(getPreview(documentId, configurationId));

        // the configuration is expected to be inline in the body
        String content = new String(request.getContent(), IOUtils.CHARSET);
        if (content.isEmpty())
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT));
        ImporterConfiguration configuration = loadConfiguration(content);
        if (configuration == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_CONTENT_PARSING_FAILED));
        return ReplyUtils.toHttpResponse(getPreview(documentId, configuration));
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
        String name = request.getParameter("name"); // the name of the artifact to produce
        String base = request.getParameter("base"); // the base family URI for the artifact
        String version = request.getParameter("version"); // the version for the artifact
        String archetype = request.getParameter("archetype"); // the archetype for the artifact
        String superseded = request.getParameter("superseded"); // the superseded artifact
        if (name == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"));
        if (base == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'base'"));
        if (version == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'version'"));
        if (archetype == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'archetype'"));
        Artifact metadata = new ArtifactFuture(name, base, version, archetype, superseded);

        // should use a stored configuration?
        String configurationId = request.getParameter("configuration");
        if (configurationId != null)
            return ReplyUtils.toHttpResponse(beginImport(documentId, configurationId, metadata));

        // the configuration is expected to be inline in the body
        String content = new String(request.getContent(), IOUtils.CHARSET);
        if (content.isEmpty())
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT));
        ImporterConfiguration configuration = loadConfiguration(content);
        if (configuration == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_CONTENT_PARSING_FAILED));
        return ReplyUtils.toHttpResponse(beginImport(documentId, configuration, metadata));
    }

    /**
     * When all the configurations are requested
     *
     * @return The HTTP response
     */
    private HttpResponse onGetConfigurations() {
        return ReplyUtils.toHttpResponse(retrieveConfigurations());
    }

    /**
     * When a specific configuration is requested
     *
     * @param configurationId The identifier of a configuration
     * @return The HTTP response
     */
    private HttpResponse onGetConfiguration(String configurationId) {
        return ReplyUtils.toHttpResponse(retrieveConfiguration(configurationId));
    }

    /**
     * When the configurations for an importer are requested
     *
     * @param importerId The identifier of an importer
     * @return The HTTP response
     */
    private HttpResponse onGetConfigurationsFor(String importerId) {
        return ReplyUtils.toHttpResponse(retrieveConfigurations(importerId));
    }

    /**
     * When a new document is uploaded
     *
     * @param request The request to handle
     * @return The document
     */
    private HttpResponse onPutConfiguration(HttpApiRequest request) {
        String content = new String(request.getContent(), IOUtils.CHARSET);
        if (content.isEmpty())
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT));
        ImporterConfiguration configuration = loadConfiguration(content);
        if (configuration == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_CONTENT_PARSING_FAILED));
        return ReplyUtils.toHttpResponse(storeConfiguration(configuration));
    }

    /**
     * When a drop request for a document is received
     *
     * @param configurationId The identifier of a configuration
     * @return The HTTP response
     */
    private HttpResponse onPostDeleteConfiguration(String configurationId) {
        return ReplyUtils.toHttpResponse(deleteConfiguration(configurationId));
    }

    @Override
    public Reply getDocuments() {
        onActivated();
        Collection<Document> result = new ArrayList<>();
        synchronized (documents) {
            for (Document document : documents.values()) {
                if (document.checkAccess().isSuccess())
                    result.add(document);
            }
        }
        return new ReplyResultCollection<>(result);
    }

    @Override
    public Reply getDocument(String documentId) {
        onActivated();
        Document document;
        synchronized (documents) {
            document = documents.get(documentId);
        }
        if (document == null)
            return ReplyNotFound.instance();
        Reply reply = document.checkAccess();
        if (!reply.isSuccess())
            return reply;
        return new ReplyResult<>(document);
    }

    @Override
    public Reply upload(String name, String fileName, byte[] content) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_UPLOAD_DOCUMENT);
        if (!reply.isSuccess())
            return reply;
        if (!storage.exists() && !storage.mkdirs())
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access document storage");
        Document document = new Document(name, fileName);
        reply = securityService.getSecuredResources().createDescriptorFor(document);
        if (!reply.isSuccess())
            return reply;
        File fileDescriptor = new File(storage, getDocDescriptorFile(document));
        File fileContent = new File(storage, getDocContentFile(document));
        try (Writer writer = IOUtils.getWriter(fileDescriptor)) {
            writer.write(document.serializedJSON());
            writer.flush();
        } catch (IOException exception) {
            Logging.get().error(exception);
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to write descriptor in storage");
        }
        try (FileOutputStream stream = new FileOutputStream(fileContent)) {
            stream.write(content);
            stream.flush();
        } catch (IOException exception) {
            Logging.get().error(exception);
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to write document in storage");
        }
        synchronized (documents) {
            documents.put(document.getIdentifier(), document);
        }
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new DocumentUploadedEvent(document, this));
        return new ReplyResult<>(document);
    }

    @Override
    public Reply drop(String documentId) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Document document;
        synchronized (documents) {
            document = documents.remove(documentId);
            if (document == null)
                return ReplyNotFound.instance();
        }
        Reply reply = securityService.checkAction(ACTION_DROP_DOCUMENT, document);
        if (!reply.isSuccess())
            return reply;
        reply = securityService.getSecuredResources().deleteDescriptorFor(document.getIdentifier());
        if (!reply.isSuccess())
            return reply;
        File fileDescriptor = new File(storage, getDocDescriptorFile(document));
        File fileContent = new File(storage, getDocContentFile(document));
        if (!fileDescriptor.delete())
            Logging.get().error("Failed to delete " + fileDescriptor.getAbsolutePath());
        if (!fileContent.delete())
            Logging.get().error("Failed to delete " + fileContent.getAbsolutePath());
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new DocumentDroppedEvent(document, this));
        return ReplySuccess.instance();
    }

    @Override
    public Reply getStreamFor(String documentId) {
        onActivated();
        Document document;
        synchronized (documents) {
            document = documents.get(documentId);
            if (document == null)
                return ReplyNotFound.instance();
        }
        Reply reply = document.checkAccess();
        if (!reply.isSuccess())
            return reply;
        File fileContent = new File(storage, getDocContentFile(document));
        if (!fileContent.exists())
            return ReplyNotFound.instance();
        try {
            FileInputStream result = new FileInputStream(fileContent);
            return new ReplyResult<>(result);
        } catch (FileNotFoundException exception) {
            Logging.get().error(exception);
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access document storage");
        }
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
    public Reply getPreview(String documentId, ImporterConfiguration configuration) {
        Importer importer = getImporter(configuration.getImporter());
        if (importer == null)
            return ReplyNotFound.instance();
        return importer.getPreview(documentId, configuration);
    }

    @Override
    public Reply getPreview(String documentId, String configurationId) {
        Reply reply = retrieveConfiguration(configurationId);
        if (!reply.isSuccess())
            return reply;
        return getPreview(documentId, ((ReplyResult<ImporterConfiguration>) reply).getData());
    }

    @Override
    public Reply beginImport(String documentId, ImporterConfiguration configuration, Artifact metadata) {
        Importer importer = getImporter(configuration.getImporter());
        if (importer == null)
            return ReplyNotFound.instance();
        JobExecutionService executionService = Register.getComponent(JobExecutionService.class);
        if (executionService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = importer.getImportJob(documentId, configuration, metadata);
        if (!reply.isSuccess())
            return reply;
        return executionService.schedule(((ReplyResult<Job>) reply).getData());
    }

    @Override
    public Reply beginImport(String documentId, String configurationId, Artifact metadata) {
        Reply reply = retrieveConfiguration(configurationId);
        if (!reply.isSuccess())
            return reply;
        return beginImport(documentId, ((ReplyResult<ImporterConfiguration>) reply).getData(), metadata);
    }

    @Override
    public Reply storeConfiguration(ImporterConfiguration configuration) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_STORE_CONFIG);
        if (!reply.isSuccess())
            return reply;
        if (!storage.exists() && !storage.mkdirs())
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access document storage");
        reply = securityService.getSecuredResources().createDescriptorFor(configuration);
        if (!reply.isSuccess())
            return reply;
        File file = new File(storage, getConfigurationFile(configuration));
        try (Writer writer = IOUtils.getWriter(file)) {
            writer.write(configuration.serializedJSON());
            writer.flush();
        } catch (IOException exception) {
            Logging.get().error(exception);
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to write configuration in storage");
        }
        synchronized (configurations) {
            configurations.put(configuration.getIdentifier(), configuration);
        }
        return ReplySuccess.instance();
    }

    @Override
    public Reply retrieveConfigurations() {
        onActivated();
        Collection<ImporterConfiguration> result = new ArrayList<>();
        synchronized (configurations) {
            for (ImporterConfiguration configuration : configurations.values()) {
                if (configuration.checkAccess().isSuccess())
                    result.add(configuration);
            }
        }
        return new ReplyResultCollection<>(result);
    }

    @Override
    public Reply retrieveConfiguration(String configurationId) {
        onActivated();
        ImporterConfiguration configuration;
        synchronized (configurations) {
            configuration = configurations.get(configurationId);
        }
        if (configuration == null)
            return ReplyNotFound.instance();
        Reply reply = configuration.checkAccess();
        if (!reply.isSuccess())
            return reply;
        return new ReplyResult<>(configuration);
    }

    @Override
    public Reply retrieveConfigurations(String importerId) {
        onActivated();
        Collection<ImporterConfiguration> result = new ArrayList<>();
        synchronized (configurations) {
            for (ImporterConfiguration configuration : configurations.values()) {
                if (configuration.checkAccess().isSuccess() && Objects.equals(configuration.getImporter(), importerId))
                    result.add(configuration);
            }
        }
        return new ReplyResultCollection<>(result);
    }

    @Override
    public Reply deleteConfiguration(String configurationId) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        ImporterConfiguration configuration;
        synchronized (configurations) {
            configuration = configurations.get(configurationId);
            if (configuration == null)
                return ReplyNotFound.instance();
            Reply reply = securityService.checkAction(ACTION_DELETE_CONFIG, configuration);
            if (!reply.isSuccess())
                return reply;
            reply = securityService.getSecuredResources().deleteDescriptorFor(configuration.getIdentifier());
            if (!reply.isSuccess())
                return reply;
            configurations.remove(configurationId);
            File file = new File(storage, getConfigurationFile(configuration));
            if (!file.delete())
                Logging.get().error("Failed to delete " + file.getAbsolutePath());
            return ReplySuccess.instance();
        }
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

    /**
     * Gets the file for a configuration to store
     *
     * @param configuration The configuration
     * @return The file name
     */
    private static String getConfigurationFile(ImporterConfiguration configuration) {
        return "configuration-" + configuration.getStorageId() + ".json";
    }

    /**
     * Gets whether a file name is a serialized configuration file
     *
     * @param name The name of a file
     * @return Whether this is a configuration descriptor file
     */
    private static boolean isConfigurationFile(String name) {
        return name.startsWith("configuration-") && name.endsWith(".json");
    }
}
