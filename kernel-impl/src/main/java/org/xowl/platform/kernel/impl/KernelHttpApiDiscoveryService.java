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
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
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
class KernelHttpApiDiscoveryService implements HttpApiDiscoveryService, HttpApiService {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(KernelHttpApiDiscoveryService.class, "/org/xowl/platform/kernel/impl/api_discovery.raml", "API Discovery Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(KernelHttpApiDiscoveryService.class, "/org/xowl/platform/kernel/impl/api_discovery.html", "API Discovery Service - Documentation", HttpApiResource.MIME_HTML);
    /**
     * The default API resources for the platform
     */
    private static final HttpApiResource[] RESOURCE_DEFAULTS = new HttpApiResource[]{
            new HttpApiResourceBase(KernelHttpApiDiscoveryService.class, "/org/xowl/platform/kernel/impl/api_traits.raml", "Standard Traits", HttpApiResource.MIME_RAML),
            new HttpApiResourceBase(KernelHttpApiDiscoveryService.class, "/org/xowl/platform/kernel/impl/schema_infra_utils.json", "Schema - xOWL Infrastructure", HttpConstants.MIME_JSON),
            new HttpApiResourceBase(KernelHttpApiDiscoveryService.class, "/org/xowl/platform/kernel/impl/schema_platform_kernel.json", "Schema - xOWL Platform - Kernel", HttpConstants.MIME_JSON)
    };

    /**
     * The URI for the API services
     */
    private final String apiUri;

    /**
     * Initializes this service
     */
    public KernelHttpApiDiscoveryService() {
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/kernel/discovery";
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
        return Register.getComponents(HttpApiService.class);
    }

    @Override
    public Collection<HttpApiResource> getResources() {
        Map<String, HttpApiResource> resources = new HashMap<>();
        for (HttpApiService httpApiService : getServices()) {
            HttpApiResource resource = httpApiService.getApiSpecification();
            if (resource != null)
                resources.put(resource.getIdentifier(), resource);
            resource = httpApiService.getApiDocumentation();
            if (resource != null)
                resources.put(resource.getIdentifier(), resource);
            HttpApiResource[] additionalResources = httpApiService.getApiOtherResources();
            if (additionalResources != null) {
                for (int i = 0; i != additionalResources.length; i++) {
                    resources.put(additionalResources[i].getIdentifier(), additionalResources[i]);
                    resources.put(additionalResources[i].getIdentifier(), additionalResources[i]);
                }
            }
        }
        return resources.values();
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
        if (request.getUri().equals(apiUri + "/services")) {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (HttpApiService service : getServices()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(service.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else if (request.getUri().equals(apiUri + "/resources")) {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (HttpApiResource resource : getResources()) {
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
}
