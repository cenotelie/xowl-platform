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

package org.xowl.platform.kernel.platform;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.HttpAPIService;

import java.util.Collection;

/**
 * Represents a service for the management of this platform
 *
 * @author Laurent Wouters
 */
public interface PlatformManagementService extends HttpAPIService {
    /**
     * Exit code when the platform is shutting down as requested
     */
    int PLATFORM_EXIT_NORMAL = 0;
    /**
     * Exit code when the platform is shutting down to be restarted
     */
    int PLATFORM_EXIT_RESTART = 5;

    /**
     * Gets the description of the bundles on this platform
     *
     * @return The bundle on this platform
     */
    Collection<OSGiBundle> getPlatformBundles();

    /**
     * Regenerates a self-signed TLS certificate and setup its use in the OSGi platform configuration
     *
     * @param alias The alias to use in the TLS certificate
     * @return Whether the operation was successful
     */
    XSPReply regenerateTLSConfig(String alias);

    /**
     * Shutdowns the platform
     */
    XSPReply shutdown();

    /**
     * Restarts the platform
     */
    XSPReply restart();
}
