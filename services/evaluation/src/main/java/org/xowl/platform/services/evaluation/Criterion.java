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

import org.xowl.infra.store.Serializable;
import org.xowl.platform.kernel.Identifiable;

import java.util.Map;

/**
 * Represents a criterion for the evaluation of elements in the context of an evaluation
 *
 * @author Laurent Wouters
 */
public interface Criterion extends Identifiable, Serializable {
    /**
     * Gets the parent type of this criterion
     *
     * @return The parent type of this criterion
     */
    CriterionType getType();

    /**
     * Gets the parameters for this criterion
     *
     * @return The parameters for this criterion
     */
    Map<String, String> getParameters();

    /**
     * Gets the result according to this criterion for an evaluable element
     *
     * @param element The evaluable element
     * @return The result, or null if there is none
     */
    CriterionResult getResultFor(Evaluable element);
}
