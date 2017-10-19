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

import fr.cenotelie.commons.utils.RichString;
import fr.cenotelie.commons.utils.TextUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecurityService;

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
     * The event type
     */
    private final String type;
    /**
     * The service that emitted this event
     */
    private final Service emitter;
    /**
     * The platform user that triggered this event
     */
    private final PlatformUser creator;
    /**
     * The creation timestamp
     */
    private final Date timestamp;
    /**
     * The human-readable description of this event
     */
    private final RichString description;

    /**
     * Initializes this event
     *
     * @param type        The event type
     * @param emitter     The service that emitted this event
     * @param description The human-readable description of this event
     */
    public EventBase(String type, Service emitter, RichString description) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        this.identifier = Event.class.getCanonicalName() + "." + UUID.randomUUID().toString();
        this.type = type;
        this.emitter = emitter;
        this.creator = securityService != null ? securityService.getCurrentUser() : null;
        this.timestamp = new Date();
        this.description = description;
    }

    /**
     * Initializes this event
     *
     * @param type        The event type
     * @param emitter     The service that emitted this event
     * @param description The human-readable description of this event
     */
    public EventBase(String type, Service emitter, String description) {
        this(type, emitter, new RichString(description));
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
    public String getType() {
        return type;
    }

    @Override
    public Service getEmitter() {
        return emitter;
    }

    @Override
    public PlatformUser getCreator() {
        return creator;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public RichString getDescription() {
        return description;
    }

    @Override
    public String serializedString() {
        return identifier + ": " + description;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \""
                + TextUtils.escapeStringJSON(Event.class.getCanonicalName())
                + "\", \"identifier\": \""
                + TextUtils.escapeStringJSON(identifier)
                + "\", \"name\": \""
                + TextUtils.escapeStringJSON(description.serializedString())
                + "\", \"eventType\": \""
                + TextUtils.escapeStringJSON(type)
                + "\", \"emitter\": \""
                + (emitter != null ? TextUtils.escapeStringJSON(emitter.getIdentifier()) : "")
                + "\", \"creator\": \""
                + (creator != null ? TextUtils.escapeStringJSON(creator.getIdentifier()) : "")
                + "\", \"timestamp\": \""
                + TextUtils.escapeStringJSON(DateFormat.getDateTimeInstance().format(timestamp))
                + "\", \"description\": "
                + description.serializedJSON()
                + "}";
    }

    @Override
    public String toString() {
        return serializedString();
    }
}
