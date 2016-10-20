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
import org.xowl.platform.kernel.RichString;

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
     * The human-readable description of this event
     */
    private final RichString description;
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
     * @param description The human-readable description of this event
     * @param type        The event type
     * @param originator  The originator of this event
     */
    public EventBase(RichString description, String type, Identifiable originator) {
        this.identifier = Event.class.getCanonicalName() + "." + UUID.randomUUID().toString();
        this.description = description;
        this.type = type;
        this.timestamp = new Date();
        this.originator = originator;
    }

    /**
     * Initializes this event
     *
     * @param description The human-readable description of this event
     * @param type        The event type
     * @param originator  The originator of this event
     */
    public EventBase(String description, String type, Identifiable originator) {
        this.identifier = Event.class.getCanonicalName() + "." + UUID.randomUUID().toString();
        this.description = new RichString(description);
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
        return description.serializedString();
    }

    @Override
    public RichString getDescription() {
        return description;
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
        return identifier + ": " + description;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \""
                + IOUtils.escapeStringJSON(Event.class.getCanonicalName())
                + "\", \"identifier\": \""
                + IOUtils.escapeStringJSON(identifier)
                + "\", \"name\": \""
                + IOUtils.escapeStringJSON(description.serializedString())
                + "\", \"description\": "
                + description.serializedJSON()
                + ", \"eventType\": \""
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
