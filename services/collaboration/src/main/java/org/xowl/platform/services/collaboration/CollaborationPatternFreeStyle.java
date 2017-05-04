/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.services.collaboration;

import fr.cenotelie.hime.redist.ASTNode;

/**
 * Represents a free-style collaboration pattern
 *
 * @author Laurent Wouters
 */
public class CollaborationPatternFreeStyle extends CollaborationPatternBase {
    /**
     * The descriptor for this collaboration pattern
     */
    public final static CollaborationPatternDescriptor DESCRIPTOR = new CollaborationPatternDescriptor(CollaborationPatternFreeStyle.class.getCanonicalName(), "Free-Style Collaboration Pattern");

    /**
     * Initializes this pattern
     */
    public CollaborationPatternFreeStyle() {
        super(CollaborationPatternFreeStyle.class.getCanonicalName(), "Free-Style Collaboration Pattern");
    }

    /**
     * Initializes this pattern
     *
     * @param definition The AST node for the serialized definition
     */
    public CollaborationPatternFreeStyle(ASTNode definition) {
        super(definition);
    }
}
