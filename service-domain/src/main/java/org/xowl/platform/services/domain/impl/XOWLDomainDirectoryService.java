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

package org.xowl.platform.services.domain.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.config.ConfigurationService;
import org.xowl.platform.services.domain.*;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.platform.utils.Utils;
import org.xowl.store.IOUtils;
import org.xowl.utils.config.Configuration;
import org.xowl.utils.config.Section;
import org.xowl.utils.logging.BufferedLogger;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements a directory service for the domain connectors
 *
 * @author Laurent Wouters
 */
public class XOWLDomainDirectoryService implements DomainDirectoryService {
    /**
     * The data about a spawned connector
     */
    private static class Registration {
        /**
         * The service
         */
        public DomainConnectorService service;
        /**
         * The reference to this service as a domain connector
         */
        ServiceRegistration refAsDomainConnector;
        /**
         * The reference to this service as a served service
         */
        ServiceRegistration refAsServedService;
    }

    /**
     * The spawned connectors by identifier
     */
    private final Map<String, Registration> connectorsById = new HashMap<>();
    /**
     * The registered factories
     */
    private final Collection<DomainConnectorFactory> factories = new ArrayList<>(8);
    /**
     * The map of statically configured connectors to resolve
     */
    private Map<String, Section> toResolve;

    @Override
    public String getIdentifier() {
        return XOWLDomainDirectoryService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Domain Directory Service";
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
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (method.equals("GET") && parameters.isEmpty()) {
            if (uri.equals(URI_API + "/domain"))
                return onMessageListDomains();
            if (uri.equals(URI_API + "/connectors"))
                return onMessageListConnectors();
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        }
        if (!method.equals("POST"))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);

        String[] actions = parameters.get("action");
        String action = actions != null && actions.length >= 1 ? actions[0] : null;
        if (action != null && action.equals("spawn"))
            return onMessageCreateConnector(parameters, content, contentType);
        if (action != null && action.equals("delete"))
            return onMessageDeleteConnector(parameters);
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Override
    public Collection<DomainConnectorService> getConnectors() {
        resolveConfigConnectors(null);
        return ServiceUtils.getServices(DomainConnectorService.class);
    }

    @Override
    public DomainConnectorService get(String identifier) {
        resolveConfigConnectors(null);
        Registration registration = connectorsById.get(identifier);
        if (registration != null)
            return registration.service;
        return ServiceUtils.getService(DomainConnectorService.class, "id", identifier);
    }

    @Override
    public Collection<DomainDescription> getDomains() {
        Collection<DomainDescription> result = new ArrayList<>(16);
        for (DomainConnectorFactory factory : factories) {
            result.addAll(factory.getDomains());
        }
        return result;
    }

    @Override
    public DomainConnectorService spawn(DomainDescription description, String identifier, String name, String[] uris, Map<DomainDescriptionParam, Object> parameters) {
        synchronized (connectorsById) {
            DomainConnectorService service = get(identifier);
            if (service != null)
                // already exists
                return null;

            for (DomainConnectorFactory factory : factories) {
                if (factory.getDomains().contains(description)) {
                    // this is the factory
                    service = factory.newConnector(description, identifier, name, uris, parameters);
                    break;
                }
            }
            if (service == null)
                // failed to create the service (factory not fond?)
                return null;

            Registration registration = new Registration();
            registration.service = service;
            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put("id", identifier);
            if (uris != null)
                properties.put("uri", uris);
            BundleContext context = FrameworkUtil.getBundle(DomainConnectorService.class).getBundleContext();
            registration.refAsDomainConnector = context.registerService(DomainConnectorService.class, service, properties);
            registration.refAsServedService = context.registerService(HttpAPIService.class, service, properties);
            connectorsById.put(identifier, registration);
            return service;
        }
    }

    @Override
    public boolean delete(String identifier) {
        synchronized (connectorsById) {
            Registration registration = connectorsById.remove(identifier);
            if (registration == null)
                return false;
            registration.refAsDomainConnector.unregister();
            registration.refAsServedService.unregister();
            return true;
        }
    }

