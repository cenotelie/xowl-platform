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

package org.xowl.platform.services.marketplace.impl;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.platform.Addon;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.marketplace.Marketplace;
import org.xowl.platform.services.marketplace.MarketplaceProvider;
import org.xowl.platform.services.marketplace.MarketplaceService;
import org.xowl.platform.services.marketplace.jobs.AddonInstallationJob;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements a marketplace service for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLMarketplaceService implements MarketplaceService, HttpApiService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/services/marketplace";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLMarketplaceService.class, "/org/xowl/platform/services/marketplace/api_service_marketplace.raml", "Marketplace Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLMarketplaceService.class, "/org/xowl/platform/services/marketplace/api_service_marketplace.html", "Marketplace Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The available marketplaces
     */
    private Collection<Marketplace> marketplaces;

    /**
     * Gets the marketplaces
     *
     * @return The marketplaces
     */
    private synchronized Collection<Marketplace> getMarketplaces() {
        if (marketplaces != null)
            return Collections.unmodifiableCollection(marketplaces);
        marketplaces = new ArrayList<>();
        ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
        if (configurationService == null)
            return Collections.unmodifiableCollection(marketplaces);
        Configuration configuration = configurationService.getConfigFor(this);
        if (configuration == null)
            return Collections.unmodifiableCollection(marketplaces);
        Collection<MarketplaceProvider> providers = ServiceUtils.getServices(MarketplaceProvider.class);
        for (Section section : configuration.getSections()) {
            String type = section.get("type");
            if (type == null || type.isEmpty())
                continue;
            for (MarketplaceProvider provider : providers) {
                if (provider.supports(type)) {
                    Marketplace marketplace = provider.newMarketplace(type, section);
                    if (marketplace != null) {
                        marketplaces.add(marketplace);
                        break;
                    }
                }
            }
        }
        return Collections.unmodifiableCollection(marketplaces);
    }

    @Override
    public String getIdentifier() {
        return XOWLMarketplaceService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - Marketplace Service";
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/addons")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            String[] inputs = request.getParameter("input");
            Collection<Addon> addons = lookupAddons(inputs != null && inputs.length > 0 ? inputs[0] : null);
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (Addon addon : addons) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(addon.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else if (request.getUri().startsWith(URI_API + "/addons")) {
            String rest = request.getUri().substring(URI_API.length() + "/addons".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String addonId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                Addon addon = getAddon(addonId);
                if (addon == null)
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, addon.serializedJSON());
            }
            rest = rest.substring(index);
            if (rest.equals("/install")) {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                // check for platform admin role
                SecurityService securityService = ServiceUtils.getService(SecurityService.class);
                if (securityService == null)
                    return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
                XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
                if (!reply.isSuccess())
                    return XSPReplyUtils.toHttpResponse(reply, null);
                return XSPReplyUtils.toHttpResponse(beginInstallOf(addonId), null);
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

    @Override
    public Collection<Addon> lookupAddons(String input) {
        Collection<Addon> result = new ArrayList<>();
        for (Marketplace marketplace : getMarketplaces()) {
            result.addAll(marketplace.lookupAddons(input));
        }
        return result;
    }

    @Override
    public Addon getAddon(String identifier) {
        for (Marketplace marketplace : getMarketplaces()) {
            Addon addon = marketplace.getAddon(identifier);
            if (addon != null)
                return addon;
        }
        return null;
    }

    @Override
    public InputStream getAddonPackage(String identifier) {
        for (Marketplace marketplace : getMarketplaces()) {
            InputStream stream = marketplace.getAddonPackage(identifier);
            if (stream != null)
                return stream;
        }
        return null;
    }

    @Override
    public XSPReply beginInstallOf(String identifier) {
        JobExecutionService service = ServiceUtils.getService(JobExecutionService.class);
        if (service == null)
            return XSPReplyServiceUnavailable.instance();
        Job job = new AddonInstallationJob(identifier);
        service.schedule(job);
        return new XSPReplyResult<>(job);
    }
}
