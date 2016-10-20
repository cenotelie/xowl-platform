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

import org.xowl.infra.store.IOUtils;
import org.xowl.platform.kernel.Identifiable;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Base implementation of an event
 *
 * @author Laurent Wouters
 */
public class EventBase implements Event {
    /**
     * The unique identifier of this event
     */
    private final String identifier;
    /**
     * The human-readable name of this event
     */
    private final String name;
    /**
     * The event type
     */
    private final String type;
    /**
     * The creation timestamp
     */
    private final Date timestamp;
    /**
     * The originator of this event
     */
    private final Identifiable originator;

    /**
     * Initializes this event
     *
     * @param name       The human-readable name of this event
     * @param type       The event type
     * @param originator The originator of this event
     */
    public EventBase(String name, String type, Identifiable originator) {
        this.identifier = Event.class.getCanonicalName() + "." + UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.timestamp = new Date();
        this.originator = originator;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public String getType() {
        return type;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public Identifiable getOrigin() {
        return originator;
    }

    @Override
    public String serializedString() {
        return identifier + ": " + name;
    }

    @Override
    public String serializedJSON() {
        return "{\"identifier\": \""
                + IOUtils.escapeStringJSON(identifier)
                + "\", \"name\":\""
                + IOUtils.escapeStringJSON(name)
                + "\", \"type\": \""
                + IOUtils.escapeStringJSON(type)
                + "\", \"timestamp\": \""
                + IOUtils.escapeStringJSON(DateFormat.getDateTimeInstance().format(timestamp))
                + "\", \"originator\": \""
                + IOUtils.escapeStringJSON(originator.getIdentifier())
                + "\"}";
    }

    @Override
    public String toString() {
        return serializedString();
    }
}
