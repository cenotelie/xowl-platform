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

import org.xowl.infra.server.api.XOWLFactory;
import org.xowl.infra.utils.Serializable;
import org.xowl.platform.kernel.Registrable;

import java.util.Collection;

/**
 * Represents a business domain, for example, the medical domain, the civil engineering domain, etc.
 *
 * @author Laurent Wouters
 */
public interface BusinessDomain extends Registrable, Serializable {
    /**
     * Gets the human-readable description of this domain
     *
     * @return The human-readable description
     */
    String getDescription();

    /**
     * Gets the factory that can be used to deserialize objects related to this domain
     *
     * @return The factory, if any
     */
    XOWLFactory getFactory();

    /**
     * Gets the schema associated to this domain
     *
     * @return The associated schema
     */
    BusinessSchema getSchema();

    /**
     * Gets the artifact archetypes related to this domain
     *
     * @return The related artifact archetypes
     */
    Collection<ArtifactArchetype> getArchetypes();
}
