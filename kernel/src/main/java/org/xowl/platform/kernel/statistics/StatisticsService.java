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

package org.xowl.platform.kernel.statistics;

import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.ServiceAction;

import java.util.Collection;

/**
 * Represents the statistics aggregation service for the platform
 *
 * @author Laurent Wouters
 */
public interface StatisticsService extends Service {
    /**
     * Service action to get the available metrics
     */
    ServiceAction ACTION_GET_METRICS = new ServiceAction(StatisticsService.class.getCanonicalName() + ".GetMetrics", "Statistics Service - Get Metrics");
    /**
     * Service action to get the current log events
     */
    ServiceAction ACTION_POLL = new ServiceAction(StatisticsService.class.getCanonicalName() + ".PollMetric", "Statistics Service - Poll Metric");

    /**
     * The actions for this service
     */
    ServiceAction[] ACTIONS = new ServiceAction[]{
            ACTION_GET_METRICS,
            ACTION_POLL
    };

    /**
     * Gets the known metrics
     *
     * @return The known metrics
     */
    Collection<Metric> getMetrics();

    /**
     * Gets the last value for the specified metric
     *
     * @param metric The requested metric
     * @return The last value (or null if the metric is not provided)
     */
    MetricSnapshot pollMetric(Metric metric);

    /**
     * Gets the last value for the specified metric
     *
     * @param metric The requested metric
     * @return The last value (or null if the metric is not provided)
     */
    MetricSnapshot pollMetric(String metric);
}
