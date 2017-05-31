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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.platform.PlatformGroup;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents the sharing of an secured resource with a particular group
 *
 * @author Laurent Wouters
 */
public class SecuredResourceSharingWithGroup implements SecuredResourceSharing {
    /**
     * The identifier of the allowed group
     */
    private final String group;

    /**
     * Initializes this sharing
     *
     * @param group The identifier of the allowed group
     */
    public SecuredResourceSharingWithGroup(String group) {
        this.group = group;
    }

    /**
     * Initializes this sharing
     *
     * @param definition The serialized definition
     */
    public SecuredResourceSharingWithGroup(ASTNode definition) {
        String group = "";
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("group".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                group = value.substring(1, value.length() - 1);
            }
        }
        this.group = group;
    }

    @Override
    public boolean isAllowedAccess(SecurityService securityService, PlatformUser user) {
        PlatformGroup group = securityService.getRealm().getGroup(this.group);
        return group != null && (group.getUsers().contains(user) || group.getAdmins().contains(user));
    }

    @Override
    public String serializedString() {
        return group;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(SecuredResourceSharingWithGroup.class.getCanonicalName()) +
                "\", \"group\": \"" +
                TextUtils.escapeStringJSON(group) +
                "\"}";
    }

    @Override
    public boolean equals(Object object) {
        return (object != null
                && object instanceof SecuredResourceSharingWithGroup
                && ((SecuredResourceSharingWithGroup) object).group.equals(this.group));
    }
}
