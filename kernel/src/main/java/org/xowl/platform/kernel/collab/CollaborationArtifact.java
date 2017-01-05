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

import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;

/**
 * Represents an input/output artifact for a collaboration
 *
 * @author Laurent Wouters
 */
public interface CollaborationArtifact extends Identifiable, Serializable {
    /**
     * Gets the specification for this artifact
     *
     * @return The specification for this artifact
     */
    CollaborationSpecification getSpecification();
}
