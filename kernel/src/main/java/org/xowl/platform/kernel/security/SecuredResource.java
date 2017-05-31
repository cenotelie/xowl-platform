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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.utils.ApiError;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.platform.PlatformUser;

import java.util.Collection;

/**
 * Represents a resource that is owned by a platform user and can be shared with others in the collaboration
 *
 * @author Laurent Wouters
 */
public interface SecuredResource extends Identifiable, Serializable {
    /**
     * Action to change the ownership of the resource
     */
    SecuredAction ACTION_MANAGE_OWNERSHIP = new SecuredAction(SecuredResource.class.getCanonicalName() + ".ManageOwnership", "Secured Resource - Manage Ownership", SecuredActionPolicyIsResourceOwner.DESCRIPTOR);
    /**
     * Action to manage the sharing of the resource
     */
    SecuredAction ACTION_MANAGE_SHARING = new SecuredAction(SecuredResource.class.getCanonicalName() + ".ManageSharing", "Secured Resource - Manage Sharing", SecuredActionPolicyIsResourceOwner.DESCRIPTOR);
    /**
     * Action to access the resource
     */
    SecuredAction ACTION_ACCESS = new SecuredAction(SecuredResource.class.getCanonicalName() + ".Access", "Secured Resource - Access", SecuredActionPolicyIsInSharing.DESCRIPTOR);

    /**
     * API error - The provided user is already an owner of the resource
     */
    ApiError ERROR_ALREADY_OWNER = new ApiError(0x00000036,
            "The provided user is already an owner of the resource.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000036.html");
    /**
     * API error - Impossible to remove the last owner of the resource
     */
    ApiError ERROR_LAST_OWNER = new ApiError(0x00000037,
            "Impossible to remove the last owner of the resource.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000037.html");

    /**
     * Gets the owners of this resource
     *
     * @return The owners of this resource
     */
    Collection<String> getOwners();

    /**
     * Adds an owner of this resource
     *
     * @param user The new owner for this resource
     * @return The protocol reply
     */
    XSPReply addOwner(PlatformUser user);

    /**
     * Removes an owner of this resource
     *
     * @param user The previous owner for this resource
     * @return The protocol reply
     */
    XSPReply removeOwner(PlatformUser user);

    /**
     * Gets the specifications of how this resource is shared
     *
     * @return The specifications of how this resource is shared
     */
    Collection<SecuredResourceSharing> getSharings();

    /**
     * Adds a sharing for this resource
     *
     * @param sharing The sharing to add
     * @return The protocol reply
     */
    XSPReply addSharing(SecuredResourceSharing sharing);

    /**
     * Remove a sharing for this resource
     *
     * @param sharing The sharing to remove
     * @return The protocol reply
     */
    XSPReply removeSharing(SecuredResourceSharing sharing);
}
