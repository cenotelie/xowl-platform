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

import fr.cenotelie.commons.utils.Serializable;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents the sharing of an secured resource with others
 *
 * @author Laurent Wouters
 */
public interface SecuredResourceSharing extends Serializable {
    /**
     * Gets whether the specified platform user is allowed access to the resource
     *
     * @param securityService The current security service
     * @param user            A platform user
     * @return Whether the platform user is allowed access
     */
    boolean isAllowedAccess(SecurityService securityService, PlatformUser user);
}
