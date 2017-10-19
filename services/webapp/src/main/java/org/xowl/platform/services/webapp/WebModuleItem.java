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

import fr.cenotelie.commons.utils.Serializable;
import fr.cenotelie.commons.utils.TextUtils;

/**
 * Represents an item in a web module
 *
 * @author Laurent Wouters
 */
public class WebModuleItem implements Serializable {
    /**
     * The name of this item
     */
    private final String name;
    /**
     * The URI element for this item
     */
    private final String uri;
    /**
     * The URI for the icon, if any
     */
    private final String icon;

    /**
     * Initializes this web module item
     *
     * @param name The name of this item
     * @param uri  The URI element for this item
     * @param icon The URI for the icon, if any
     */
    public WebModuleItem(String name, String uri, String icon) {
        this.name = name;
        this.uri = uri;
        this.icon = icon;
    }

    /**
     * Gets the name of this item
     *
     * @return The name of this item
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the URI element for this item
     *
     * @return The URI element for this item
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
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(WebModuleItem.class.getCanonicalName()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"uri\": \"" +
                TextUtils.escapeStringJSON(uri) +
                "\", \"icon\": \"" +
                (icon == null ? "" : TextUtils.escapeStringJSON(icon)) +
                "\"}";
    }
}
