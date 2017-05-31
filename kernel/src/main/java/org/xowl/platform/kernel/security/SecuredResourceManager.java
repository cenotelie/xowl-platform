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
    XSPReply getDescriptorFor(SecuredResource resource);

    /**
     * Deletes the security descriptor for the specified resource
     *
     * @param resource A secured resource
     * @return The protocol reply
     */
    XSPReply deleteDescriptorFor(SecuredResource resource);

    /**
     * Checks whether the current user is an owner of the specified resource
     *
     * @return The protocol reply
     */
    XSPReply checkIsResourceOwner(SecuredResource resource);

    /**
     * Checks whether the current user is either an owner of is part of the sharing of the specified resource
     *
     * @param resource The secured resource
     * @return The protocol reply
     */
    XSPReply checkIsInSharing(SecuredResource resource);
}
