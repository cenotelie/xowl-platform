/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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

import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Serializable;

/**
 * Represents a port of a web module
 *
 * @author Laurent Wouters
 */
public class WebModulePart implements Serializable {
    /**
     * The name of this part
     */
    private final String name;
    /**
     * The URI element for this part
     */
    private final String uri;
    /**
     * The URI for the icon, if any
     */
    private final String icon;

    /**
     * Initializes this web module part
     *
     * @param name The name of this part
     * @param uri  The URI element for this part
     * @param icon The URI for the icon, if any
     */
    public WebModulePart(String name, String uri, String icon) {
        this.name = name;
        this.uri = uri;
        this.icon = icon;
    }

    /**
     * Gets the name of this part
     *
     * @return The name of this part
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the URI element for this part
     *
     * @return The URI element for this part
     */
    public String getUri() {
        return uri;
    }

    @Override
    public String serializedString() {
        return name;
    }

    @Override
    public String serializedJSON() {
        return "{\"name\": \"" +
                IOUtils.escapeStringJSON(name) +
                "\", \"uri\": \"" +
                IOUtils.escapeStringJSON(uri) +
                "\", \"icon\": \"" +
                (icon == null ? "" : IOUtils.escapeStringJSON(icon)) +
                "\"}";
    }
}
