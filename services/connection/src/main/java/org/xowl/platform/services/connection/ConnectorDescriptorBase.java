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

package org.xowl.platform.services.connection;

import org.xowl.infra.utils.TextUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * Base implementation of a domain description
 *
 * @author Laurent Wouters
 */
public class ConnectorDescriptorBase implements ConnectorDescriptor {
    /**
     * The domain's unique identifier
     */
    protected final String identifier;
    /**
     * The domain's name
     */
    protected final String name;
    /**
     * The domain's description
     */
    protected final String description;

    /**
     * Initializes this description
     *
     * @param identifier  The domain's unique identifier
     * @param name        The domain's name
     * @param description The domain's description
     */
    public ConnectorDescriptorBase(String identifier, String name, String description) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
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
    public String getDescription() {
        return description;
    }

    @Override
    public Collection<ConnectorDescriptorParam> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(ConnectorDescriptor.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"description\": \"");
        builder.append(TextUtils.escapeStringJSON(description));
        builder.append("\", \"parameters\": [");
        boolean first = true;
        for (ConnectorDescriptorParam param : getParameters()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(param.serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}