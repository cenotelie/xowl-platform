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

package org.xowl.platform.kernel.impl;

import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.artifacts.ArtifactArchetype;
import org.xowl.platform.kernel.artifacts.BusinessDirectoryService;
import org.xowl.platform.kernel.artifacts.BusinessDomain;
import org.xowl.platform.kernel.artifacts.BusinessSchema;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements the business directory service
 *
 * @author Laurent Wouters
 */
public class XOWLBusinessDirectoryService implements BusinessDirectoryService {
    /**
     * The URIs for this service
     */
    private static final String[] URIS = new String[]{
            "services/core/business/archetypes",
            "services/core/business/archetype",
            "services/core/business/domains",
            "services/core/business/domain",
            "services/core/business/schemas",
            "services/core/business/schema"
    };

    /**
     * The registered domains
     */
    private final Map<String, BusinessDomain> domains;
    /**
     * The registered schemas
     */
    private final Map<String, BusinessSchema> schemas;
    /**
     * The registered archetypes
     */
    private final Map<String, ArtifactArchetype> archetypes;

    /**
     * Initializes this service
     */
    public XOWLBusinessDirectoryService() {
        this.domains = new HashMap<>();
        this.schemas = new HashMap<>();
        this.archetypes = new HashMap<>();
    }

    @Override
    public String getIdentifier() {
        return XOWLBusinessDirectoryService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Business Directory Service";
    }

    @Override
    public Collection<BusinessDomain> getDomains() {
        return Collections.unmodifiableCollection(domains.values());
    }

    @Override
    public Collection<BusinessSchema> getSchemas() {
        return Collections.unmodifiableCollection(schemas.values());
    }

    @Override
    public Collection<ArtifactArchetype> getArchetypes() {
        return Collections.unmodifiableCollection(archetypes.values());
    }

    @Override
    public BusinessDomain getDomain(String identifier) {
        return domains.get(identifier);
    }

    @Override
    public BusinessSchema getSchema(String identifier) {
        return schemas.get(identifier);
    }

    @Override
    public ArtifactArchetype getArchetype(String identifier) {
        return archetypes.get(identifier);
    }

    @Override
    public void register(BusinessDomain domain) {
        this.domains.put(domain.getIdentifier(), domain);
    }

    @Override
    public void register(ArtifactArchetype archetype) {
        this.archetypes.put(archetype.getIdentifier(), archetype);
    }

    @Override
    public void register(BusinessSchema schema) {
        this.schemas.put(schema.getIdentifier(), schema);
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIS);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (!method.equals("GET"))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD);
        String[] ids = parameters.get("id");
        switch (uri) {
            case "services/core/business/archetypes":
                return onGetArchetypes();
            case "services/core/business/domains":
                return onGetDomains();
            case "services/core/business/schemas":
                return onGetSchemas();
            case "services/core/business/archetype":
                if (ids == null || ids.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return onGetArchetype(ids[0]);
            case "services/core/business/domain":
                if (ids == null || ids.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return onGetDomain(ids[0]);
            case "services/core/business/schema":
                if (ids == null || ids.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return onGetSchema(ids[0]);
        }
        return null;
    }

    /**
     * Responds to a request for the registered archetypes
     *
     * @return The response
     */
    private HttpResponse onGetArchetypes() {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (ArtifactArchetype archetype : getArchetypes()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(archetype.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for a registered archetypes
     *
     * @return The response
     */
    private HttpResponse onGetArchetype(String identifier) {
        ArtifactArchetype archetype = getArchetype(identifier);
        if (archetype != null)
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, archetype.serializedJSON());
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Responds to a request for the registered domains
     *
     * @return The response
     */
    private HttpResponse onGetDomains() {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (BusinessDomain domain : getDomains()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(domain.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for a registered domain
     *
     * @return The response
     */
    private HttpResponse onGetDomain(String identifier) {
        BusinessDomain domain = getDomain(identifier);
        if (domain != null)
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, domain.serializedJSON());
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Responds to a request for the registered schemas
     *
     * @return The response
     */
    private HttpResponse onGetSchemas() {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (BusinessSchema schema : getSchemas()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(schema.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for a registered domain
     *
     * @return The response
     */
    private HttpResponse onGetSchema(String identifier) {
        BusinessSchema schema = getSchema(identifier);
        if (schema != null)
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, schema.serializedJSON());
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }
}
