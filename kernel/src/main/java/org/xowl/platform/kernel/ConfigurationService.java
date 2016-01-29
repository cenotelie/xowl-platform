/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.kernel;

import org.xowl.infra.utils.config.Configuration;
import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.Service;

import java.io.File;

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
    Configuration getConfigFor(Identifiable entity);

    /**
     * Resolves the specified file name against the configuration repository represented by this service
     *
     * @param file The file to resolve
     * @return The resolved file
     */
    File resolve(String file);
}