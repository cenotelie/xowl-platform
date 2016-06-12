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

import org.xowl.infra.store.IOUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base implementation of a web module service
 *
 * @author Laurent Wouters
 */
public abstract class WebModuleBase implements WebModule {
    /**
     * The service's identifier
     */
    protected final String identifier;
    /**
     * The service's human readable name
     */
    protected final String name;
    /**
     * The service URI part
     */
    protected final String uri;
    /**
     * The icon for this module, if any
     */
    protected final String icon;
    /**
     * The items of this module
     */
    protected final List<WebModuleItem> items;

    /**
     * Initializes this service
     *
     * @param identifier The service's identifier
     * @param name       The service's human readable name
     * @param uri        The service URI part
     * @param icon       The icon for this module, if any
     */
    public WebModuleBase(String identifier, String name, String uri, String icon) {
        this.identifier = identifier;
        this.name = name;
        this.uri = uri;
        this.icon = icon;
        this.items = new ArrayList<>(5);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getURI() {
        return uri;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public Collection<WebModuleItem> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"identifier\": \"");
        builder.append(IOUtils.escapeStringJSON(getIdentifier()));
        builder.append("\", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(getName()));
        builder.append("\", \"uri\": \"");
        builder.append(IOUtils.escapeStringJSON(getURI()));
        builder.append("\", \"icon\": \"");
        builder.append(getIcon() == null ? "" : IOUtils.escapeStringJSON(getIcon()));
        builder.append("\", \"items\": [");
        for (int i = 0; i != items.size(); i++) {
            if (i != 0)
                builder.append(", ");
            builder.append(items.get(i).serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}
