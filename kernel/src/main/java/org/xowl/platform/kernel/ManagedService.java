/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.kernel;

import java.util.Comparator;

/**
 * Represents a service for which the lifecycle is managed by the platform.
 * The lifecycle of these services will be tied to the platform so that they are explicitly started and stopped in an orderly fashion.
 * Managed services are categorized in tiers that represent the relative priority of the service.
 *
 * @author Laurent Wouters
 */
public interface ManagedService extends Service {
    /**
     * Tier for managed services that oversee asynchronous operations.
     * Services in this tier must be started last and stopped first.
     */
    int TIER_ASYNC = 0;

    /**
     * Tier for services that oversee interactions of the platform with the external world.
     * Services in this tier have intermediate priority, they are started before the asynchronous operations but after the internal services.
     * They are stopped after asynchronous operations have been stopped, but before other internal services.
     */
    int TIER_IO = 50;

    /**
     * Tier for other internal services.
     * Service in this tier are the first started and the last to be stopped.
     */
    int TIER_INTERNAL = 100;

    /**
     * Gets the lifecycle tier of this service
     *
     * @return The lifecycle tied of this service
     */
    int getLifecycleTier();

    /**
     * Reacts to the start lifecycle event
     */
    void onLifecycleStart();

    /**
     * Reacts to the stop lifecycle event
     */
    void onLifecycleStop();

    /**
     * The comparator to use for sorting managed services when starting up
     */
    Comparator<ManagedService> COMPARATOR_STARTUP = new Comparator<ManagedService>() {
        @Override
        public int compare(ManagedService s1, ManagedService s2) {
            return Integer.compare(s2.getLifecycleTier(), s1.getLifecycleTier());
        }
    };

    /**
     * The comparator to use for sorting managed services when shutting down
     */
    Comparator<ManagedService> COMPARATOR_SHUTDOWN = new Comparator<ManagedService>() {
        @Override
        public int compare(ManagedService s1, ManagedService s2) {
            return Integer.compare(s1.getLifecycleTier(), s2.getLifecycleTier());
        }
    };
}
