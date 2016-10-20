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

import org.xowl.infra.store.IOUtils;
import org.xowl.platform.kernel.KernelSchema;

import java.util.Collection;
import java.util.Collections;

/**
 * Implements an archetype of schemas stored in artifacts
 *
 * @author Laurent Wouters
 */
public class SchemaArtifactArchetype implements ArtifactArchetype {
    /**
     * The singleton instance
     */
    public static final ArtifactArchetype INSTANCE = new SchemaArtifactArchetype();

    /**
     * Initializes this archetype
     */
    private SchemaArtifactArchetype() {
    }

    @Override
    public String getIdentifier() {
        return SchemaArtifactArchetype.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "Schema Artifact Archetype";
    }

    @Override
    public String getDescription() {
        return "An archetype for artifacts that contain schemas for other artifacts.";
    }

    @Override
    public BusinessSchema getSchema() {
        return KernelSchema.IMPL;
    }

    @Override
    public Collection<BusinessDomain> getDomains() {
        return Collections.singletonList(SchemaDomain.INSTANCE);
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                IOUtils.escapeStringJSON(ArtifactArchetype.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                IOUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(getName()) +
                "\", \"description\": \"" +
                IOUtils.escapeStringJSON(getDescription()) +
                "\"}";
    }
}
