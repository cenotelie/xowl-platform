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

package org.xowl.platform.services.evaluation;

import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.RichString;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a new evaluation has been created
 *
 * @author Laurent Wouters
 */
public class EvaluationCreatedEvent extends EventBase {
    /**
     * The created evaluation
     */
    private final Evaluation evaluation;

    /**
     * Gets the created evaluation
     *
     * @return The created evaluation
     */
    public Evaluation getCreatedEvaluation() {
        return evaluation;
    }

    /**
     * Initializes this event
     *
     * @param evaluation The created evaluation
     * @param originator The originator for this event
     */
    public EvaluationCreatedEvent(Evaluation evaluation, Identifiable originator) {
        super(new RichString("Created evaluation ", evaluation), EvaluationCreatedEvent.class.getCanonicalName(), originator);
        this.evaluation = evaluation;
    }
}
