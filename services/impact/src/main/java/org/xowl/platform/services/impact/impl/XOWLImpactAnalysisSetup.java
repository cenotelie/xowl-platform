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

package org.xowl.platform.services.impact.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.services.impact.ImpactAnalysisSetup;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Implements the setup parameters of an impact analysis
 *
 * @author Laurent Wouters
 */
class XOWLImpactAnalysisSetup implements ImpactAnalysisSetup {

    /**
     * Initializes this analysis setup
     *
     * @param definition The definition
     */
    public XOWLImpactAnalysisSetup(ASTNode definition) {
        // FIXME: implement this
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String serializedString() {
        // FIXME: implement this
        throw new NotImplementedException();
    }

    @Override
    public String serializedJSON() {
        // FIXME: implement this
        throw new NotImplementedException();
    }
}
