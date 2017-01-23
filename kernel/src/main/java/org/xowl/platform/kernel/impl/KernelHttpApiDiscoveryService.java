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
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.*;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation the HTTP API discovery service
 *
 * @author Laurent Wouters
 */
public class KernelHttpApiDiscoveryService implements HttpApiDiscoveryService, HttpApiService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/kernel/discovery";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_discovery.raml", "API Discovery Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_discovery.html", "API Discovery Service - Documentation", HttpApiResource.MIME_HTML);
    /**
     * The default API resources for the platform
     */
    private static final HttpApiResource[] RESOURCE_DEFAULTS = new HttpApiResource[]{
            new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_traits.raml", "Standard Traits", HttpApiResource.MIME_RAML),
            new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/schema_infra_utils.json", "Schema - xOWL Infrastructure", HttpConstants.MIME_JSON),
            new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/schema_platform_kernel.json", "Schema - xOWL Platform - Kernel", HttpConstants.MIME_JSON)
    };

    /**
     * The known services
     */
    private final Map<String, HttpApiService> services;
    /**
     * All the known resources
     */
    private final Map<String, HttpApiResource> allResources;
    /**
     * The other resources for the API documentation
     */
    private final Map<String, HttpApiResource> otherResources;

    /**
     * Initializes this service
     */
    public KernelHttpApiDiscoveryService() {
        this.services = new HashMap<>();
        this.allResources = new HashMap<>();
        this.otherResources = new HashMap<>();
    }

    @Override
    public String getIdentifier() {
        return KernelHttpApiDiscoveryService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - API Discovery Service";
    }

    @Override
    public Collection<HttpApiService> getServices() {
        return services.values();
    }

    @Override
    public Collection<HttpApiResource> getResources() {
        return allResources.values();
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
        if (request.getUri().equals(URI_API + "/services")) {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (HttpApiService service : services.values()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(service.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else if (request.getUri().equals(URI_API + "/resources")) {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (HttpApiResource resource : otherResources.values()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(resource.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
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
        return RESOURCE_DEFAULTS;
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
     * Registers an API service
     *
     * @param service The service to register
     */
    public void registerService(HttpApiService service) {
        services.put(service.getIdentifier(), service);
        HttpApiResource resource = service.getApiSpecification();
        if (resource != null)
            allResources.put(resource.getIdentifier(), resource);
        resource = service.getApiDocumentation();
        if (resource != null)
            allResources.put(resource.getIdentifier(), resource);
        HttpApiResource[] resources = service.getApiOtherResources();
        if (resources != null) {
            for (int i = 0; i != resources.length; i++) {
                this.allResources.put(resources[i].getIdentifier(), resources[i]);
                this.otherResources.put(resources[i].getIdentifier(), resources[i]);
            }
        }
    }

    /**
     * Un-registers an API service
     *
     * @param service The service to un-register
     */
    public void unregisterService(HttpApiService service) {
        services.remove(service.getIdentifier());
        HttpApiResource resource = service.getApiSpecification();
        if (resource != null)
            allResources.remove(resource.getIdentifier());
        resource = service.getApiDocumentation();
        if (resource != null)
            allResources.remove(resource.getIdentifier());
        HttpApiResource[] resources = service.getApiOtherResources();
        if (resources != null) {
            for (int i = 0; i != resources.length; i++) {
                this.allResources.remove(resources[i].getIdentifier());
                this.otherResources.remove(resources[i].getIdentifier());
            }
        }
    }
}
