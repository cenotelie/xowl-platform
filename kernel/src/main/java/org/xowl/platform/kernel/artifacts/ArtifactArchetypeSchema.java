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

/**
 * Implements an archetype of schemas stored in artifacts
 *
 * @author Laurent Wouters
 */
public class ArtifactArchetypeSchema extends ArtifactArchetypeBase {
    /**
     * The singleton instance
     */
    public static final ArtifactArchetype INSTANCE = new ArtifactArchetypeSchema(
            ArtifactArchetypeSchema.class.getCanonicalName(),
            "Schema Artifact Archetype",
            "An archetype for artifacts that contain schemas for other artifacts."
    );

    /**
     * Initializes this archetype
     *
     * @param identifier  The archetype's identifier
     * @param name        The archetype's name
     * @param description The archetype's description
     */
    private ArtifactArchetypeSchema(String identifier, String name, String description) {
        super(identifier, name, description);
    }

    @Override
    public ArtifactSchema getSchema() {
        return ArtifactSchemaRDFS.INSTANCE;
    }
}
