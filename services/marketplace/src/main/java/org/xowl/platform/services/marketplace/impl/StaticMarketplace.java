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
     * All the addons in this marketplace
     */
    protected final Map<String, Proxy> addons;
    /**
     * Whether this marketplace must be reloaded
     */
    protected boolean mustReload;

    public StaticMarketplace() {
        this.addons = new HashMap<>();
        this.mustReload = true;
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
        addons.clear();
        for (String addonId : descriptor.getAddons()) {
            Proxy proxy = new Proxy(addonId);
            addons.put(addonId, proxy);
        }
        mustReload = false;
    }

    @Override
    public Collection<Addon> lookupAddons(String input) {
        loadContent();
        // is this an exact ID match
        if (input != null && !input.isEmpty()) {
            Proxy proxy = addons.get(input);
            if (proxy != null)
                return Collections.singletonList(proxy.getDescriptor());
        }

        Collection<Addon> result = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            for (Proxy proxy : addons.values())
                result.add(proxy.getDescriptor());
            return result;
        }
        String[] values = input.split(" ");
        Collection<String> terms = new ArrayList<>(values.length);
        for (int i = 0; i != values.length; i++) {
            if (!values[i].isEmpty())
                terms.add(values[i].toLowerCase());
        }
        for (Proxy proxy : addons.values()) {
            Addon addon = proxy.getDescriptor();
            if (stringMatches(addon.getIdentifier().toLowerCase(), terms)
                    || stringMatches(addon.getName().toLowerCase(), terms)
                    || tagsMatches(addon.getTags(), terms)) {
                result.add(addon);
            }
        }
        return result;
    }

    /**
     * Gets whether the specified content string contains the terms to look for
     *
     * @param content A string
     * @param terms   The list of terms to look for in the content
     * @return Whether the string contains all the terms
     */
    private boolean stringMatches(String content, Collection<String> terms) {
        for (String term : terms) {
            if (!content.contains(term))
                return false;
        }
        return true;
    }

    /**
     * Gets whether one of term matches a tag
     *
     * @param tags  The tags to match
     * @param terms The terms to match
     * @return Whether at least one term is a tag
     */
    private boolean tagsMatches(Collection<String> tags, Collection<String> terms) {
        for (String term : terms) {
            if (tags.contains(term))
                return true;
        }
        return false;
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
