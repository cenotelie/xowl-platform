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

package org.xowl.platform.kernel.artifacts;

import org.xowl.infra.store.IOUtils;
import org.xowl.platform.kernel.KernelSchema;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents the archetype of an artifact that follow no specific schema
 *
 * @author Laurent Wouters
 */
public class FreeArtifactArchetype implements ArtifactArchetype {
    /**
     * The singleton instance
     */
    public static final ArtifactArchetype INSTANCE = new FreeArtifactArchetype();

    /**
     * Initializes this archetype
     */
    private FreeArtifactArchetype() {
    }

    @Override
    public String getIdentifier() {
        return FreeArtifactArchetype.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "Free Artifact Archetype";
    }

    @Override
    public String getDescription() {
        return "An archetype for artifacts that follow no specific schema.";
    }

    @Override
    public BusinessSchema getSchema() {
        return KernelSchema.IMPL;
    }

    @Override
    public Collection<BusinessDomain> getDomains() {
        return Collections.emptyList();
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                IOUtils.escapeStringJSON(ArtifactArchetype.class.getCanonicalName()) +
                "\", \"id\": \"" +
                IOUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(getName()) +
                "\", \"description\": \"" +
                IOUtils.escapeStringJSON(getDescription()) +
                "\"}";
    }
}
