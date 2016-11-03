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

import org.xowl.infra.utils.Serializable;
import org.xowl.platform.kernel.Identifiable;

import java.util.Collection;

/**
 * Represents an archetype of artifacts.
 * An archetype is a kind of recurrent kind of data stored within an artifact. This is usually related to a business domain.
 * Examples of archetypes: A functional architecture in system engineering, etc.
 *
 * @author Laurent Wouters
 */
public interface ArtifactArchetype extends Identifiable, Serializable {
    /**
     * Gets the human-readable description of this archetype
     *
     * @return The description of this archetype
     */
    String getDescription();

    /**
     * Gets the schema associated to this archetype
     *
     * @return The associated schema
     */
    BusinessSchema getSchema();

    /**
     * Gets the business domains related to this archetype
     *
     * @return The related business domains
     */
    Collection<BusinessDomain> getDomains();
}
