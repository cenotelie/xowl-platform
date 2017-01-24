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

import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;

/**
 * Base implementation of a domain parameter
 *
 * @author Laurent Wouters
 */
public class ConnectorDescriptorParam implements Identifiable, Serializable {
    /**
     * Type hint for a string parameter
     */
    public static final String TYPE_HINT_STRING = "string";
    /**
     * Type hint for a password parameter
     */
    public static final String TYPE_HINT_PASSWORD = "password";
    /**
     * Type hint for a number parameter
     */
    public static final String TYPE_HINT_NUMBER = "number";
    /**
     * Type hint for an uri parameter
     */
    public static final String TYPE_HINT_URI = "uri";

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
    public ConnectorDescriptorParam(String id, String name, boolean isRequired, String typeHint) {
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

    /**
     * Gets whether the parameter is required
     *
     * @return Whether the parameter is required
     */
    public boolean isRequired() {
        return isRequired;
    }

    /**
     * Gets the type hint for this parameter
     *
     * @return The type hint
     */
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
                TextUtils.escapeStringJSON(ConnectorDescriptorParam.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"isRequired\": " +
                isRequired +
                ", \"typeHint\": \"" +
                TextUtils.escapeStringJSON(typeHint) +
                "\"}";
    }
}
