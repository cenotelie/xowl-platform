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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConnection;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.platform.Addon;
import org.xowl.platform.services.marketplace.Category;
import org.xowl.platform.services.marketplace.MarketplaceDescriptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements a static marketplace that is online
 *
 * @author Laurent Wouters
 */
class OnlineMarketplace extends StaticMarketplace {
    /**
     * A proxy for the lazy loading of an addon descriptor
     */
    private class Proxy {
        /**
         * The identifier of the addon
         */
        public final String identifier;
        /**
         * The cache
         */
        private Addon cache;

        /**
         * Initializes this proxy
         *
         * @param identifier The identifier of the addon
         */
        public Proxy(String identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets the addon descriptor
         *
         * @return Tha addon descriptor
         */
        public synchronized Addon getDescriptor() {
            if (cache != null)
                return cache;
            cache = loadAddonDescriptor(identifier);
            return cache;
        }
    }

    /**
     * The URL location
     */
    private final String location;
    /**
     * The HTTP connection to use
     */
    private final HttpConnection connection;
    /**
     * The categories in this marketplace
     */
    private final Map<String, Category> categories;
    /**
     * All the addons in this marketplace
     */
    private final Map<String, Proxy> addons;
    /**
     * The addons, by category
     */
    private final Map<String, Collection<Proxy>> addonsByCategory;
    /**
     * Whether this marketplace must be reloaded
     */
    private boolean mustReload;

    /**
     * Initializes this marketplace
     *
     * @param configuration The configuration for this marketplace
     */
    public OnlineMarketplace(Section configuration) {
        String url = configuration.get("url");
        this.location = url.endsWith("/") ? url : (url + "/");
        this.connection = new HttpConnection(
                this.location,
                configuration.get("login"),
                configuration.get("password")
        );
        this.categories = new HashMap<>();
        this.addons = new HashMap<>();
        this.addonsByCategory = new HashMap<>();
        this.mustReload = true;
    }

    /**
     * Loads the content of this marketplace
     */
    private synchronized void loadContent() {
        if (!mustReload)
            return;
        MarketplaceDescriptor descriptor = loadMarketplaceDescriptor();
        if (descriptor == null)
            return;
        categories.clear();
        addons.clear();
        addonsByCategory.clear();
        for (Category category : descriptor.getCategories()) {
            categories.put(category.getIdentifier(), category);
        }
        for (MarketplaceDescriptor.Addon addonDescriptor : descriptor.getAddons()) {
            Proxy proxy = new Proxy(addonDescriptor.identifier);
            addons.put(addonDescriptor.identifier, proxy);
            for (int i = 0; i != addonDescriptor.categories.length; i++) {
                Collection<Proxy> forCategory = addonsByCategory.get(addonDescriptor.categories[i]);
                if (forCategory == null) {
                    forCategory = new ArrayList<>();
                    addonsByCategory.put(addonDescriptor.categories[i], forCategory);
                }
                forCategory.add(proxy);
            }
        }
        mustReload = false;
    }

    /**
     * Loads the marketplace descriptor
     *
     * @return The marketplace descriptor
     */
    private MarketplaceDescriptor loadMarketplaceDescriptor() {
        HttpResponse response = connection.request(MARKETPLACE_DESCRIPTOR, "GET", HttpConstants.MIME_JSON);
        if (response.getCode() != HttpURLConnection.HTTP_OK) {
            Logging.getDefault().error("Cannot find marketplace descriptor " + location + MARKETPLACE_DESCRIPTOR + " (" + response.getCode() + ")");
            return null;
        }
        String content = response.getBodyAsString();
        if (content == null) {
            Logging.getDefault().error("Marketplace descriptor is empty " + location + MARKETPLACE_DESCRIPTOR);
            return null;
        }
        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
        if (definition == null) {
            Logging.getDefault().error("Failed to parse marketplace descriptor " + location + MARKETPLACE_DESCRIPTOR);
            return null;
        }
        return new MarketplaceDescriptor(definition);
    }

    /**
     * Loads an addon descriptor
     *
     * @param identifier The identifier of the addon to load
     * @return The addon descriptor
     */
    private Addon loadAddonDescriptor(String identifier) {
        HttpResponse response = connection.request(identifier + ".descriptor", "GET", HttpConstants.MIME_JSON);
        if (response.getCode() != HttpURLConnection.HTTP_OK) {
            Logging.getDefault().error("Cannot find addon descriptor " + location + identifier + ".descriptor (" + response.getCode() + ")");
            return null;
        }
        String content = response.getBodyAsString();
        if (content == null) {
            Logging.getDefault().error("Addon descriptor is empty " + location + identifier + ".descriptor");
            return null;
        }
        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
        if (definition == null) {
            Logging.getDefault().error("Failed to parse addon descriptor  " + location + identifier + ".descriptor");
            return null;
        }
        return new Addon(definition);
    }

    @Override
    public Collection<Category> getCategories() {
        loadContent();
        return Collections.unmodifiableCollection(categories.values());
    }

    @Override
    public Collection<Addon> lookupAddons(String input, String categoryId) {
        loadContent();
        // is this an exact ID match
        if (input != null) {
            Proxy proxy = addons.get(input);
            if (proxy != null)
                return Collections.singletonList(proxy.getDescriptor());
        }
        // get the collection for the category
        Collection<Proxy> collection;
        if (categoryId != null)
            collection = addonsByCategory.get(categoryId);
        else
            collection = addons.values();
        if (collection == null)
            return Collections.emptyList();
        Collection<Addon> result = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            for (Proxy proxy : collection)
                result.add(proxy.getDescriptor());
            return result;
        }
        String[] values = input.split(" ");
        Collection<String> terms = new ArrayList<>(values.length);
        for (int i = 0; i != values.length; i++) {
            if (!values[i].isEmpty())
                terms.add(values[i].toLowerCase());
        }
        for (Proxy proxy : collection) {
            Addon addon = proxy.getDescriptor();
            if (stringMatches(addon.getIdentifier().toLowerCase(), terms)
                    || stringMatches(addon.getName().toLowerCase(), terms)
                    || stringMatches(addon.getDescription().toLowerCase(), terms)) {
                result.add(addon);
            }
        }
        return result;
    }

    @Override
    public Addon getAddon(String identifier) {
        loadContent();
        Proxy proxy = addons.get(identifier);
        if (proxy == null)
            return null;
        return proxy.getDescriptor();
    }

    @Override
    public InputStream getAddonPackage(String identifier) {
        loadContent();
        Proxy proxy = addons.get(identifier);
        if (proxy == null)
            return null;
        HttpResponse response = connection.request(identifier + ".zip", "GET", HttpConstants.MIME_JSON);
        if (response.getCode() != HttpURLConnection.HTTP_OK) {
            Logging.getDefault().error("Cannot find addon package " + location + identifier + ".zip (" + response.getCode() + ")");
            return null;
        }
        byte[] content = response.getBodyAsBytes();
        return new ByteArrayInputStream(content);
    }
}
