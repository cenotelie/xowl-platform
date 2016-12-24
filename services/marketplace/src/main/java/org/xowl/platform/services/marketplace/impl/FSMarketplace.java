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
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.platform.Addon;
import org.xowl.platform.services.marketplace.Category;
import org.xowl.platform.services.marketplace.Marketplace;
import org.xowl.platform.services.marketplace.MarketplaceDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Implements a marketplace that is on the local file system
 *
 * @author Laurent Wouters
 */
class FSMarketplace implements Marketplace {
    /**
     * The descriptor file for a file-system marketplace
     */
    public static final String MARKETPLACE_DESCRIPTOR = "marketplace.json";

    /**
     * The location of the marketplace
     */
    private final File location;
    /**
     * The categories in this marketplace
     */
    private final Map<String, Category> categories;
    /**
     * All the addons in this marketplace
     */
    private final Map<String, Addon> addons;
    /**
     * The addons, by category
     */
    private final Map<String, Collection<Addon>> addonsByCategory;
    /**
     * Whether this marketplace must be reloaded
     */
    private boolean mustReload;

    /**
     * Initializes this marketplace
     *
     * @param configuration The configuration for this marketplace
     */
    public FSMarketplace(Section configuration) {
        this.location = new File(new File(System.getProperty(Env.ROOT)), configuration.get("location"));
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
            Addon addon = loadAddonDescriptor(addonDescriptor.identifier);
            if (addon != null) {
                addons.put(addon.getIdentifier(), addon);
                for (int i = 0; i != addonDescriptor.categories.length; i++) {
                    Collection<Addon> forCategory = addonsByCategory.get(addonDescriptor.categories[i]);
                    if (forCategory == null) {
                        forCategory = new ArrayList<>();
                        addonsByCategory.put(addonDescriptor.categories[i], forCategory);
                    }
                    forCategory.add(addon);
                }
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
        File fileDescriptor = new File(location, MARKETPLACE_DESCRIPTOR);
        if (!fileDescriptor.exists()) {
            Logging.getDefault().error("Cannot find marketplace descriptor " + fileDescriptor.getAbsolutePath());
            return null;
        }
        String content = null;
        try (InputStream stream = new FileInputStream(fileDescriptor)) {
            content = Files.read(stream, Files.CHARSET);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
        }
        if (content == null) {
            Logging.getDefault().error("Failed to parse marketplace descriptor " + fileDescriptor.getAbsolutePath());
            return null;
        }
        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
        if (definition == null) {
            Logging.getDefault().error("Failed to parse marketplace descriptor " + fileDescriptor.getAbsolutePath());
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
        File fileDescriptor = new File(location, identifier + ".descriptor");
        if (!fileDescriptor.exists()) {
            Logging.getDefault().error("Cannot find addon descriptor " + fileDescriptor.getAbsolutePath());
            return null;
        }
        String content = null;
        try (InputStream stream = new FileInputStream(fileDescriptor)) {
            content = Files.read(stream, Files.CHARSET);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
        }
        if (content == null) {
            Logging.getDefault().error("Failed to parse addon descriptor " + fileDescriptor.getAbsolutePath());
            return null;
        }
        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
        if (definition == null) {
            Logging.getDefault().error("Failed to parse addon descriptor " + fileDescriptor.getAbsolutePath());
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
    public Collection<Addon> lookupAddons(String identifier, String name, String categoryId) {
        loadContent();
        // get the collection for the category
        Collection<Addon> collection;
        if (categoryId != null)
            collection = addonsByCategory.get(categoryId);
        else
            collection = addons.values();
        if (collection == null)
            return Collections.emptyList();
        // search by id?
        if (identifier != null) {
            Addon addon = addons.get(identifier);
            if (addon != null)
                return Collections.singletonList(addon);
        }
        Collection<Addon> result = new ArrayList<>();
        for (Addon addon : collection) {
            if (identifier != null && addon.getIdentifier().matches(identifier))
                result.add(addon);
            else if (name != null && addon.getIdentifier().matches(name))
                result.add(addon);
            else if (identifier == null && name == null)
                result.add(addon);
        }
        return result;
    }

    @Override
    public Addon getAddon(String identifier) {
        loadContent();
        return addons.get(identifier);
    }

    @Override
    public InputStream getAddonPackage(String identifier) {
        loadContent();
        Addon addon = addons.get(identifier);
        if (addon == null)
            return null;
        File filePackage = new File(location, identifier + ".zip");
        if (!filePackage.exists()) {
            Logging.getDefault().error("Cannot find addon package " + filePackage.getAbsolutePath());
            return null;
        }
        try {
            return new FileInputStream(filePackage);
        } catch (IOException exception) {
            Logging.getDefault().error("Cannot open addon package " + filePackage.getAbsolutePath());
            return null;
        }
    }
}
