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
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.Logging;
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
                } else if (isConfigurationFile(files[i].getName())) {
                    try (Reader reader = Files.getReader(files[i].getAbsolutePath())) {
                        String content = Files.read(reader);
                        reloadConfiguration(content);
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
        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
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
        return XSPReplyUtils.toHttpResponse(getDocuments(), null);
    }

    /**
     * When a single document is requested
     *
     * @param documentId The identifier of the document
     * @return The HTTP response
     */
    private HttpResponse onGetDocument(String documentId) {
        return XSPReplyUtils.toHttpResponse(getDocument(documentId), null);
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
        XSPReply reply = upload(names[0], fileNames[0], request.getContent());
        return XSPReplyUtils.toHttpResponse(reply, null);
    }

    /**
     * When a drop request for a document is received
     *
     * @param documentId The identifier of the document
     * @return The HTTP response
     */
    private HttpResponse onPostDropDocument(String documentId) {
        return XSPReplyUtils.toHttpResponse(drop(documentId), null);
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
        String[] configurationIds = request.getParameter("configuration");
        if (configurationIds != null && configurationIds.length > 0)
            return XSPReplyUtils.toHttpResponse(getPreview(documentId, configurationIds[0]), null);

        // the configuration is expected to be inline in the body
        String content = new String(request.getContent(), Files.CHARSET);
        if (content.isEmpty())
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
        ImporterConfiguration configuration = loadConfiguration(content);
        if (configuration == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED), null);
        return XSPReplyUtils.toHttpResponse(getPreview(documentId, configuration), null);
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
        String[] names = request.getParameter("name"); // the name of the artifact to produce
        String[] bases = request.getParameter("base"); // the base family URI for the artifact
        String[] versions = request.getParameter("version"); // the version for the artifact
        String[] archetypes = request.getParameter("archetype"); // the archetype for the artifact
        String[] superseded = request.getParameter("superseded"); // the superseded artifacts
        if (names == null || names.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
        if (bases == null || bases.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'base'"), null);
        if (versions == null || versions.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'version'"), null);
        if (archetypes == null || archetypes.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'archetype'"), null);
        if (superseded == null)
            superseded = new String[0];
        Artifact metadata = new ArtifactFuture(names[0], bases[0], versions[0], archetypes[0], superseded);

        // should use a stored configuration?
        String[] configurationIds = request.getParameter("configuration");
        if (configurationIds != null && configurationIds.length > 0)
            return XSPReplyUtils.toHttpResponse(beginImport(documentId, configurationIds[0], metadata), null);

        // the configuration is expected to be inline in the body
        String content = new String(request.getContent(), Files.CHARSET);
        if (content.isEmpty())
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
        ImporterConfiguration configuration = loadConfiguration(content);
        if (configuration == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED), null);
        return XSPReplyUtils.toHttpResponse(beginImport(documentId, configuration, metadata), null);
    }

    /**
     * When all the configurations are requested
     *
     * @return The HTTP response
     */
    private HttpResponse onGetConfigurations() {
        return XSPReplyUtils.toHttpResponse(retrieveConfigurations(), null);
    }

    /**
     * When a specific configuration is requested
     *
     * @param configurationId The identifier of a configuration
     * @return The HTTP response
     */
    private HttpResponse onGetConfiguration(String configurationId) {
        return XSPReplyUtils.toHttpResponse(retrieveConfiguration(configurationId), null);
    }

    /**
     * When the configurations for an importer are requested
     *
     * @param importerId The identifier of an importer
     * @return The HTTP response
     */
    private HttpResponse onGetConfigurationsFor(String importerId) {
        return XSPReplyUtils.toHttpResponse(retrieveConfigurations(importerId), null);
    }

    /**
     * When a new document is uploaded
     *
     * @param request The request to handle
     * @return The document
     */
    private HttpResponse onPutConfiguration(HttpApiRequest request) {
        String content = new String(request.getContent(), Files.CHARSET);
        if (content.isEmpty())
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
        ImporterConfiguration configuration = loadConfiguration(content);
        if (configuration == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED), null);
        return XSPReplyUtils.toHttpResponse(storeConfiguration(configuration), null);
    }

    /**
     * When a drop request for a document is received
     *
     * @param configurationId The identifier of a configuration
     * @return The HTTP response
     */
    private HttpResponse onPostDeleteConfiguration(String configurationId) {
        return XSPReplyUtils.toHttpResponse(deleteConfiguration(configurationId), null);
    }

    @Override
    public XSPReply getDocuments() {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_DOCUMENT_METADATA);
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResultCollection<>(documents.values());
    }

    @Override
    public XSPReply getDocument(String documentId) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_DOCUMENT_METADATA);
        if (!reply.isSuccess())
            return reply;
        Document document = documents.get(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();
        return new XSPReplyResult<>(document);
    }

    @Override
    public XSPReply upload(String name, String fileName, byte[] content) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_UPLOAD_DOCUMENT);
        if (!reply.isSuccess())
            return reply;
        if (storage == null)
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access document storage");
        if (!storage.exists() && !storage.mkdirs())
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access document storage");
        Document document = new Document(name, fileName);
        File fileDescriptor = new File(storage, getDocDescriptorFile(document));
        File fileContent = new File(storage, getDocContentFile(document));
        try (FileOutputStream stream = new FileOutputStream(fileDescriptor)) {
            OutputStreamWriter writer = new OutputStreamWriter(stream, Files.CHARSET);
            writer.write(document.serializedJSON());
            writer.flush();
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to write descriptor in storage");
        }
        try (FileOutputStream stream = new FileOutputStream(fileContent)) {
            stream.write(content);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to write document in storage");
        }
        documents.put(document.getIdentifier(), document);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new DocumentUploadedEvent(document, this));
        return new XSPReplyResult<>(document);
    }

    @Override
    public XSPReply drop(String documentId) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_DROP_DOCUMENT);
        if (!reply.isSuccess())
            return reply;
        if (storage == null)
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access document storage");
        Document document = documents.remove(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();
        File fileDescriptor = new File(storage, getDocDescriptorFile(document));
        File fileContent = new File(storage, getDocContentFile(document));
        if (!fileDescriptor.delete())
            Logging.getDefault().error("Failed to delete " + fileDescriptor.getAbsolutePath());
        if (!fileContent.delete())
            Logging.getDefault().error("Failed to delete " + fileContent.getAbsolutePath());
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new DocumentDroppedEvent(document, this));
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply getStreamFor(String documentId) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_DOCUMENT_CONTENT);
        if (!reply.isSuccess())
            return reply;
        if (storage == null)
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access document storage");
        Document document = documents.get(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();
        File fileContent = new File(storage, getDocContentFile(document));
        if (!fileContent.exists())
            return XSPReplyNotFound.instance();
        try {
            FileInputStream result = new FileInputStream(fileContent);
            return new XSPReplyResult<>(result);
        } catch (FileNotFoundException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access document storage");
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
    public XSPReply getPreview(String documentId, ImporterConfiguration configuration) {
        Importer importer = getImporter(configuration.getImporter());
        if (importer == null)
            return XSPReplyNotFound.instance();
        return importer.getPreview(documentId, configuration);
    }

    @Override
    public XSPReply getPreview(String documentId, String configurationId) {
        XSPReply reply = retrieveConfiguration(configurationId);
        if (!reply.isSuccess())
            return reply;
        return getPreview(documentId, ((XSPReplyResult<ImporterConfiguration>) reply).getData());
    }

    @Override
    public XSPReply beginImport(String documentId, ImporterConfiguration configuration, Artifact metadata) {
        Importer importer = getImporter(configuration.getImporter());
        if (importer == null)
            return XSPReplyNotFound.instance();
        JobExecutionService executionService = Register.getComponent(JobExecutionService.class);
        if (executionService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = importer.getImportJob(documentId, configuration, metadata);
        if (!reply.isSuccess())
            return reply;
        return executionService.schedule(((XSPReplyResult<Job>) reply).getData());
    }

    @Override
    public XSPReply beginImport(String documentId, String configurationId, Artifact metadata) {
        XSPReply reply = retrieveConfiguration(configurationId);
        if (!reply.isSuccess())
            return reply;
        return beginImport(documentId, ((XSPReplyResult<ImporterConfiguration>) reply).getData(), metadata);
    }

    @Override
    public XSPReply storeConfiguration(ImporterConfiguration configuration) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_STORE_CONFIG);
        if (!reply.isSuccess())
            return reply;
        if (storage == null)
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access configuration storage");
        File file = new File(storage, getConfigurationFile(configuration));
        try (FileOutputStream stream = new FileOutputStream(file)) {
            OutputStreamWriter writer = new OutputStreamWriter(stream, Files.CHARSET);
            writer.write(configuration.serializedJSON());
            writer.flush();
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to write configuration in storage");
        }
        synchronized (configurations) {
            configurations.put(configuration.getIdentifier(), configuration);
        }
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply retrieveConfigurations() {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_RETRIEVE_CONFIG);
        if (!reply.isSuccess())
            return reply;
        if (storage == null)
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access configuration storage");
        synchronized (configurations) {
            return new XSPReplyResultCollection<>(configurations.values());
        }
    }

    @Override
    public XSPReply retrieveConfiguration(String configurationId) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_RETRIEVE_CONFIG);
        if (!reply.isSuccess())
            return reply;
        if (storage == null)
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access configuration storage");
        synchronized (configurations) {
            ImporterConfiguration configuration = configurations.get(configurationId);
            if (configuration == null)
                return XSPReplyNotFound.instance();
            return new XSPReplyResult<>(configuration);
        }
    }

    @Override
    public XSPReply retrieveConfigurations(String importerId) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_RETRIEVE_CONFIG);
        if (!reply.isSuccess())
            return reply;
        if (storage == null)
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access configuration storage");
        Collection<ImporterConfiguration> result = new ArrayList<>();
        synchronized (configurations) {
            for (ImporterConfiguration configuration : configurations.values()) {
                if (Objects.equals(configuration.getImporter(), importerId))
                    result.add(configuration);
            }
        }
        return new XSPReplyResultCollection<>(result);
    }

    @Override
    public XSPReply deleteConfiguration(String configurationId) {
        onActivated();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_DELETE_CONFIG);
        if (!reply.isSuccess())
            return reply;
        if (storage == null)
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to access configuration storage");
        synchronized (configurations) {
            ImporterConfiguration configuration = configurations.remove(configurationId);
            if (configuration == null)
                return XSPReplyNotFound.instance();
            File file = new File(storage, getConfigurationFile(configuration));
            if (!file.delete())
                Logging.getDefault().error("Failed to delete " + file.getAbsolutePath());
        }
        return XSPReplySuccess.instance();
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
