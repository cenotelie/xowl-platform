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

package org.xowl.platform.services.webapp;

import org.xowl.platform.kernel.Service;

import java.net.URL;

/**
 * Represents an additional module for the web application
 *
 * @author Laurent Wouters
 */
public interface WebModuleService extends Service {
    /**
     * The modules folder
     */
    String MODULES = "/modules/";

    /**
     * Gets the URI part for this module
     *
     * @return The URI part for this module
     */
    String getURI();

    /**
     * Gets the URL for the requested resource
     *
     * @param resource The requested resource, local to this module
     * @return The URL for the requested resource
     */
    URL getResource(String resource);
}
