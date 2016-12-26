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

import org.xowl.platform.kernel.platform.Addon;
import org.xowl.platform.services.marketplace.Category;
import org.xowl.platform.services.marketplace.Marketplace;
import org.xowl.platform.services.marketplace.MarketplaceDescriptor;

import java.util.*;

/**
 * Basic implementation of a static marketplace
 */
abstract class StaticMarketplace implements Marketplace {
    /**
     * The descriptor file for a file-system marketplace
     */
    protected static final String MARKETPLACE_DESCRIPTOR = "marketplace.json";

    /**
     * A proxy for the lazy loading of an addon descriptor
     */
    protected class Proxy {
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
     * The categories in this marketplace
     */
    protected final Map<String, Category> categories;
    /**
     * All the addons in this marketplace
     */
    protected final Map<String, Proxy> addons;
    /**
     * The addons, by category
     */
    protected final Map<String, Collection<Proxy>> addonsByCategory;
    /**
     * Whether this marketplace must be reloaded
     */
    protected boolean mustReload;

    public StaticMarketplace() {
        this.categories = new HashMap<>();
        this.addons = new HashMap<>();
        this.addonsByCategory = new HashMap<>();
        this.mustReload = true;
    }


    /**
     * Gets whether the specified content string contains the terms to look for
     *
     * @param content A string
     * @param terms   The list of terms to look for in the content
     * @return Whether the string contains all the terms
     */
    protected boolean stringMatches(String content, Collection<String> terms) {
        for (String term : terms) {
            if (!content.contains(term))
                return false;
        }
        return true;
    }

    /**
     * Loads the content of this marketplace
     */
    protected synchronized void loadContent() {
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

    /**
     * Loads the marketplace descriptor
     *
     * @return The marketplace descriptor
     */
    protected abstract MarketplaceDescriptor loadMarketplaceDescriptor();

    /**
     * Loads an addon descriptor
     *
     * @param identifier The identifier of the addon to load
     * @return The addon descriptor
     */
    protected abstract Addon loadAddonDescriptor(String identifier);
}
