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

package org.xowl.platform.services.security.internal;

import org.xowl.infra.store.IOUtils;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents a user on this platform
 *
 * @author Laurent Wouters
 */
class XOWLInternalUser implements PlatformUser {
    /**
     * The identifier of this user
     */
    private final String identifier;

    /**
     * Initializes this user
     *
     * @param identifier The identifier of this user
     */
    public XOWLInternalUser(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return identifier;
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                IOUtils.escapeStringJSON(XOWLInternalUser.class.getCanonicalName()) +
                "\", \"id\": \"" +
                IOUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(getName()) +
                "\"}";
    }

    @Override
    public boolean hasRole(String role) {
        // TODO: fill this
        return true;
    }
}
