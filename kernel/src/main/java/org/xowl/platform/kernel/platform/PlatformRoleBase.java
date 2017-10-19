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

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.hime.redist.ASTNode;

/**
 * Base implementation of a user role for the platform
 *
 * @author Laurent Wouters
 */
public class PlatformRoleBase implements PlatformRole {
    /**
     * The unique identifier of this role
     */
    private final String identifier;
    /**
     * The human-readable name of this role
     */
    private final String name;

    /**
     * Initializes this role
     *
     * @param identifier The unique identifier of this role
     * @param name       The human-readable name of this role
     */
    public PlatformRoleBase(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    /**
     * Initializes this role
     *
     * @param definition The AST node for the serialized definition
     */
    public PlatformRoleBase(ASTNode definition) {
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
        return "{\"type\": \""
                + TextUtils.escapeStringJSON(PlatformRole.class.getCanonicalName())
                + "\", \"identifier\": \""
                + TextUtils.escapeStringJSON(identifier)
                + "\", \"name\":\""
                + TextUtils.escapeStringJSON(name)
                + "\"}";
    }
}
