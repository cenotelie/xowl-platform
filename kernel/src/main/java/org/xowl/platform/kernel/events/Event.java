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

import org.xowl.infra.store.Serializable;
import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.RichString;

import java.util.Date;

/**
 * Represents an event on the platform
 *
 * @author Laurent Wouters
 */
public interface Event extends Identifiable, Serializable {
    /**
     * Gets a description of this event
     *
     * @return The description of this event
     */
    RichString getDescription();

    /**
     * Gets the event type
     *
     * @return The event type
     */
    String getType();

    /**
     * Gets the timestamp when the event was created
     *
     * @return The timestamp when the event was created
     */
    Date getTimestamp();

    /**
     * Gets the entity that emitted this event
     *
     * @return The entity of origin
     */
    Identifiable getOrigin();
}
