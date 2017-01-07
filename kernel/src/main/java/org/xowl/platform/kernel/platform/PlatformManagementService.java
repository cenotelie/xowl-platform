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
import org.xowl.infra.utils.collections.Couple;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricBase;
import org.xowl.infra.utils.product.Product;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.statistics.MeasurableService;

import java.io.InputStream;
import java.util.Collection;

/**
 * Represents a service for the management of this platform
 *
 * @author Laurent Wouters
 */
public interface PlatformManagementService extends Service, MeasurableService {
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
