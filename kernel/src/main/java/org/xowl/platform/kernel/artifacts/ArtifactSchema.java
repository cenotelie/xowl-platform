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

package org.xowl.platform.kernel.artifacts;

import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.utils.Serializable;
import org.xowl.platform.kernel.Registrable;

import java.util.Collection;

/**
 * Represents the schema of a domain or artifact
 *
 * @author Laurent Wouters
 */
public interface ArtifactSchema extends Registrable, Serializable {
    /**
     * Gets whether this schema can be deployed in a triple store
     *
     * @return Whether this schema can be deployed in a triple store
     */
    boolean isDeployable();

    /**
     * Gets the schema's definition
     *
     * @return The schema's definition
     */
    Collection<Quad> getDefinition();

    /**
     * Gets the schema's definition
     *
     * @param deployable Whether the definition shall be deployable
     * @return The schema's definition
     */
    Collection<Quad> getDefinition(boolean deployable);
}
