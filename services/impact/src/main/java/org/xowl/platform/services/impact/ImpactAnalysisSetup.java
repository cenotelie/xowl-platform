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

import java.util.Collection;

/**
 * The setup of an impact analysis
 *
 * @author Laurent Wouters
 */
public interface ImpactAnalysisSetup extends Serializable {
    /**
     * Get the root of the analysis
     *
     * @return The root
     */
    IRINode getRoot();

    /**
     * Get the degree of the analysis
     * Maximum number of hop
     *
     * @return The degree
     */
    int getDegree();

    /**
     * Get the filters for walking the graph
     *
     * @return The filters
     */
    Collection<ImpactAnalysisFilterLink> getWalkerFilters();

    /**
     * Gets whether the walker filters define the included links, or the excluded ones
     *
     * @return Whether the walker filters define the included links, or the excluded ones
     */
    boolean isWalkerFilterInclusive();

    /**
     * Get the filters on the result
     *
     * @return The filters
     */
    Collection<ImpactAnalysisFilterType> getResultFilter();

    /**
     * Gets whether the result filters define the included types, or the excluded ones
     *
     * @return Whether the result filters define the included types, or the excluded ones
     */
    boolean isResultFilterInclusive();
}
