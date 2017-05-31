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
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents the sharing of an owned resource with everybody in the collaboration
 *
 * @author Laurent Wouters
 */
public class OwnedResourceSharingWithEverybody implements OwnedResourceSharing {
    /**
     * Initializes this sharing
     */
    public OwnedResourceSharingWithEverybody() {
    }

    /**
     * Initializes this sharing
     *
     * @param definition The serialized definition
     */
    public OwnedResourceSharingWithEverybody(ASTNode definition) {
    }

    @Override
    public boolean isAllowedAccess(SecurityService securityService, PlatformUser user) {
        return true;
    }

    @Override
    public String serializedString() {
        return "everybody";
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(OwnedResourceSharingWithEverybody.class.getCanonicalName()) +
                "\"}";
    }

    @Override
    public boolean equals(Object object) {
        return (object != null && object instanceof OwnedResourceSharingWithEverybody);
    }
}
