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

package org.xowl.platform.services.webapp;

import org.xowl.infra.store.Serializable;

import java.util.Collection;

/**
 * Represents an additional module for the web application
 *
 * @author Laurent Wouters
 */
public interface WebModule extends Serializable {
    /**
     * Gets the name of this module
     *
     * @return The name of this module
     */
    String getName();

    /**
     * Gets the URI for this module
     *
     * @return the URI for this module
     */
    String getURI();

    /**
     * Gets the icon for this module, if any
     *
     * @return The icon for this module, if any
     */
    String getIcon();

    /**
     * Gets the items for this module, if any
     *
     * @return The items for this module
     */
    Collection<WebModuleItem> getItems();
}
