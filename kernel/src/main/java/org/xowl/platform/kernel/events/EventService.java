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

import org.xowl.platform.kernel.Service;

/**
 * Represents a service on the platform for managing events
 *
 * @author Laurent Wouters
 */
public interface EventService extends Service {
    /**
     * When an event happened
     * This will propagate the event to the appropriate consumers, if any
     *
     * @param event The event
     */
    void onEvent(Event event);

    /**
     * Subscribe to a flow of event
     *
     * @param consumer   The subscribing consumer
     * @param originator The specific event originator to observe, or null if any origin is acceptable
     * @param eventType  The specific event type to wait for, or null if any event type is acceptable
     */
    void subscribe(EventConsumer consumer, EventOriginator originator, String eventType);
}
