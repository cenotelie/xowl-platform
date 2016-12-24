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
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.platform.Addon;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.marketplace.Category;
import org.xowl.platform.services.marketplace.Marketplace;
import org.xowl.platform.services.marketplace.MarketplaceProvider;
import org.xowl.platform.services.marketplace.MarketplaceService;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements a marketplace service for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLMarketplaceService implements MarketplaceService {
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
     * The resource for the API's schema
     */
    private static final HttpApiResource RESOURCE_SCHEMA = new HttpApiResourceBase(XOWLMarketplaceService.class, "/org/xowl/platform/services/marketplace/schema_platform_marketplace.json", "Marketplace Service - Schema", HttpConstants.MIME_JSON);

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
        return "xOWL Federation Platform - Marketplace Service";
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
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
    public Collection<Category> getCategories() {
        Collection<Category> categories = new ArrayList<>();
        for (Marketplace marketplace : getMarketplaces()) {
            categories.addAll(marketplace.getCategories());
        }
        return categories;
    }

    @Override
    public Collection<Addon> lookupAddons(String identifier, String name, String categoryId) {
        Collection<Addon> result = new ArrayList<>();
        for (Marketplace marketplace : getMarketplaces()) {
            result.addAll(marketplace.lookupAddons(identifier, name, categoryId));
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
    public XSPReply beginInstallOf(String identifier) {
        return XSPReplyUnsupported.instance();
    }
}
