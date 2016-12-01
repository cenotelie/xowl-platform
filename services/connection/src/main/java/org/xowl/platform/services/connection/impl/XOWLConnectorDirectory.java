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

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;
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
public class XOWLConnectorDirectory implements ConnectorDirectoryService {
    /**
     * The data about a spawned connector
     */
    private static class Registration {
        /**
         * The service
         */
        public ConnectorService service;
        /**
         * The reference to this service as a connector
         */
        ServiceRegistration refAsDomainConnector;
        /**
         * The reference to this service as a served service
         */
        ServiceRegistration refAsServedService;
    }

    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/admin/connectors",
            "services/core/descriptors"
    };

    /**
     * The spawned connectors by identifier
     */
    private final Map<String, Registration> connectorsById = new HashMap<>();
    /**
     * The registered factories
     */
    private final Collection<ConnectorServiceFactory> factories = new ArrayList<>(8);
    /**
     * The map of statically configured connectors to resolve
     */
    private Map<String, List<Section>> toResolve;
    /**
     * Flag whether resolving operations are in progress
     */
    private boolean isResolving;

    @Override
    public String getIdentifier() {
        return XOWLConnectorDirectory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Domain Directory Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (method.equals("GET")) {
            if (uri.equals("services/core/descriptors"))
                return onMessageListDescriptors();
            if (uri.equals("services/admin/connectors")) {
                String[] ids = parameters.get("id");
                if (ids != null && ids.length > 0)
                    return onMessageGetConnector(ids[0]);
                return onMessageListConnectors();
            }
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        }
        if (!method.equals("POST"))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);

        String[] actions = parameters.get("action");
        String action = actions != null && actions.length >= 1 ? actions[0] : null;
        if (action != null && action.equals("spawn"))
            return onMessageCreateConnector(parameters, content);
        if (action != null && action.equals("delete"))
            return onMessageDeleteConnector(parameters);
        if (action != null && action.equals("pull"))
            return onMessagePullFromConnector(parameters);
        if (action != null && action.equals("push"))
            return onMessagePushToConnector(parameters);
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Override
    public Collection<ConnectorService> getConnectors() {
        resolveConfigConnectors(null);
        return ServiceUtils.getServices(ConnectorService.class);
    }

    @Override
    public ConnectorService get(String identifier) {
        resolveConfigConnectors(null);
        Registration registration = connectorsById.get(identifier);
        if (registration != null)
            return registration.service;
        return ServiceUtils.getService(ConnectorService.class, "id", identifier);
    }

    @Override
    public Collection<ConnectorDescription> getDescriptors() {
        Collection<ConnectorDescription> result = new ArrayList<>(16);
        for (ConnectorServiceFactory factory : factories) {
            result.addAll(factory.getDescriptors());
        }
        return result;
    }

    @Override
    public XSPReply spawn(ConnectorDescription description, String identifier, String name, String[] uris, Map<ConnectorDescriptionParam, Object> parameters) {
        synchronized (connectorsById) {
            ConnectorService service = get(identifier);
            if (service != null)
                // already exists
                return new XSPReplyFailure("A connector with this identifier already exists");

            for (ConnectorServiceFactory factory : factories) {
                if (factory.getDescriptors().contains(description)) {
                    // this is the factory
                    XSPReply reply = factory.newConnector(description, identifier, name, uris, parameters);
                    if (!reply.isSuccess())
                        return reply;
                    service = ((XSPReplyResult<ConnectorService>) reply).getData();
                    break;
                }
            }
            if (service == null)
                // failed to create the service (factory not fond?)
                return new XSPReplyFailure("Could not find the factory to create this service");

            Registration registration = new Registration();
            registration.service = service;
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("id", service.getIdentifier());
            BundleContext context = FrameworkUtil.getBundle(ConnectorService.class).getBundleContext();
            registration.refAsDomainConnector = context.registerService(ConnectorService.class, service, properties);
            registration.refAsServedService = context.registerService(HttpApiService.class, service, null);
            connectorsById.put(identifier, registration);
            EventService eventService = ServiceUtils.getService(EventService.class);
            if (eventService != null)
                eventService.onEvent(new ConnectorSpawnedEvent(this, registration.service));
            return new XSPReplyResult<>(registration.service);
        }
    }

    @Override
    public XSPReply delete(String identifier) {
        synchronized (connectorsById) {
            Registration registration = connectorsById.remove(identifier);
            if (registration == null)
                return XSPReplyNotFound.instance();
            registration.refAsDomainConnector.unregister();
            registration.refAsServedService.unregister();
            EventService eventService = ServiceUtils.getService(EventService.class);
            if (eventService != null)
                eventService.onEvent(new ConnectorDeletedEvent(this, registration.service));
            return XSPReplySuccess.instance();
        }
    }

    /**
     * When a new connector factory service comes online
     *
     * @param factory The new factory
     */
    public void onFactoryOnline(ConnectorServiceFactory factory) {
        synchronized (factories) {
            factories.add(factory);
        }
        resolveConfigConnectors(factory);
    }

    /**
     * When a connector factory service comes offline
     *
     * @param factory The factory
     */
    public void onFactoryOffline(ConnectorServiceFactory factory) {
        synchronized (factories) {
            factories.remove(factory);
        }
    }

    /**
     * Resolve statically configured connectors
     *
     * @param factory The new factory, if any
     */
    private void resolveConfigConnectors(ConnectorServiceFactory factory) {
        if (isResolving)
            return;
        isResolving = true;
        if (toResolve == null) {
            ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
            if (configurationService == null)
                return;
            Configuration configuration = configurationService.getConfigFor(this);
            toResolve = new HashMap<>();
            for (Section section : configuration.getSections()) {
                String descriptorId = section.get("descriptor");
                if (descriptorId == null)
                    continue;
                List<Section> sections = toResolve.get(descriptorId);
                if (sections == null) {
                    sections = new ArrayList<>();
                    toResolve.put(descriptorId, sections);
                }
                sections.add(section);
            }
        }
        List<Map.Entry<String, List<Section>>> entries = new ArrayList<>(toResolve.entrySet());
        for (Map.Entry<String, List<Section>> entry : entries) {
            if (factory != null) {
                // this is a new factory
                for (ConnectorDescription description : factory.getDescriptors()) {
                    if (description.getIdentifier().equals(entry.getKey())) {
                        resolveConfigConnectors(description, entry.getValue());
                        break;
                    }
                }
            } else {
                synchronized (factories) {
                    for (ConnectorServiceFactory existingFactory : factories) {
                        boolean found = false;
                        for (ConnectorDescription description : existingFactory.getDescriptors()) {
                            if (description.getIdentifier().equals(entry.getKey())) {
                                resolveConfigConnectors(description, entry.getValue());
                                found = true;
                                break;
                            }
                        }
                        if (found)
                            break;
                    }
                }
            }
        }
        isResolving = false;
    }

    /**
     * Resolves a statically configured connector
     *
     * @param description The description
     * @param sections    The configurations to resolve
     */
    private void resolveConfigConnectors(ConnectorDescription description, List<Section> sections) {
        for (int i = sections.size() - 1; i != -1; i--) {
            if (resolveConfigConnector(description, sections.get(i)))
                sections.remove(i);
        }
        if (sections.isEmpty())
            toResolve.remove(description.getIdentifier());
    }

    /**
     * Resolves a statically configured connector
     *
     * @param description The description
     * @param section     The configuration to resolve
     * @return Whether the operation succeeded
     */
    private boolean resolveConfigConnector(ConnectorDescription description, Section section) {
        String id = section.getName();
        String name = section.get("name");
        if (id == null || name == null)
            return false;
        List<String> uris = section.getAll("uris");
        Map<ConnectorDescriptionParam, Object> customParams = new HashMap<>();
        for (String property : section.getProperties()) {
            if (property.equals("name") || property.equals("uris"))
                continue;
            ConnectorDescriptionParam parameter = null;
            for (ConnectorDescriptionParam p : description.getParameters()) {
                if (p.getIdentifier().equals(property)) {
                    parameter = p;
                    break;
                }
            }
            if (parameter == null)
                continue;
            List<String> values = section.getAll(property);
            if (values.size() == 1)
                customParams.put(parameter, values.get(0));
            else if (values.size() > 1)
                customParams.put(parameter, values.toArray());
        }
        XSPReply reply = spawn(description, id, name, uris.toArray(new String[uris.size()]), customParams);
        return reply.isSuccess();
    }

    /**
     * Responds to a request for the list of the connectors
     *
     * @return The response
     */
    private HttpResponse onMessageListConnectors() {
        Collection<ConnectorService> connectors = ServiceUtils.getServices(ConnectorService.class);
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
        ConnectorService connector = ServiceUtils.getService(ConnectorService.class, "id", connectorId);
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
        Collection<ConnectorDescription> domains = getDescriptors();
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (ConnectorDescription description : domains) {
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
     * @param parameters The request parameters
     * @param content    The content
     * @return The response
     */
    private HttpResponse onMessageCreateConnector(Map<String, String[]> parameters, byte[] content) {
        String[] domainIds = parameters.get("descriptor");
        if (domainIds == null || domainIds.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected descriptor parameter");
        if (content == null || content.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected JSON content");

        BufferedLogger logger = new BufferedLogger();
        ASTNode root = JSONLDLoader.parseJSON(logger, new String(content, Files.CHARSET));
        if (root == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, logger.getErrorsAsString());

        ConnectorDescription descriptor = null;
        for (ConnectorDescription description : getDescriptors()) {
            if (description.getIdentifier().equals(domainIds[0])) {
                descriptor = description;
                break;
            }
        }
        if (descriptor == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Failed to find descriptor " + domainIds[0]);

        String id = null;
        String name = null;
        List<String> uris = new ArrayList<>(2);
        Map<ConnectorDescriptionParam, Object> customParams = new HashMap<>();
        for (ASTNode member : root.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            switch (head) {
                case "identifier":
                    id = TextUtils.unescape(member.getChildren().get(1).getValue());
                    id = id.substring(1, id.length() - 1);
                    break;
                case "name":
                    name = TextUtils.unescape(member.getChildren().get(1).getValue());
                    name = name.substring(1, name.length() - 1);
                    break;
                case "uris": {
                    ASTNode valueNode = member.getChildren().get(1);
                    if (valueNode.getValue() != null) {
                        String value = TextUtils.unescape(valueNode.getValue());
                        value = value.substring(1, value.length() - 1);
                        uris.add(value);
                    } else if (valueNode.getChildren().size() > 0) {
                        for (ASTNode childNode : valueNode.getChildren()) {
                            String value = TextUtils.unescape(childNode.getValue());
                            value = value.substring(1, value.length() - 1);
                            uris.add(value);
                        }
                    }
                    break;
                }
                default: {
                    ConnectorDescriptionParam parameter = null;
                    for (ConnectorDescriptionParam p : descriptor.getParameters()) {
                        if (p.getIdentifier().equals(head)) {
                            parameter = p;
                            break;
                        }
                    }
                    if (parameter != null) {
                        ASTNode valueNode = member.getChildren().get(1);
                        if (valueNode.getValue() != null) {
                            String value = TextUtils.unescape(valueNode.getValue());
                            if (value.startsWith("\"") && value.endsWith("\""))
                                value = value.substring(1, value.length() - 1);
                            customParams.put(parameter, value);
                        } else if (valueNode.getChildren().size() > 0) {
                            String[] values = new String[valueNode.getChildren().size()];
                            for (int i = 0; i != valueNode.getChildren().size(); i++) {
                                String value = TextUtils.unescape(valueNode.getChildren().get(i).getValue());
                                if (value.startsWith("\"") && value.endsWith("\""))
                                    value = value.substring(1, value.length() - 1);
                                values[i] = value;
                            }
                            customParams.put(parameter, values);
                        }
                    }
                }
            }
        }

        if (id == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Identifier for connector not specified");
        if (name == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Name for connector not specified");

        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        reply = spawn(descriptor, id, name, uris.toArray(new String[uris.size()]), customParams);
        return XSPReplyUtils.toHttpResponse(reply, null);
    }

    /**
     * Responds to the request to delete a previously spawned parametric connector
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onMessageDeleteConnector(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected an id parameter");

        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        reply = delete(ids[0]);
        return XSPReplyUtils.toHttpResponse(reply, null);
    }

    /**
     * Responds to the request to pull an artifact from a connector
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onMessagePullFromConnector(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected an id parameter");
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Could not find the job execution service");
        Job job = new PullArtifactJob(ids[0]);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }

    /**
     * Responds to the request to push an artifact to a connector
     * When successful, this action creates the appropriate job and returns it.
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onMessagePushToConnector(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected an id parameter");
        String[] artifacts = parameters.get("artifact");
        if (artifacts == null || artifacts.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected an artifact parameter");

        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Could not find the job execution service");
        Job job = new PushArtifactJob(ids[0], artifacts[0]);
        executor.schedule(job);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, job.serializedJSON());
    }
}
