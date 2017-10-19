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
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Basic implementation of an authorization policy
 *
 * @author Laurent Wouters
 */
public abstract class SecuredActionPolicyBase implements SecuredActionPolicy {
    /**
     * The identifier for this policy
     */
    protected final String identifier;
    /**
     * The name of this policy
     */
    protected final String name;

    /**
     * Initializes this policy
     *
     * @param identifier The identifier for this policy
     * @param name       The name of this policy
     */
    protected SecuredActionPolicyBase(String identifier, String name) {
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
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(SecuredActionPolicy.class.getCanonicalName()) +
                "\", \"identifier\":\"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\"}";
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, SecuredAction action, Object data) {
        return isAuthorized(securityService, user, action);
    }

    /**
     * Loads a policy definition
     *
     * @param definition The serialized definition
     */
    public static SecuredActionPolicy load(ASTNode definition) {
        String identifier = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
                break;
            }
        }
        if (identifier == null)
            return null;
        for (SecuredActionPolicyProvider provider : Register.getComponents(SecuredActionPolicyProvider.class)) {
            SecuredActionPolicy result = provider.newPolicy(identifier, definition);
            if (result != null)
                return result;
        }
        return null;
    }
}
