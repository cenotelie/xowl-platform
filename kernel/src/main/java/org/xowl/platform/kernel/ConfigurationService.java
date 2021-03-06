/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.kernel;

import fr.cenotelie.commons.utils.Identifiable;
import fr.cenotelie.commons.utils.ini.IniDocument;

/**
 * The configuration service that provides configuration elements to other entities
 *
 * @author Laurent Wouters
 */
public interface ConfigurationService extends Service {
    /**
     * Gets the configuration for the specified entity
     *
     * @param entity An entity
     * @return The corresponding configuration element
     */
    IniDocument getConfigFor(Identifiable entity);

    /**
     * Gets the configuration for the entity with the specified identifier
     *
     * @param entityId The identifier of an entity
     * @return The corresponding configuration element
     */
    IniDocument getConfigFor(String entityId);
}
