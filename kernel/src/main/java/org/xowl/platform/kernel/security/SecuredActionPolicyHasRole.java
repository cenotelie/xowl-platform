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
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents an authorization policy that requires the user to have a specific role
 *
 * @author Laurent Wouters
 */
public class SecuredActionPolicyHasRole extends SecuredActionPolicyBase {
    /**
     * The descriptor for this policy
     */
    public static final SecuredActionPolicyDescriptor DESCRIPTOR = new SecuredActionPolicyDescriptor(
            SecuredActionPolicyHasRole.class.getCanonicalName(),
            "User has role",
            new SecuredActionPolicyDescriptor.Parameter("role", PlatformRole.class.getCanonicalName()));

    /**
     * The identifier of the required role
     */
    protected final PlatformRole role;

    /**
     * Initializes this policy
     *
     * @param role The required role
     */
    public SecuredActionPolicyHasRole(PlatformRole role) {
        super(DESCRIPTOR.getIdentifier(), DESCRIPTOR.getName());
        this.role = role;
    }

    /**
     * Creates a new instance from a serialized definition
     *
     * @param definition The serialized definition
     * @return The configured policy, or null if the definition is not valid
     */
    public static SecuredActionPolicyHasRole newInstance(ASTNode definition) {
        String roleId = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("role".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                roleId = value.substring(1, value.length() - 1);
            }
        }
        if (roleId == null)
            return null;
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformRole role = securityService.getRealm().getRole(roleId);
        if (role == null)
            return null;
        return new SecuredActionPolicyHasRole(role);
    }

    @Override
    public SecuredActionPolicyDescriptor getDescriptor() {
        return DESCRIPTOR;
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
                TextUtils.escapeStringJSON(role.getIdentifier()) +
                "\"}";
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, SecuredAction action) {
        return securityService.getRealm().checkHasRole(user.getIdentifier(), role.getIdentifier());
    }
}
