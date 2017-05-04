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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.platform.Addon;
import org.xowl.platform.services.marketplace.MarketplaceDescriptor;

import java.io.*;

/**
 * Implements a static marketplace that is on the local file system
 *
 * @author Laurent Wouters
 */
class FSMarketplace extends StaticMarketplace {
    /**
     * The location of the marketplace
     */
    private final File location;

    /**
     * Initializes this marketplace
     *
     * @param configuration The configuration for this marketplace
     */
    public FSMarketplace(Section configuration) {
        File target = new File(configuration.get("location"));
        if (!target.isAbsolute())
            // path is relative, make it relative to the distribution's root
            target = new File(new File(System.getProperty(Env.ROOT)), configuration.get("location"));
        this.location = target;
    }

    @Override
    protected MarketplaceDescriptor loadMarketplaceDescriptor() {
        File fileDescriptor = new File(location, MARKETPLACE_DESCRIPTOR);
        if (!fileDescriptor.exists()) {
            Logging.get().error("Cannot find marketplace descriptor " + fileDescriptor.getAbsolutePath());
            return null;
        }
        try (Reader reader = IOUtils.getReader(fileDescriptor)) {
            ASTNode definition = JsonLoader.parseJson(Logging.get(), reader);
            if (definition == null) {
                Logging.get().error("Failed to parse marketplace descriptor " + fileDescriptor.getAbsolutePath());
                return null;
            }
            return new MarketplaceDescriptor(definition);
        } catch (IOException exception) {
            Logging.get().error(exception);
            return null;
        }
    }

    @Override
    protected Addon loadAddonDescriptor(String identifier) {
        File fileDescriptor = new File(location, identifier + ".descriptor");
        if (!fileDescriptor.exists()) {
            Logging.get().error("Cannot find addon descriptor " + fileDescriptor.getAbsolutePath());
            return null;
        }
        try (Reader reader = IOUtils.getReader(fileDescriptor)) {
            ASTNode definition = JsonLoader.parseJson(Logging.get(), reader);
            if (definition == null) {
                Logging.get().error("Failed to parse addon descriptor " + fileDescriptor.getAbsolutePath());
                return null;
            }
            return new Addon(definition);
        } catch (IOException exception) {
            Logging.get().error(exception);
            return null;
        }
    }

    @Override
    public InputStream getAddonPackage(String identifier) {
        loadContent();
        Proxy proxy = addons.get(identifier);
        if (proxy == null)
            return null;
        File filePackage = new File(location, identifier + ".zip");
        if (!filePackage.exists()) {
            Logging.get().error("Cannot find addon package " + filePackage.getAbsolutePath());
            return null;
        }
        try {
            return new FileInputStream(filePackage);
        } catch (IOException exception) {
            Logging.get().error("Cannot open addon package " + filePackage.getAbsolutePath());
            return null;
        }
    }
}
