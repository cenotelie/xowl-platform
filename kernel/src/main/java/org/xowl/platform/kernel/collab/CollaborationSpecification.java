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

package org.xowl.platform.kernel.collab;

import org.xowl.platform.kernel.platform.PlatformRole;

import java.util.Collection;

/**
 * Specifies a collaboration so that it can be spawned
 *
 * @author Laurent Wouters
 */
public interface CollaborationSpecification {
    /**
     * Gets the specifications of the inputs for this collaboration
     *
     * @return The specifications of the inputs for this collaboration
     */
    Collection<CollaborationArtifactSpecification> getInputs();

    /**
     * Gets the specifications of the outputs for this collaboration
     *
     * @return The specification of the outputs for this collaboration
     */
    Collection<CollaborationArtifactSpecification> getOutputs();

    /**
     * Gets the roles for the collaboration
     *
     * @return The roles for the collaboration
     */
    Collection<PlatformRole> getRoles();
}
