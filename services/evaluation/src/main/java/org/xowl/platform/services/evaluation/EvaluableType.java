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

import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.Serializable;
import org.xowl.platform.kernel.Registrable;

import java.util.Map;

/**
 * Represents a kind of element that can be evaluated.
 * API users must register
 *
 * @author Laurent Wouters
 */
public interface EvaluableType extends Registrable, Serializable {
    /**
     * Gets the evaluable elements of this type in the stored artifacts
     *
     * @return The evaluable elements
     */
    Reply getElements();

    /**
     * Gets the evaluable elements given the specified parameters
     *
     * @param parameters The parameters
     * @return The evaluable element
     */
    Evaluable getElement(Map<String, String> parameters);
}
