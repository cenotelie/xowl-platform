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

import fr.cenotelie.commons.utils.Identifiable;
import fr.cenotelie.commons.utils.Serializable;

import java.util.Collection;

/**
 * Represents the context of an evaluation
 *
 * @author Laurent Wouters
 */
public interface Evaluation extends Identifiable, Serializable {
    /**
     * Gets the evaluated elements
     *
     * @return The evaluated elements
     */
    Collection<Evaluable> getEvaluables();

    /**
     * Gets the criteria for this evaluation
     *
     * @return The criteria for this evaluation
     */
    Collection<Criterion> getCriteria();
}
