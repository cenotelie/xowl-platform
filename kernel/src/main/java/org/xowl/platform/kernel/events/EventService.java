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

package org.xowl.platform.kernel.events;

import org.xowl.infra.utils.collections.Couple;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricBase;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.statistics.MeasurableService;

/**
 * Represents a service on the platform for managing events
 *
 * @author Laurent Wouters
 */
public interface EventService extends Service, MeasurableService {
    /**
     * The metric for the total number of processed events
     */
    Metric METRIC_TOTAL_PROCESSED_EVENTS = new MetricBase(EventService.class.getCanonicalName() + ".TotalProcessedEvents",
            "Event Service - Total processed events",
            "events",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));
    /**
     * The metric for the number of queued events
     */
    Metric METRIC_QUEUED_EVENTS = new MetricBase(EventService.class.getCanonicalName() + ".QueuedEvents",
            "Event Service - Queued events",
            "events",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));

    /**
     * When an event happened
     * This will propagate the event to the appropriate consumers, if any
     *
     * @param event The event
     */
    void onEvent(Event event);

    /**
     * Subscribes to a flow of event
     *
     * @param consumer  The subscribing consumer
     * @param eventType The specific event type to wait for, or null if any event type is acceptable
     */
    void subscribe(EventConsumer consumer, String eventType);

    /**
     * Un-subscribes to all events
     *
     * @param consumer The un-subscribing consumer
     */
    void unsubscribe(EventConsumer consumer);
}
