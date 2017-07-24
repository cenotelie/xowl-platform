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

import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.api.ReplyResult;
import org.xowl.infra.utils.api.ReplyUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.platform.Addon;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.security.SecuredAction;
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
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLMarketplaceService.class, "/org/xowl/platform/services/marketplace/api_service_marketplace.raml", "Marketplace Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLMarketplaceService.class, "/org/xowl/platform/services/marketplace/api_service_marketplace.html", "Marketplace Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The URI for the API services
     */
    private final String apiUri;
    /**
     * The available marketplaces
     */
    private Collection<Marketplace> marketplaces;

    /**
     * Initializes this service
     */
    public XOWLMarketplaceService() {
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/services/marketplace";
    }

    /**
     * Gets the marketplaces
     *
     * @return The marketplaces
     */
    private synchronized Collection<Marketplace> getMarketplaces() {
        if (marketplaces != null)
            return Collections.unmodifiableCollection(marketplaces);
        marketplaces = new ArrayList<>();
        ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
        if (configurationService == null)
            return Collections.unmodifiableCollection(marketplaces);
        Configuration configuration = configurationService.getConfigFor(MarketplaceService.class.getCanonicalName());
        if (configuration == null)
            return Collections.unmodifiableCollection(marketplaces);
        Collection<MarketplaceProvider> providers = Register.getComponents(MarketplaceProvider.class);
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
        return PlatformUtils.NAME + " - Marketplace Service";
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
        if (request.getUri().equals(apiUri + "/addons")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            String input = request.getParameter("input");
            Collection<Addon> addons = lookupAddons(input);
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
        } else if (request.getUri().startsWith(apiUri + "/addons")) {
            String rest = request.getUri().substring(apiUri.length() + "/addons".length() + 1);
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
                return ReplyUtils.toHttpResponse(beginInstallOf(addonId), null);
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
    public Reply beginInstallOf(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(PlatformManagementService.ACTION_INSTALL_ADDON);
        if (!reply.isSuccess())
            return reply;
        JobExecutionService service = Register.getComponent(JobExecutionService.class);
        if (service == null)
            return ReplyServiceUnavailable.instance();
        Job job = new AddonInstallationJob(identifier);
        service.schedule(job);
        return new ReplyResult<>(job);
    }
}
