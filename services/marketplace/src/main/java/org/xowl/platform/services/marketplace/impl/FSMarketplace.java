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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     * The descriptor for this marketplace
     */
    private final MarketplaceDescriptor descriptor;
    /**
     * All the addons in this marketplace
     */
    private final Map<String, Addon> addons;

    /**
     * Initializes this marketplace
     *
     * @param configuration The configuration for this marketplace
     */
    public FSMarketplace(Section configuration) {
        this.location = new File(new File(System.getProperty(Env.ROOT)), configuration.get("location"));
        this.addons = new HashMap<>();
        File fileDescriptor = new File(location, MARKETPLACE_DESCRIPTOR);
        if (!fileDescriptor.exists()) {
            this.descriptor = new MarketplaceDescriptor();
            Logging.getDefault().error("Cannot find marketplace descriptor " + fileDescriptor.getAbsolutePath());
        } else {
            String content = null;
            try (InputStream stream = new FileInputStream(fileDescriptor)) {
                content = Files.read(stream, Files.CHARSET);
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
            }
            if (content == null) {
                this.descriptor = new MarketplaceDescriptor();
            } else {
                ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
                if (definition == null) {
                    Logging.getDefault().error("Failed to parse marketplace descriptor " + fileDescriptor.getAbsolutePath());
                    this.descriptor = new MarketplaceDescriptor();
                } else {
                    this.descriptor = new MarketplaceDescriptor(definition);
                }
            }
        }
    }

    @Override
    public Collection<Category> getCategories() {
        return descriptor.getCategories();
    }

    @Override
    public Collection<Addon> lookupAddons(String identifier, String name, String categoryId) {
        return null;
    }
}
