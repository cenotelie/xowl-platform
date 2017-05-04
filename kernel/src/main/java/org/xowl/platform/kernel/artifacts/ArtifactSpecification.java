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

package org.xowl.platform.kernel.artifacts;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;

/**
 * Represents the specification of an artifact
 *
 * @author Laurent Wouters
 */
public class ArtifactSpecification implements Identifiable, Serializable {
    /**
     * The identifier for this specification
     */
    private final String identifier;
    /**
     * The name for this specification
     */
    private final String name;
    /**
     * The identifier of the archetype for the artifacts that match this specification
     */
    private final String archetype;

    /**
     * Initializes this specification
     *
     * @param identifier The identifier of this specification
     * @param name       The name for this specification
     * @param archetype  The identifier of the archetype for the artifacts that match this specification
     */
    public ArtifactSpecification(String identifier, String name, String archetype) {
        this.identifier = identifier;
        this.name = name;
        this.archetype = archetype;
    }

    /**
     * Initializes this specification
     *
     * @param definition The AST node for the serialized definition
     */
    public ArtifactSpecification(ASTNode definition) {
        String identifier = "";
        String name = "";
        String archetype = "";
        for (ASTNode pair : definition.getChildren()) {
            String key = TextUtils.unescape(pair.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
            value = value.substring(1, value.length() - 1);
            switch (key) {
                case "identifier":
                    identifier = value;
                    break;
                case "name":
                    name = value;
                    break;
                case "archetype":
                    archetype = value;
                    break;
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.archetype = archetype;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the identifier of the archetype for the artifacts that match this specification
     *
     * @return The identifier of the archetype for the artifacts that match this specification
     */
    public String getArchetype() {
        return archetype;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(ArtifactSpecification.class.getCanonicalName()) +
                "\", \"identifier\":\"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"archetype\": \"" +
                TextUtils.escapeStringJSON(archetype) +
                "\"}";
    }
}
