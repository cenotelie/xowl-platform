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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents an authorization policy that requires the user to have a specific role
 *
 * @author Laurent Wouters
 */
public class SecuredActionPolicyHasRole extends SecuredActionPolicyBase {
    /**
     * The identifier of the required role
     */
    protected final String role;

    /**
     * Initializes this policy
     *
     * @param roleId The identifier of the required role
     */
    public SecuredActionPolicyHasRole(String roleId) {
        super(SecuredActionPolicyHasRole.class.getCanonicalName(), "Has Role");
        this.role = roleId;
    }

    /**
     * Initializes this policy
     *
     * @param role The required role
     */
    public SecuredActionPolicyHasRole(PlatformRole role) {
        this(role.getIdentifier());
    }

    /**
     * Initializes this policy
     *
     * @param definition The serialized definition
     */
    public SecuredActionPolicyHasRole(ASTNode definition) {
        super(SecuredActionPolicyHasRole.class.getCanonicalName(), "Role policy");
        String role = null;
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
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(SecuredActionPolicy.class.getCanonicalName()) +
                "\", \"identifier\":\"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"role\": \"" +
                TextUtils.escapeStringJSON(role) +
                "\"}";
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, SecuredAction action) {
        return securityService.getRealm().checkHasRole(user.getIdentifier(), role);
    }
}
