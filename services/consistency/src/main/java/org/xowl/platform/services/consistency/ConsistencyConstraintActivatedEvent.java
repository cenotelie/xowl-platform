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

package org.xowl.platform.services.consistency;

import fr.cenotelie.commons.utils.RichString;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a consistency constraint has been activated
 *
 * @author Laurent Wouters
 */
public class ConsistencyConstraintActivatedEvent extends EventBase {
    /**
     * The type for this event
     */
    public static final String TYPE = ConsistencyConstraintActivatedEvent.class.getCanonicalName();

    /**
     * The activated constraint
     */
    private final ConsistencyConstraint constraint;

    /**
     * Gets the activated constraint
     *
     * @return The activated constraint
     */
    public ConsistencyConstraint getActivatedConstraint() {
        return constraint;
    }

    /**
     * Initializes this event
     *
     * @param constraint The activated constraint
     * @param emitter    The service that emitted this event
     */
    public ConsistencyConstraintActivatedEvent(ConsistencyConstraint constraint, Service emitter) {
        super(TYPE, emitter, new RichString("Activated constraint ", constraint));
        this.constraint = constraint;
    }
}
