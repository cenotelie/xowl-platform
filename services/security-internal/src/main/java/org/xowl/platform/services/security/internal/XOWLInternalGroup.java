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

import org.xowl.platform.kernel.platform.PlatformGroupBase;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformUser;

import java.util.Collection;

/**
 * Represents a group of users on this platform
 *
 * @author Laurent Wouters
 */
public class XOWLInternalGroup extends PlatformGroupBase {
    /**
     * The parent realm
     */
    private final XOWLInternalRealm realm;

    /**
     * Initializes this user
     *
     * @param realm      The parent realm
     * @param identifier The identifier of this group
     * @param name       The name of this group
     */
    public XOWLInternalGroup(XOWLInternalRealm realm, String identifier, String name) {
        super(identifier, name);
        this.realm = realm;
    }

    @Override
    public Collection<PlatformUser> getUsers() {
        return null;
    }

    @Override
    public Collection<PlatformUser> getAdmins() {
        return null;
    }

    @Override
    public Collection<PlatformRole> getRoles() {
        return realm.getEntityRoles(XOWLInternalRealm.GROUPS + getIdentifier());
    }
}
