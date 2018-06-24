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

import fr.cenotelie.commons.utils.ini.IniSection;
import org.xowl.platform.kernel.Registrable;

/**
 * Represents a provider of security token services
 *
 * @author Laurent Wouters
 */
public interface SecurityTokenServiceProvider extends Registrable {
    /**
     * Tries to instantiates a token security service
     *
     * @param identifier    The identifier of the service to instantiate
     * @param configuration The configuration for the service
     * @return The security token service, or null if it cannot be instantiated
     */
    SecurityTokenService newService(String identifier, IniSection configuration);
}
