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
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents a manager of secured resources
 *
 * @author Laurent Wouters
 */
public interface SecuredResourceManager extends Identifiable {
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
     * Creates a security descriptor for the specified resource
     *
     * @param resource A secured resource
     * @return The protocol reply
     */
    XSPReply createDescriptorFor(SecuredResource resource);

    /**
     * Gets the security descriptor for the specified resource
     *
     * @param resource A secured resource
     * @return The protocol reply
     */
    XSPReply getDescriptorFor(String resource);

    /**
     * Adds an owner of a resource
     *
     * @param resource A secured resource
     * @param user     The new owner for this resource
     * @return The protocol reply
     */
    XSPReply addOwner(String resource, String user);

    /**
     * Removes an owner of a resource
     *
     * @param resource A secured resource
     * @param user     The previous owner for this resource
     * @return The protocol reply
     */
    XSPReply removeOwner(String resource, String user);

    /**
     * Adds a sharing for a resource
     *
     * @param resource A secured resource
     * @param sharing  The sharing to add
     * @return The protocol reply
     */
    XSPReply addSharing(String resource, SecuredResourceSharing sharing);

    /**
     * Remove a sharing for a resource
     *
     * @param resource A secured resource
     * @param sharing  The sharing to remove
     * @return The protocol reply
     */
    XSPReply removeSharing(String resource, SecuredResourceSharing sharing);

    /**
     * Deletes the security descriptor for the specified resource
     *
     * @param resource A secured resource
     * @return The protocol reply
     */
    XSPReply deleteDescriptorFor(String resource);

    /**
     * Checks whether the current user is an owner of the specified resource
     *
     * @param securityService The current security service
     * @param user            The user
     * @param resource        The identifier of the resource
     * @return The protocol reply
     */
    XSPReply checkIsResourceOwner(SecurityService securityService, PlatformUser user, String resource);

    /**
     * Checks whether the current user is either an owner of is part of the sharing of the specified resource
     *
     * @param securityService The current security service
     * @param user            The user
     * @param resource        The identifier of the resource
     * @return The protocol reply
     */
    XSPReply checkIsInSharing(SecurityService securityService, PlatformUser user, String resource);
}
