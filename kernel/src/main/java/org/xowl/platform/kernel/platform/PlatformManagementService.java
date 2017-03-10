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

import org.osgi.framework.Bundle;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.utils.ApiError;
import org.xowl.infra.utils.collections.Couple;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricBase;
import org.xowl.infra.utils.product.Product;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.statistics.MeasurableService;

import java.io.InputStream;
import java.util.Collection;

/**
 * Represents a service for the management of this platform
 *
 * @author Laurent Wouters
 */
public interface PlatformManagementService extends SecuredService, MeasurableService {
    /**
     * Exit code when the platform is shutting down as requested
     */
    int PLATFORM_EXIT_NORMAL = 0;
    /**
     * Exit code when the platform is shutting down to be restarted
     */
    int PLATFORM_EXIT_RESTART = 5;

    /**
     * The metric for the used memory
     */
    Metric METRIC_USED_MEMORY = new MetricBase(PlatformManagementService.class.getCanonicalName() + ".UsedMemory",
            "Platform Management Service - Used Memory",
            "bytes",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));
    /**
     * The metric for the free memory
     */
    Metric METRIC_FREE_MEMORY = new MetricBase(PlatformManagementService.class.getCanonicalName() + ".FreeMemory",
            "Platform Management Service - Free Memory",
            "bytes",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));
    /**
     * The metric for the total reserved memory
     */
    Metric METRIC_TOTAL_MEMORY = new MetricBase(PlatformManagementService.class.getCanonicalName() + ".TotalMemory",
            "Platform Management Service - Total Memory",
            "bytes",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));
    /**
     * The metric for the max memory that can be reserved
     */
    Metric METRIC_MAX_MEMORY = new MetricBase(PlatformManagementService.class.getCanonicalName() + ".MaxMemory",
            "Platform Management Service - Max Memory",
            "bytes",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));

    /**
     * Service action to get the product descriptor for the platform
     */
    SecuredAction ACTION_GET_PRODUCT = new SecuredAction(
            PlatformManagementService.class.getCanonicalName() + ".GetProduct",
            "Platform Management Service - Get Product");
    /**
     * Service action to get the description of the bundles on this platform
     */
    SecuredAction ACTION_GET_BUNDLES = new SecuredAction(
            PlatformManagementService.class.getCanonicalName() + ".GetBundles",
            "Platform Management Service - Get Bundles");
    /**
     * Service action to get the addons installed on this platform
     */
    SecuredAction ACTION_GET_ADDONS = new SecuredAction(
            PlatformManagementService.class.getCanonicalName() + ".GetAddons",
            "Platform Management Service - Get Addons");
    /**
     * Service action to install an addon on the platform
     */
    SecuredAction ACTION_INSTALL_ADDON = new SecuredAction(
            PlatformManagementService.class.getCanonicalName() + ".InstallAddon",
            "Platform Management Service - InstallAddon");
    /**
     * Service action to uninstall an addon from the platform
     */
    SecuredAction ACTION_UNINSTALL_ADDON = new SecuredAction(
            PlatformManagementService.class.getCanonicalName() + ".UninstallAddon",
            "Platform Management Service - Uninstall Addon");
    /**
     * Service action to shutdown the platform
     */
    SecuredAction ACTION_SHUTDOWN = new SecuredAction(
            PlatformManagementService.class.getCanonicalName() + ".Shutdown",
            "Platform Management Service - Shutdown");
    /**
     * Service action to restart the platform
     */
    SecuredAction ACTION_RESTART = new SecuredAction(
            PlatformManagementService.class.getCanonicalName() + ".Restart",
            "Platform Management Service - Restart");


    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_GET_PRODUCT,
            ACTION_GET_BUNDLES,
            ACTION_GET_ADDONS,
            ACTION_INSTALL_ADDON,
            ACTION_UNINSTALL_ADDON,
            ACTION_SHUTDOWN,
            ACTION_RESTART
    };

    /**
     * API error - The addon is already installed
     */
    ApiError ERROR_ADDON_ALREADY_INSTALLED = new ApiError(0x00000021,
            "The addon is already installed.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000021.html");
    /**
     * API error - The addon is not installed
     */
    ApiError ERROR_ADDON_NOT_INSTALLED = new ApiError(0x00000022,
            "The addon is not installed.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000022.html");
    /**
     * API error - The provided addon package is invalid
     */
    ApiError ERROR_INVALID_ADDON_PACKAGE = new ApiError(0x00000023,
            "The provided addon package is invalid.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000023.html");

    /**
     * Gets the product descriptor for the platform
     *
     * @return The product descriptor
     */
    Product getPlatformProduct();

    /**
     * Gets the description of the bundles on this platform
     *
     * @return The bundle on this platform
     */
    Collection<Bundle> getPlatformBundles();

    /**
     * Gets the addons installed on this platform
     *
     * @return The installed addons
     */
    Collection<Addon> getAddons();

    /**
     * Installs an addon on the platform
     *
     * @param identifier    The identifier of the addon
     * @param packageStream A stream to the addon package
     * @return The protocol reply
     */
    XSPReply installAddon(String identifier, InputStream packageStream);

    /**
     * Un-installs an addon from the platform
     *
     * @param identifier The identifier of the addon
     * @return The protocol reply
     */
    XSPReply uninstallAddon(String identifier);

    /**
     * Shutdowns the platform
     *
     * @return The protocol reply
     */
    XSPReply shutdown();

    /**
     * Restarts the platform
     *
     * @return The protocol reply
     */
    XSPReply restart();
}
