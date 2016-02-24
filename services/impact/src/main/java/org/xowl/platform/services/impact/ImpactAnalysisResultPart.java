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
import org.xowl.infra.utils.collections.Couple;

import java.util.Collection;

/**
 * An element of result for an impact analysis
 *
 * @author Madeleine Wouters
 */
public interface ImpactAnalysisResultPart extends Serializable {
    /**
     * Get the associated node
     *
     * @return The associated node
     */
    IRINode getNode();

    /**
     * Get the associated degree
     *
     * @return The associated degree
     */
    int getDegree();

    /**
     * Get the associated types
     *
     * @return The associated types
     */
    Collection<IRINode> getTypes();

    /**
     * Get the associated paths : all the paths to reach this node from the root
     *
     * @return The associated paths
     */
    Collection<Collection<Couple<String, IRINode>>> getPaths();
}
