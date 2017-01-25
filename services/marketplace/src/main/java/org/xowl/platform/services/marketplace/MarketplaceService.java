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

package org.xowl.platform.services.marketplace;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.platform.Addon;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

import java.io.InputStream;
import java.util.Collection;

/**
 * Represents a marketplace service that provides addons which can be installed in the platform
 *
 * @author Laurent Wouters
 */
public interface MarketplaceService extends SecuredService {
    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            PlatformManagementService.ACTION_INSTALL_ADDON
    };

    /**
     * Lookups available addons on the marketplace
     *
     * @param input The input to look for
     * @return The collection of matching addons
     */
    Collection<Addon> lookupAddons(String input);

    /**
     * Gets the addon descriptor for the specified identifier
     *
     * @param identifier The identifier of an addon
     * @return The associated descriptor (or null if it cannot be found)
     */
    Addon getAddon(String identifier);

    /**
     * Gets a stream for the specified addon
     *
     * @param identifier The identifier of an addon
     * @return A stream on the package for the addon
     */
    InputStream getAddonPackage(String identifier);

    /**
     * Launches a job for the installation of an addon
     *
     * @param identifier The identifier of the addon to install
     * @return The protocol reply
     */
    XSPReply beginInstallOf(String identifier);
}