    /**
     * When a new connector factory service comes online
     *
     * @param factory The new factory
     */
    public void onFactoryOnline(DomainConnectorFactory factory) {
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
    public void onFactoryOffline(DomainConnectorFactory factory) {
        synchronized (factories) {
            factories.remove(factory);
        }
    }

    /**
     * Resolve statically configured connectors
     *
     * @param factory The new factory, if any
     */
    private void resolveConfigConnectors(DomainConnectorFactory factory) {
        if (toResolve == null) {
            ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
            if (configurationService == null)
                return;
            Configuration configuration = configurationService.getConfigFor(this);
            if (configuration == null)
                return;
            toResolve = new HashMap<>();
            for (Section section : configuration.getSections()) {
                String domain = section.get("domain");
                if (domain == null)
                    continue;
                toResolve.put(domain, section);
            }
        }
        for (Map.Entry<String, Section> entry : toResolve.entrySet()) {
            if (factory != null) {
                // this is a new factory
                for (DomainDescription domain : factory.getDomains()) {
                    if (domain.getIdentifier().equals(entry.getKey())) {
                        resolveConfigConnector(domain, entry.getValue());
                        break;
                    }
                }
            } else {
                synchronized (factories) {
                    for (DomainConnectorFactory existingFactory : factories) {
                        boolean found = false;
                        for (DomainDescription domain : existingFactory.getDomains()) {
                            if (domain.getIdentifier().equals(entry.getKey())) {
                                resolveConfigConnector(domain, entry.getValue());
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
    }

    /**
     * Resolves a statically configured connector
     *
     * @param domain  The domain
     * @param section The configuration
     */
    private void resolveConfigConnector(DomainDescription domain, Section section) {
        String id = section.getName();
        String name = section.get("name");
        if (id == null || name == null)
            return;
        List<String> uris = section.getAll("uris");
        Map<DomainDescriptionParam, Object> customParams = new HashMap<>();
        for (String property : section.getProperties()) {
            if (property.equals("name") || property.equals("uris"))
                continue;
            DomainDescriptionParam parameter = null;
            for (DomainDescriptionParam p : domain.getParameters()) {
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
        DomainConnectorService service = spawn(domain, id, name, (String[]) uris.toArray(), customParams);
        if (service != null) {
            toResolve.remove(domain.getIdentifier());
        }
    }

    /**
     * Responds to a request for the list of the connectors
     *
     * @return The response
     */
    private HttpResponse onMessageListConnectors() {
        Collection<DomainConnectorService> connectors = ServiceUtils.getServices(DomainConnectorService.class);
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (DomainConnectorService connector : connectors) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(connector.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for the list of the domains
     *
     * @return The response
     */
    private HttpResponse onMessageListDomains() {
        Collection<DomainDescription> domains = getDomains();
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (DomainDescription domain : domains) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(domain.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, builder.toString());
    }

    /**
     * Responds to the request to spawn a new parametric connector
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onMessageCreateConnector(Map<String, String[]> parameters, byte[] content, String contentType) {
        String[] domainIds = parameters.get("domain");
        if (domainIds == null || domainIds.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Expected domain parameter");
        if (content == null || content.length == 0 || !IOUtils.MIME_JSON.equals(contentType))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Expected JSON content");

        BufferedLogger logger = new BufferedLogger();
        ASTNode root = Utils.parseJSON(logger, new String(content, Utils.DEFAULT_CHARSET));
        if (root == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, Utils.getLog(logger));

        DomainDescription domain = null;
        for (DomainDescription description : getDomains()) {
            if (description.getIdentifier().equals(domainIds[0])) {
                domain = description;
                break;
            }
        }
        if (domain == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Failed to find domain " + domainIds[0]);

        String id = null;
        String name = null;
        List<String> uris = new ArrayList<>(2);
        Map<DomainDescriptionParam, Object> customParams = new HashMap<>();
        for (ASTNode member : root.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            switch (head) {
                case "identifier":
                    id = IOUtils.unescape(member.getChildren().get(1).getValue());
                    id = id.substring(1, id.length() - 1);
                    break;
                case "name":
                    name = IOUtils.unescape(member.getChildren().get(1).getValue());
                    name = name.substring(1, name.length() - 1);
                    break;
                case "uris": {
                    ASTNode valueNode = member.getChildren().get(1);
                    if (valueNode.getValue() != null) {
                        String value = IOUtils.unescape(valueNode.getValue());
                        value = value.substring(1, value.length() - 1);
                        uris.add(value);
                    } else if (valueNode.getChildren().size() > 0) {
                        for (ASTNode childNode : valueNode.getChildren()) {
                            String value = IOUtils.unescape(childNode.getValue());
                            value = value.substring(1, value.length() - 1);
                            uris.add(value);
                        }
                    }
                    break;
                }
                default: {
                    DomainDescriptionParam parameter = null;
                    for (DomainDescriptionParam p : domain.getParameters()) {
                        if (p.getIdentifier().equals(head)) {
                            parameter = p;
                            break;
                        }
                    }
                    if (parameter != null) {
                        ASTNode valueNode = member.getChildren().get(1);
                        if (valueNode.getValue() != null) {
                            String value = IOUtils.unescape(valueNode.getValue());
                            if (value.startsWith("\"") && value.endsWith("\""))
                                value = value.substring(1, value.length() - 1);
                            customParams.put(parameter, value);
                        } else if (valueNode.getChildren().size() > 0) {
                            String[] values = new String[valueNode.getChildren().size()];
                            for (int i = 0; i != valueNode.getChildren().size(); i++) {
                                String value = IOUtils.unescape(valueNode.getChildren().get(i).getValue());
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
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Identifier for connector not specified");
        if (name == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Name for connector not specified");

        DomainConnectorService connector = spawn(domain, id, name, (String[]) uris.toArray(), customParams);
        return new HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, connector.serializedJSON());
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
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Expected an id parameter");
        boolean success = delete(ids[0]);
        if (success)
            return new HttpResponse(HttpURLConnection.HTTP_OK);
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Failed to delete connector with id " + ids[0]);
    }
}
