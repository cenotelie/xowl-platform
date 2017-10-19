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

package org.xowl.platform.services.connection.impl;

import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.*;
import fr.cenotelie.commons.utils.config.Configuration;
import fr.cenotelie.commons.utils.config.Section;
import fr.cenotelie.commons.utils.http.HttpConstants;
import fr.cenotelie.commons.utils.http.HttpResponse;
import fr.cenotelie.commons.utils.http.URIUtils;
import fr.cenotelie.commons.utils.json.Json;
import fr.cenotelie.commons.utils.logging.BufferedLogger;
import fr.cenotelie.hime.redist.ASTNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.connection.*;
import org.xowl.platform.services.connection.events.ConnectorDeletedEvent;
import org.xowl.platform.services.connection.events.ConnectorSpawnedEvent;
import org.xowl.platform.services.connection.jobs.PullArtifactJob;
import org.xowl.platform.services.connection.jobs.PushArtifactJob;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements a directory service for the connectors
 *
 * @author Laurent Wouters
 */
public class XOWLConnectionService implements ConnectionService, HttpApiService, ManagedService {
    /**
     * The data about a spawned connector
     */
    private static class Registration {
        /**
         * The service
         */
        public ConnectorService service;
        /**
         * The reference to this service
         */
        ServiceRegistration[] references;
    }

    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLConnectionService.class, "/org/xowl/platform/services/connection/api_service_connection.raml", "Connection Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLConnectionService.class, "/org/xowl/platform/services/connection/api_service_connection.html", "Connection Service - Documentation", HttpApiResource.MIME_HTML);
    /**
     * The resource for the API's schema
     */
    private static final HttpApiResource RESOURCE_SCHEMA = new HttpApiResourceBase(XOWLConnectionService.class, "/org/xowl/platform/services/connection/schema_platform_connection.json", "Connection Service - Schema", HttpConstants.MIME_JSON);

    /**
     * The URI for the API services
     */
    private final String apiUri;
    /**
     * The spawned connectors by identifier
     */
    private final Map<String, Registration> connectorsById;

    /**
     * Initializes this service
     */
    public XOWLConnectionService() {
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/services/connection";
        this.connectorsById = new HashMap<>();
    }

    @Override
    public String getIdentifier() {
        return XOWLConnectionService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Connection Service";
    }

    @Override
    public int getLifecycleTier() {
        return TIER_IO;
    }

    @Override
    public void onLifecycleStart() {
        ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
        if (configurationService == null)
            return;
        Configuration configuration = configurationService.getConfigFor(ConnectionService.class.getCanonicalName());
        for (Section section : configuration.getSections()) {
            resolveConfigConnector(section);
        }
    }

