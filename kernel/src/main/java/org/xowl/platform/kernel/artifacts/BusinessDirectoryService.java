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

import org.xowl.platform.kernel.Service;

import java.util.Collection;

/**
 * Represents a service that exposes a directory of business domains, schemas and archetypes
 *
 * @author Laurent Wouters
 */
public interface BusinessDirectoryService extends Service {
    /**
     * Gets the registered domains
     *
     * @return The registered domains
     */
    Collection<BusinessDomain> getDomains();

    /**
     * Gets the registered schemas
     *
     * @return The registered schemas
     */
    Collection<BusinessSchema> getSchemas();

    /**
     * Gets the registered artifact archetypes
     *
     * @return The registered artifact archetypes
     */
    Collection<ArtifactArchetype> getArchetypes();

    /**
     * Gets the domain for the specified identifier
     *
     * @param identifier The identifier of a domain
     * @return The associated domain, if any
     */
    BusinessDomain getDomain(String identifier);

    /**
     * Gets the schema for the specified identifier (usually its URI)
     *
     * @param identifier The identifier of a schema
     * @return The associated schema, if any
     */
    BusinessSchema getSchema(String identifier);

    /**
     * Gets the artifact archetype for the specified identifier
     *
     * @param identifier The identifier of an artifact archetype
     * @return The associated archetype, if any
     */
    ArtifactArchetype getArchetype(String identifier);
}
