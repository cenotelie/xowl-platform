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

package org.xowl.platform.kernel.impl;

import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.concurrent.SafeRunnable;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.infra.utils.metrics.MetricSnapshotInt;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.events.Event;
import org.xowl.platform.kernel.events.EventConsumer;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.platform.PlatformShutdownEvent;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The default implementation of the event service for the platform
 *
 * @author Laurent Wouters
 */
public class KernelEventService implements EventService {
    /**
     * The maximum number of queued events
     */
    private static final int QUEUE_LENGTH = 128;

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
    private final Map<Identifiable, Map<String, List<EventConsumer>>> routes;
    /**
     * The total number of processed events
     */
    private int totalProcessed;

    /**
     * Initializes this service
     */
    public KernelEventService() {
        this.dispatchThread = new Thread(new SafeRunnable(Logging.getDefault()) {
            @Override
            public void doRun() {
                KernelEventService.this.dispatchRun();
            }
        }, KernelEventService.class.getCanonicalName() + ".EventDispatcher");
        this.mustStop = new AtomicBoolean(false);
        this.queue = new ArrayBlockingQueue<>(QUEUE_LENGTH);
        this.routes = new HashMap<>();
        this.totalProcessed = 0;
        this.dispatchThread.start();
    }

    /**
     * When the platform is closing
     */
    public void close() {
        mustStop.set(true);
        try {
            dispatchThread.join();
        } catch (InterruptedException exception) {
            Logging.getDefault().error(exception);
        }
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
        Logging.getDefault().info(event);
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
    public void subscribe(EventConsumer consumer, Identifiable originator, String eventType) {
        synchronized (routes) {
            Map<String, List<EventConsumer>> sub = routes.get(originator);
            if (sub == null) {
                sub = new HashMap<>();
                routes.put(originator, sub);
            }
            List<EventConsumer> consumers = sub.get(eventType);
            if (consumers == null) {
                consumers = new ArrayList<>();
                sub.put(eventType, consumers);
            }
            consumers.add(consumer);
        }
    }

    /**
     * Main function for the dispatching thread
     */
    private void dispatchRun() {
        List<EventConsumer> consumers = new ArrayList<>(16);
        while (!mustStop.get()) {
            try {
                consumers.clear();
                // get an event
                Event event = queue.take();

                // build the list of consumers
                Map<String, List<EventConsumer>> sub = routes.get(event.getOrigin());
                if (sub != null) {
                    List<EventConsumer> sub2 = sub.get(event.getType());
                    if (sub2 != null)
                        consumers.addAll(sub2);
                    sub2 = sub.get(null);
                    if (sub2 != null)
                        consumers.addAll(sub2);
                }
                sub = routes.get(null);
                if (sub != null) {
                    List<EventConsumer> sub2 = sub.get(event.getType());
                    if (sub2 != null)
                        consumers.addAll(sub2);
                    sub2 = sub.get(null);
                    if (sub2 != null)
                        consumers.addAll(sub2);
                }

                // dispatch
                for (EventConsumer consumer : consumers) {
                    try {
                        consumer.onEvent(event);
                    } catch (Throwable throwable) {
                        Logging.getDefault().error(throwable);
                    }
                }
                totalProcessed++;

                // stop on platform shutdown
                if (PlatformShutdownEvent.TYPE.equals(event.getType()))
                    return;
            } catch (InterruptedException exception) {
                Logging.getDefault().error(exception);
            }
        }
    }
}