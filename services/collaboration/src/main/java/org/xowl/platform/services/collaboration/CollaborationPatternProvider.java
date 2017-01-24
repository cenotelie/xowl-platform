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

import org.xowl.platform.kernel.Registrable;

import java.util.Collection;

/**
 * A provider of collaboration pattern
 *
 * @author Laurent Wouters
 */
public interface CollaborationPatternProvider extends Registrable {
    /**
     * Gets the descriptors for the supported patterns
     *
     * @return The descriptors for the supported patterns
     */
    Collection<CollaborationPatternDescriptor> getPatterns();

    /**
     * Instantiates a collaboration pattern
     *
     * @param identifier The identifier of a collaboration pattern
     * @return The collaboration pattern, or null it cannot be instantiated
     */
    CollaborationPattern instantiate(String identifier);

    /**
     * Instantiates a collaboration pattern
     *
     * @param descriptor The descriptor of the pattern
     * @return The collaboration pattern, or null it cannot be instantiated
     */
    CollaborationPattern instantiate(CollaborationPatternDescriptor descriptor);
}
