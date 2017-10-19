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

import fr.cenotelie.commons.utils.api.Reply;
import org.xowl.platform.kernel.Service;

/**
 * Represents a part of the security service for the management of security tokens
 *
 * @author Laurent Wouters
 */
public interface SecurityTokenService extends Service {
    /**
     * Gets the name to use for security tokens
     *
     * @return The name to use for security tokens
     */
    String getTokenName();

    /**
     * Builds an authentication token for the specified user login
     *
     * @param login The user login
     * @return The new authentication token
     */
    String newTokenFor(String login);

    /**
     * Checks whether the specified token is valid
     *
     * @param token The authentication token to check
     * @return The protocol reply, or null if the token is invalid
     */
    Reply checkToken(String token);
}
