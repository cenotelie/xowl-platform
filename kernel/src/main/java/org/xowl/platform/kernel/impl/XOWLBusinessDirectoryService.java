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

import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.platform.kernel.artifacts.ArtifactArchetype;
import org.xowl.platform.kernel.artifacts.BusinessDirectoryService;
import org.xowl.platform.kernel.artifacts.BusinessDomain;
import org.xowl.platform.kernel.artifacts.BusinessSchema;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the business directory service
 *
 * @author Laurent Wouters
 */
public class XOWLBusinessDirectoryService implements BusinessDirectoryService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/kernel/business";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_business.raml", "Business Directory Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_business.html", "Business Directory Service - Documentation", HttpApiResource.MIME_HTML);


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
        return "xOWL Collaboration Platform - Business Directory Service";
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
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
        if (request.getUri().equals(URI_API + "/archetypes")) {
            return onGetArchetypes();
        } else if (request.getUri().equals(URI_API + "/domains")) {
            return onGetDomains();
        } else if (request.getUri().equals(URI_API + "/schemas")) {
            return onGetSchemas();
        } else if (request.getUri().startsWith(URI_API + "/archetypes")) {
            String rest = URIUtils.decodeComponent(request.getUri().substring(URI_API.length() + "/archetypes".length() + 1));
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            return onGetArchetype(rest);
        } else if (request.getUri().startsWith(URI_API + "/domains")) {
            String rest = URIUtils.decodeComponent(request.getUri().substring(URI_API.length() + "/domains".length() + 1));
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            return onGetDomain(rest);
        } else if (request.getUri().startsWith(URI_API + "/schemas")) {
            String rest = URIUtils.decodeComponent(request.getUri().substring(URI_API.length() + "/schemas".length() + 1));
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            return onGetSchema(rest);
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
        return null;
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
