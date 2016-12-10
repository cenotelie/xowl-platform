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

package org.xowl.platform.services.storage;

import org.xowl.infra.utils.collections.Couple;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricBase;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.storage.impl.XOWLStorageService;

/**
 * Represents a triple-store service for the platform
 *
 * @author Laurent Wouters
 */
public interface StorageService extends ArtifactStorageService, HttpApiService, MeasurableService {
    /**
     * The total artifacts count metric
     */
    Metric METRIC_TOTAL_ARTIFACTS_COUNT = new MetricBase(XOWLStorageService.class.getCanonicalName() + ".TotalArtifactsCount",
            "Storage Service - Total artifacts count",
            "artifacts",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));
    /**
     * The total artifacts count metric
     */
    Metric METRIC_LIVE_ARTIFACTS_COUNT = new MetricBase(XOWLStorageService.class.getCanonicalName() + ".LiveArtifactsCount",
            "Storage Service - Live artifacts count",
            "artifacts",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));

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
