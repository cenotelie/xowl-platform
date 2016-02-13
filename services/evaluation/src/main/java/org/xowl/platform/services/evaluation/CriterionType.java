/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.evaluation;

import org.xowl.infra.store.Serializable;
import org.xowl.platform.kernel.Identifiable;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a kind of criterion for the evaluation of an element
 *
 * @author Laurent Wouters
 */
public interface CriterionType extends Identifiable, Serializable {
    /**
     * Gets the parameters for this type of criterion
     *
     * @return The parameters
     */
    Collection<CriterionParam> getParameters();

    /**
     * Gets the criterion that corresponds to this type given the parameters
     *
     * @param parameters The parameters
     * @return The criterion
     */
    Criterion getCriterion(Map<CriterionParam, String> parameters);

    /**
     * Gets whether this criterion supports a type of evaluable element
     *
     * @param evaluableType The type of elements to evaluate
     * @return Whether the criterion is applicable
     */
    boolean supports(EvaluableType evaluableType);
}
