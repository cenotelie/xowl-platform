/*******************************************************************************
 * Copyright (c) 2016 Madeleine Wouters
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
 *     Madeleine Wouters - woutersmadeleine@gmail.com
 ******************************************************************************/

package org.xowl.platform.services.impact;

import org.xowl.infra.store.Serializable;
import org.xowl.infra.store.rdf.IRINode;

/**
 * Represents a filter in the impact analysis on a link between nodes
 *
 * @author Madeleine Wouters
 */
public interface ImpactAnalysisFilterLink extends Serializable {
    /**
     * Apply this filter
     *
     * @param link    The link to the target
     * @return Whether to keep the neighbour (target)
     */
    boolean apply(IRINode link);
}
