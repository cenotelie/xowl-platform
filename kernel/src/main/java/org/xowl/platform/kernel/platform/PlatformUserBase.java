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

package org.xowl.platform.kernel.platform;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;

/**
 * Base implementation for a platform user
 *
 * @author Laurent Wouters
 */
public abstract class PlatformUserBase implements PlatformUser {
    /**
     * The identifier of this user
     */
    private final String identifier;
    /**
     * The name of this user
     */
    private final String name;

    /**
     * Initializes this user
     *
     * @param identifier The identifier of this user
     * @param name       The name of this user
     */
    public PlatformUserBase(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    /**
     * Initializes this user
     *
     * @param definition The AST node for the serialized definition
     */
    public PlatformUserBase(ASTNode definition) {
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
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(PlatformUser.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"roles\": [");
        boolean first = true;
        for (PlatformRole role : getRoles()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("{\"type\": \"");
            builder.append(TextUtils.escapeStringJSON(PlatformRole.class.getCanonicalName()));
            builder.append("\", \"identifier\": \"");
            builder.append(TextUtils.escapeStringJSON(role.getIdentifier()));
            builder.append("\", \"name\": \"");
            builder.append(TextUtils.escapeStringJSON(role.getName()));
            builder.append("\"}");
        }
        builder.append("]}");
        return builder.toString();
    }

    @Override
    public String toString() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof PlatformUser && this.identifier.equals(((PlatformUser) o).getIdentifier()));
    }
}
