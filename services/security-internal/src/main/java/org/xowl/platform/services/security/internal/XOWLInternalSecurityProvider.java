/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

import org.xowl.infra.utils.config.Section;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.security.SecurityRealm;
import org.xowl.platform.kernel.security.SecurityRealmProvider;

/**
 * Implements a provider for the internal security realm
 *
 * @author Laurent Wouters
 */
class XOWLInternalSecurityProvider implements SecurityRealmProvider {
    @Override
    public String getIdentifier() {
        return XOWLInternalSecurityProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Internal Security Provider";
    }

    @Override
    public SecurityRealm newRealm(String identifier, Section configuration) {
        if (XOWLInternalRealm.class.getCanonicalName().equals(identifier))
            return new XOWLInternalRealm(configuration);
        return null;
    }
}
