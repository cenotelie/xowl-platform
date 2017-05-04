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

package org.xowl.platform.services.collaboration;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;

/**
 * Base implementation for collaboration patterns
 *
 * @author Laurent Wouters
 */
public class CollaborationPatternBase implements CollaborationPattern {
    /**
     * The identifier of this pattern
     */
    private final String identifier;
    /**
     * The name of this pattern
     */
    private final String name;

    /**
     * Initializes this pattern
     *
     * @param identifier The identifier of this pattern
     * @param name       The name of this pattern
     */
    public CollaborationPatternBase(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    /**
     * Initializes this pattern
     *
     * @param definition The AST node for the serialized definition
     */
    public CollaborationPatternBase(ASTNode definition) {
        String identifier = "";
        String name = "";
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            }
        }
        this.identifier = identifier;
        this.name = name;
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
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" + TextUtils.escapeStringJSON(CollaborationPattern.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\"}";
    }

    @Override
    public String toString() {
        return identifier;
    }
}
