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

package org.xowl.platform.kernel.stdimpl;

import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.commons.utils.metrics.Metric;
import fr.cenotelie.commons.utils.metrics.MetricSnapshot;
import fr.cenotelie.commons.utils.metrics.MetricSnapshotInt;
import org.xowl.platform.kernel.ManagedService;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.events.Event;
import org.xowl.platform.kernel.events.EventConsumer;
import org.xowl.platform.kernel.events.EventService;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The default implementation of the event service for the platform
 *
 * @author Laurent Wouters
 */
public class KernelEventService implements EventService, ManagedService {
    /**
     * The maximum number of queued events
     */
    private static final int QUEUE_LENGTH = 128;
    /**
     * The time to wait for events, in ms
     */
    private static final long WAIT_TIME = 500;

    /**
     * The thread dispatching events
     */
    private final Thread dispatchThread;
    /**
     * Flag whether the dispatcher thread must stop
     */
    private final AtomicBoolean mustStop;
    /**
     * The queue of events to be dispatched
     */
    private final BlockingQueue<Event> queue;
    /**
     * The routes for the events
     */
    private final Map<String, List<EventConsumer>> routes;
    /**
     * The total number of processed events
     */
    private int totalProcessed;

    /**
     * Initializes this service
     */
    public KernelEventService() {
        this.dispatchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                KernelEventService.this.dispatchRun();
            }
        }, KernelEventService.class.getCanonicalName() + ".EventDispatcher");
        this.mustStop = new AtomicBoolean(false);
        this.queue = new ArrayBlockingQueue<>(QUEUE_LENGTH);
        this.routes = new HashMap<>();
        this.totalProcessed = 0;
        this.dispatchThread.start();
    }

    @Override
    public String getIdentifier() {
        return KernelEventService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Event Dispatch Service";
    }

    @Override
    public int getLifecycleTier() {
        return TIER_INTERNAL;
    }

    @Override
    public void onLifecycleStart() {
        // do nothing
    }

    @Override
    public void onLifecycleStop() {
        mustStop.set(true);
        try {
            if (dispatchThread.isAlive()) {
                dispatchThread.interrupt();
                dispatchThread.join();
            }
        } catch (InterruptedException exception) {
            Logging.get().error(exception);
        }
    }

    @Override
    public Collection<Metric> getMetrics() {
        return Arrays.asList(METRIC_TOTAL_PROCESSED_EVENTS, METRIC_QUEUED_EVENTS);
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        if (metric == METRIC_TOTAL_PROCESSED_EVENTS)
            return new MetricSnapshotInt(totalProcessed);
        if (metric == METRIC_QUEUED_EVENTS)
            return new MetricSnapshotInt(queue.size());
        return null;
    }

    @Override
    public void onEvent(Event event) {
        Logging.get().info(event);
        while (true) {
            try {
                if (queue.add(event))
                    return;
            } catch (IllegalStateException exception) {
                // queue is full
            }
        }
    }

    @Override
    public void subscribe(EventConsumer consumer, String eventType) {
        synchronized (routes) {
            List<EventConsumer> consumers = routes.get(eventType);
            if (consumers == null) {
                consumers = new ArrayList<>();
                routes.put(eventType, consumers);
            }
            consumers.add(consumer);
        }
    }

    @Override
    public void unsubscribe(EventConsumer consumer) {
        synchronized (routes) {
            for (Map.Entry<String, List<EventConsumer>> sub : routes.entrySet()) {
                sub.getValue().remove(consumer);
            }
        }
    }

    /**
     * Main function for the dispatching thread
     */
    private void dispatchRun() {
        List<EventConsumer> dispatchTo = new ArrayList<>(16);
        while (!mustStop.get()) {
            // get an event
            Event event;
            try {
                event = queue.poll(WAIT_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                return;
            }
            if (event != null) {
                // build the list of consumers
                dispatchTo.clear();
                List<EventConsumer> consumers = routes.get(event.getType());
                if (consumers != null)
                    dispatchTo.addAll(consumers);
                consumers = routes.get(null);
                if (consumers != null)
                    dispatchTo.addAll(consumers);
                // dispatch
                for (EventConsumer consumer : dispatchTo) {
                    try {
                        consumer.onEvent(event);
                    } catch (Throwable throwable) {
                        Logging.get().error(throwable);
                    }
                }
                totalProcessed++;
            }
        }
    }
}
