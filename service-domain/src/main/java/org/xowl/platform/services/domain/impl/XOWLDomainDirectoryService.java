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
import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.domain.DomainConnectorService;
import org.xowl.platform.services.domain.DomainDirectoryService;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.store.IOUtils;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Implements a directory service for the domain connectors
 *
 * @author Laurent Wouters
 */
public class XOWLDomainDirectoryService implements DomainDirectoryService {
    /**
     * The spawned connectors
     */
    private final Map<String, ParametricDomainConnector> parametricConnectors = new HashMap<>();

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
        if (method.equals("GET") && parameters.isEmpty())
            return onMessageListConnectors();
        String[] actions = parameters.get("action");
        String action = actions != null && actions.length >= 1 ? actions[0] : null;
        if (action != null && action.equals("spawn") && (method.equals("POST") || method.equals("GET")))
            return onMessageCreateConnector(parameters);
        if (action != null && action.equals("delete") && (method.equals("POST") || method.equals("GET")))
            return onMessageDeleteConnector(parameters);
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Override
    public Collection<DomainConnectorService> getConnectors() {
        return ServiceUtils.getServices(DomainConnectorService.class);
    }

    @Override
    public DomainConnectorService get(String identifier) {
        DomainConnectorService service = parametricConnectors.get(identifier);
        if (service != null)
            return service;
        return ServiceUtils.getService(DomainConnectorService.class, "id", identifier);
    }

    @Override
    public DomainConnectorService spawn(String identifier, String name, String[] uris) {
        synchronized (parametricConnectors) {
            DomainConnectorService service = get(identifier);
            if (service != null)
                // already exists
                return null;
            BundleContext context = FrameworkUtil.getBundle(DomainConnectorService.class).getBundleContext();
            ParametricDomainConnector connector = new ParametricDomainConnector(identifier, name);
            parametricConnectors.put(identifier, connector);
            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put("id", identifier);
            if (uris != null)
                properties.put("uri", uris);
            connector.refAsDomainConnector = context.registerService(DomainConnectorService.class, connector, properties);
            connector.refAsServedService = context.registerService(HttpAPIService.class, connector, properties);
            return connector;
        }
    }

    @Override
    public boolean delete(String identifier) {
        synchronized (parametricConnectors) {
            ParametricDomainConnector connector = parametricConnectors.get(identifier);
            if (connector == null)
                return false;
            connector.refAsDomainConnector.unregister();
            connector.refAsServedService.unregister();
            parametricConnectors.remove(identifier);
            return true;
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
     * Responds to the request to spawn a new parametric connector
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onMessageCreateConnector(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        String[] names = parameters.get("name");
        String[] uris = parameters.get("uris");
        if (ids == null || ids.length == 0 || names == null || names.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Expected at least one id and one name parameter");
        DomainConnectorService connector = spawn(ids[0], names[0], uris);
        if (connector != null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "The connector already exist");
        return new HttpResponse(HttpURLConnection.HTTP_OK);
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
