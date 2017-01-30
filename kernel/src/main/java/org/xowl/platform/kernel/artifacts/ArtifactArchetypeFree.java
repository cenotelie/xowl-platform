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

import org.xowl.platform.kernel.KernelSchema;

/**
 * Represents the archetype of an artifact that follow no specific schema
 *
 * @author Laurent Wouters
 */
public class ArtifactArchetypeFree extends ArtifactArchetypeBase {
    /**
     * The singleton instance
     */
    public static final ArtifactArchetype INSTANCE = new ArtifactArchetypeFree(
            ArtifactArchetypeFree.class.getCanonicalName(),
            "Free Artifact Archetype",
            "An archetype for artifacts that follow no specific schema."
    );

    /**
     * Initializes this archetype
     *
     * @param identifier  The archetype's identifier
     * @param name        The archetype's name
     * @param description The archetype's description
     */
    private ArtifactArchetypeFree(String identifier, String name, String description) {
        super(identifier, name, description);
    }

    @Override
    public ArtifactSchema getSchema() {
        return KernelSchema.IMPL;
    }
}
