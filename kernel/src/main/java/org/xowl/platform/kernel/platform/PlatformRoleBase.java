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

package org.xowl.platform.kernel.platform;

import org.xowl.infra.utils.TextUtils;

/**
 * Base implementation of a user role for the platform
 *
 * @author Laurent Wouters
 */
public class PlatformRoleBase implements PlatformRole {
    /**
     * The unique identifier of this role
     */
    private final String identifier;
    /**
     * The human-readable name of this role
     */
    private final String name;

    /**
     * Initializes this role
     *
     * @param identifier The unique identifier of this role
     * @param name       The human-readable name of this role
     */
    public PlatformRoleBase(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
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
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \""
                + TextUtils.escapeStringJSON(PlatformRole.class.getCanonicalName())
                + "\", \"identifier\": \""
                + TextUtils.escapeStringJSON(identifier)
                + "\", \"name\":\""
                + TextUtils.escapeStringJSON(name)
                + "\"}";
    }
}
