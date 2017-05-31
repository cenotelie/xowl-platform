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
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.platform.kernel.platform.PlatformUser;

import java.util.Collection;

/**
 * Represents a resource that is owned by a platform user and can be shared with others in the collaboration
 *
 * @author Laurent Wouters
 */
public interface OwnedResource extends Identifiable, Serializable {
    /**
     * Action to change the owner of the resource
     */
    SecuredAction ACTION_CHANGE_OWNER = new SecuredAction(OwnedResource.class.getCanonicalName() + ".ChangeOwner", "Owned Resource - Change Owner", SecuredActionPolicyIsResourceOwner.DESCRIPTOR);
    /**
     * Action to manage the sharing of the resource
     */
    SecuredAction ACTION_MANAGE_SHARING = new SecuredAction(OwnedResource.class.getCanonicalName() + ".ManageSharing", "Owned Resource - Manage Sharing", SecuredActionPolicyIsResourceOwner.DESCRIPTOR);
    /**
     * Action to access the resource
     */
    SecuredAction ACTION_ACCESS = new SecuredAction(OwnedResource.class.getCanonicalName() + ".Access", "Owned Resource - Access", SecuredActionPolicyIsAllowedAccessToResource.DESCRIPTOR);

    /**
     * Gets the owner of this resource
     *
     * @return The owner of this resource
     */
    String getOwner();

    /**
     * Changes the owner of this resource
     *
     * @param user The new owner for this resource
     * @return The protocol reply
     */
    XSPReply setOwner(PlatformUser user);

    /**
     * Gets the specifications of how this resource is shared
     *
     * @return The specifications of how this resource is shared
     */
    Collection<OwnedResourceSharing> getSharings();

    /**
     * Adds a sharing for this resource
     *
     * @param sharing The sharing to add
     * @return The protocol reply
     */
    XSPReply addSharing(OwnedResourceSharing sharing);

    /**
     * Remove a sharing for this resource
     *
     * @param sharing The sharing to remove
     * @return The protocol reply
     */
    XSPReply removeSharing(OwnedResourceSharing sharing);
}