    @Override
    public void onLifecycleStop() {
        // disconnect the connectors
        synchronized (connectorsById) {
            for (Registration registration : connectorsById.values()) {
                for (int i = 0; i != registration.references.length; i++) {
                    try {
                        registration.references[i].unregister();
                    } catch (IllegalStateException exception) {
                        // service already unregistered
                        // do nothing
                    }
                }
            }
            connectorsById.clear();
        }
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
        if (request.getUri().equals(apiUri + "/descriptors")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return onMessageListDescriptors();
        } else if (request.getUri().equals(apiUri + "/connectors")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return onMessageListConnectors();
        } else if (request.getUri().startsWith(apiUri + "/connectors")) {
            String rest = request.getUri().substring(apiUri.length() + "/connectors".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String connectorId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                switch (request.getMethod()) {
                    case HttpConstants.METHOD_GET:
                        return onMessageGetConnector(connectorId);
                    case HttpConstants.METHOD_PUT:
                        return onMessageCreateConnector(connectorId, request);
                    case HttpConstants.METHOD_DELETE:
                        return onMessageDeleteConnector(connectorId);
                }
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
            }
            rest = rest.substring(index);
            switch (rest) {
                case "/pull": {
                    if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                    return onMessagePullFromConnector(connectorId);
                }
                case "/push": {
                    if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                    return onMessagePushToConnector(connectorId, request);
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

    @Override
    public Collection<ConnectorService> getConnectors() {
        return Register.getComponents(ConnectorService.class);
    }

    @Override
    public ConnectorService getConnector(String identifier) {
        Registration registration = connectorsById.get(identifier);
        if (registration != null)
            return registration.service;
        return Register.getComponent(ConnectorService.class, "id", identifier);
    }

    @Override
    public Collection<ConnectorDescriptor> getDescriptors() {
        return Register.getComponents(ConnectorDescriptor.class);
    }

    @Override
    public Reply spawn(ConnectorDescriptor description, ConnectorServiceData specification) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_SPAWN);
        if (!reply.isSuccess())
            return reply;

        synchronized (connectorsById) {
            ConnectorService service = getConnector(specification.getIdentifier());
            if (service != null)
                // already exists
                return new ReplyApiError(ERROR_CONNECTOR_SAME_ID);

            for (ConnectorServiceFactory factory : Register.getComponents(ConnectorServiceFactory.class)) {
                service = factory.newConnector(description, specification);
                if (service != null)
                    break;
            }
            if (service == null)
                // failed to create the service (factory not fond?)
                return new ReplyApiError(ERROR_NO_FACTORY);

            Registration registration = new Registration();
            registration.service = service;
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("id", service.getIdentifier());
            BundleContext context = FrameworkUtil.getBundle(ConnectorService.class).getBundleContext();
            if (service instanceof HttpApiService) {
                registration.references = new ServiceRegistration[]{
                        context.registerService(Service.class, service, properties),
                        context.registerService(SecuredService.class, service, properties),
                        context.registerService(ConnectorService.class, service, properties),
                        context.registerService(HttpApiService.class, (HttpApiService) service, properties)
                };
            } else {
                registration.references = new ServiceRegistration[]{
                        context.registerService(Service.class, service, properties),
                        context.registerService(SecuredService.class, service, properties),
                        context.registerService(ConnectorService.class, service, properties)
                };
            }
            connectorsById.put(specification.getIdentifier(), registration);
            EventService eventService = Register.getComponent(EventService.class);
            if (eventService != null)
                eventService.onEvent(new ConnectorSpawnedEvent(this, registration.service));
            return new ReplyResult<>(registration.service);
        }
    }

    @Override
    public Reply delete(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_DELETE);
        if (!reply.isSuccess())
            return reply;

        synchronized (connectorsById) {
            Registration registration = connectorsById.remove(identifier);
            if (registration == null)
                return ReplyNotFound.instance();
            for (int i = 0; i != registration.references.length; i++)
                registration.references[i].unregister();
            EventService eventService = Register.getComponent(EventService.class);
            if (eventService != null)
                eventService.onEvent(new ConnectorDeletedEvent(this, registration.service));
            return ReplySuccess.instance();
        }
    }

    /**
     * Resolves a statically configured connector
     *
     * @param section The configuration to resolve
     */
    private void resolveConfigConnector(Section section) {
        String id = section.getName();
        String name = section.get("name");
        String descriptorId = section.get("descriptor");
        if (id == null || name == null || descriptorId == null)
            return;
        ConnectorDescriptor descriptor = null;
        for (ConnectorDescriptor d : Register.getComponents(ConnectorDescriptor.class)) {
            if (d.getIdentifier().equalsIgnoreCase(descriptorId)) {
                descriptor = d;
                break;
            }
        }
        if (descriptor == null)
            return;

        List<String> uris = section.getAll("uris");
        ConnectorServiceData specification = new ConnectorServiceData(id, name, uris.toArray(new String[uris.size()]));
        for (String property : section.getProperties()) {
            if (property.equals("name") || property.equals("uris") || property.equals("descriptor"))
                continue;
            ConnectorDescriptorParam parameter = null;
            for (ConnectorDescriptorParam p : descriptor.getParameters()) {
                if (p.getIdentifier().equals(property)) {
                    parameter = p;
                    break;
                }
            }
            if (parameter == null)
                continue;
            List<String> values = section.getAll(property);
            if (values.size() == 1)
                specification.addParameter(parameter, values.get(0));
            else if (values.size() > 1)
                specification.addParameter(parameter, values.toArray());
        }
        spawn(descriptor, specification);
    }

    /**
     * Responds to a request for the list of the connectors
     *
     * @return The response
     */
    private HttpResponse onMessageListConnectors() {
        Collection<ConnectorService> connectors = getConnectors();
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (ConnectorService connector : connectors) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(connector.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for a single connector
     *
     * @param connectorId The identifier of a connector
     * @return The response
     */
    private HttpResponse onMessageGetConnector(String connectorId) {
        ConnectorService connector = getConnector(connectorId);
        if (connector == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, connector.serializedJSON());
    }

    /**
     * Responds to a request for the list of the descriptors
     *
     * @return The response
     */
    private HttpResponse onMessageListDescriptors() {
        Collection<ConnectorDescriptor> domains = getDescriptors();
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (ConnectorDescriptor description : domains) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(description.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to the request to spawn a new parametric connector
     *
     * @param connectorId The identifier of a connector
     * @param request     The request to handle
     * @return The response
     */
    private HttpResponse onMessageCreateConnector(String connectorId, HttpApiRequest request) {
        String descriptorId = request.getParameter("descriptor");
        if (descriptorId == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'descriptor'"));
        String content = new String(request.getContent(), IOUtils.CHARSET);
        if (content.isEmpty())
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT));

        BufferedLogger logger = new BufferedLogger();
        ASTNode root = Json.parse(logger, content);
        if (root == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()));

        ConnectorDescriptor descriptor = null;
        for (ConnectorDescriptor description : getDescriptors()) {
            if (description.getIdentifier().equals(descriptorId)) {
                descriptor = description;
                break;
            }
        }
        if (descriptor == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_PARAMETER_RANGE, "'descriptor' is not the identifier of a recognized connector descriptor"));
        ConnectorServiceData specification = new ConnectorServiceData(descriptor, root);
        if (!specification.getIdentifier().equals(connectorId))
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_PARAMETER_RANGE, "'identifier' is not the same as URI parameter"));
        Reply reply = spawn(descriptor, specification);
        if (!reply.isSuccess())
            return ReplyUtils.toHttpResponse(reply);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, ((ReplyResult<ConnectorService>) reply).getData().serializedJSON());
    }

    /**
     * Responds to the request to delete a previously spawned parametric connector
     *
     * @param connectorId The identifier of the connector to delete
     * @return The response
     */
    private HttpResponse onMessageDeleteConnector(String connectorId) {
        Reply reply = delete(connectorId);
        return ReplyUtils.toHttpResponse(reply);
    }

    /**
     * Responds to the request to pull an artifact from a connector
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param connectorId The identifier of the connector to delete
     * @return The response
     */
    private HttpResponse onMessagePullFromConnector(String connectorId) {
        JobExecutionService executor = Register.getComponent(JobExecutionService.class);
        if (executor == null)
            return ReplyUtils.toHttpResponse(ReplyServiceUnavailable.instance());
        Job job = new PullArtifactJob(connectorId);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }

    /**
     * Responds to the request to push an artifact to a connector
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param connectorId The identifier of the connector to delete
     * @param request     The request to handle
     * @return The response
     */
    private HttpResponse onMessagePushToConnector(String connectorId, HttpApiRequest request) {
        String artifact = request.getParameter("artifact");
        if (artifact == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'artifact'"));

        JobExecutionService executor = Register.getComponent(JobExecutionService.class);
        if (executor == null)
            return ReplyUtils.toHttpResponse(ReplyServiceUnavailable.instance());
        Job job = new PushArtifactJob(connectorId, artifact);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }
}
