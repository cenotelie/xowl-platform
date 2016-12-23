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

import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.RichString;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a consistency rule has been activated
 *
 * @author Laurent Wouters
 */
public class ConsistencyRuleActivatedEvent extends EventBase {
    /**
     * The activated rule
     */
    private final ConsistencyRule rule;

    /**
     * Gets the activated rule
     *
     * @return The activated rule
     */
    public ConsistencyRule getActivatedRule() {
        return rule;
    }

    /**
     * Initializes this event
     *
     * @param rule       The activated rule
     * @param originator The originator for this event
     */
    public ConsistencyRuleActivatedEvent(ConsistencyRule rule, Identifiable originator) {
        super(new RichString("Activated rule ", rule), ConsistencyRuleActivatedEvent.class.getCanonicalName(), originator);
        this.rule = rule;
    }
}
