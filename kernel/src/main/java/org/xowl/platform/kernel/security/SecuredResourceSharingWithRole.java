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

package org.xowl.platform.kernel.security;

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents the sharing of an secured resource with a particular role
 *
 * @author Laurent Wouters
 */
public class SecuredResourceSharingWithRole implements SecuredResourceSharing {
    /**
     * The identifier of the allowed role
     */
    private final String role;

    /**
     * Initializes this sharing
     *
     * @param role The identifier of the allowed role
     */
    public SecuredResourceSharingWithRole(String role) {
        this.role = role;
    }

    /**
     * Initializes this sharing
     *
     * @param definition The serialized definition
     */
    public SecuredResourceSharingWithRole(ASTNode definition) {
        String role = "";
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("role".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                role = value.substring(1, value.length() - 1);
            }
        }
        this.role = role;
    }

    @Override
    public boolean isAllowedAccess(SecurityService securityService, PlatformUser user) {
        return securityService.getRealm().checkHasRole(user.getIdentifier(), role);
    }

    @Override
    public String serializedString() {
        return role;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(SecuredResourceSharingWithRole.class.getCanonicalName()) +
                "\", \"role\": \"" +
                TextUtils.escapeStringJSON(role) +
                "\"}";
    }

    @Override
    public boolean equals(Object object) {
        return (object != null
                && object instanceof SecuredResourceSharingWithRole
                && ((SecuredResourceSharingWithRole) object).role.equals(this.role));
    }
}
