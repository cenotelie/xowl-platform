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

package org.xowl.platform.services.lts;

import org.xowl.platform.kernel.Service;

/**
 * Represents a triple-store service for the platform
 *
 * @author Laurent Wouters
 */
public interface TripleStoreService extends Service {
    /**
     * Gets the live store that contains the currently active artifacts
     * Reasoning is expected to be activated on this store.
     * This store cannot be expected to be persistent.
     *
     * @return The live store
     */
    TripleStore getLiveStore();

    /**
     * Gets the long-term store that contains a copy of all the artifacts managed by the platform
     * Reasoning is not expected to be activated on this store.
     * This store is expected to be persistent.
     *
     * @return The long-term store
     */
    TripleStore getLongTermStore();

    /**
     * Gets the store that can be used to persist data for the services on the platform
     *
     * @return The service store
     */
    TripleStore getServiceStore();
}
