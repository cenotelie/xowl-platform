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

import org.xowl.infra.store.IOUtils;

/**
 * Base implementation of a domain parameter
 *
 * @author Laurent Wouters
 */
public class ConnectorDescriptionBaseParam implements ConnectorDescriptionParam {
    /**
     * The parameter's unique identifier
     */
    protected final String identifier;
    /**
     * The parameter's name
     */
    protected final String name;
    /**
     * Whether the parameter is required
     */
    protected final boolean isRequired;
    /**
     * The type hint for the parameter
     */
    protected final String typeHint;

    /**
     * Initializes this parameter
     *
     * @param id         The parameter's unique identifier
     * @param name       The parameter's name
     * @param isRequired Whether the parameter is required
     * @param typeHint   The type hint for the parameter
     */
    public ConnectorDescriptionBaseParam(String id, String name, boolean isRequired, String typeHint) {
        this.identifier = id;
        this.name = name;
        this.isRequired = isRequired;
        this.typeHint = typeHint;
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
    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public String typeHint() {
        return typeHint;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                IOUtils.escapeStringJSON(ConnectorDescriptionParam.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                IOUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(name) +
                "\", \"isRequired\": " +
                isRequired +
                ", \"typeHint\": \"" +
                IOUtils.escapeStringJSON(typeHint) +
                "\"}";
    }
}
