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

package org.xowl.platform.services.connection;

import org.xowl.infra.store.IOUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * Base implementation of a domain description
 *
 * @author Laurent Wouters
 */
public class DomainDescriptionBase implements DomainDescription {

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
    public DomainDescriptionBase(String identifier, String name, String description) {
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
    public Collection<DomainDescriptionParam> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(IOUtils.escapeStringJSON(DomainDescription.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(IOUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(name));
        builder.append("\", \"description\": \"");
        builder.append(IOUtils.escapeStringJSON(description));
        builder.append("\", \"parameters\": [");
        boolean first = true;
        for (DomainDescriptionParam param : getParameters()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(param.serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}
