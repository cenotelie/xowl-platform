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

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.hime.redist.ASTNode;

/**
 * Base implementation for artifact archetypes
 *
 * @author Laurent Wouters
 */
public class ArtifactArchetypeBase implements ArtifactArchetype {
    /**
     * The archetype's identifier
     */
    protected final String identifier;
    /**
     * The archetype's name
     */
    protected final String name;
    /**
     * The archetype's description
     */
    protected final String description;

    /**
     * Initializes this archetype
     *
     * @param identifier  The archetype's identifier
     * @param name        The archetype's name
     * @param description The archetype's description
     */
    public ArtifactArchetypeBase(String identifier, String name, String description) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
    }

    /**
     * Initializes this archetype
     *
     * @param definition The JSON definition
     */
    public ArtifactArchetypeBase(ASTNode definition) {
        String identifier = null;
        String name = null;
        String description = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("description".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                description = value.substring(1, value.length() - 1);
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(ArtifactArchetype.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"description\": \"" +
                TextUtils.escapeStringJSON(description) +
                "\", \"schema\": \"" +
                (getSchema() != null ? TextUtils.escapeStringJSON(getSchema().getIdentifier()) : "") +
                "\"}";
    }

    @Override
    public ArtifactSchema getSchema() {
        return null;
    }
}
