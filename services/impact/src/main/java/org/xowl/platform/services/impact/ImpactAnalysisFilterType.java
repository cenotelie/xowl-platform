/*******************************************************************************
 * Copyright (c) 2016 Madeleine Wouters
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General
 * Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors:
 * Madeleine Wouters - woutersmadeleine@gmail.com
 ******************************************************************************/

package org.xowl.platform.services.impact;

import org.xowl.infra.store.Serializable;
import org.xowl.infra.store.rdf.IRINode;

/**
 * Represents a filter on types in the results of an impact analysis
 *
 * @author Madeleine Wouters
 */
public interface ImpactAnalysisFilterType extends Serializable {
    /**
     * Applies this filter
     *
     * @param subject The result's subject
     * @param type    The result's type
     * @return Whether to keep this result
     */
    boolean apply(IRINode subject, IRINode type);
}
