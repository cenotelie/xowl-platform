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

package org.xowl.platform.services.community.impl;

import fr.cenotelie.commons.utils.ini.IniSection;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.services.community.profiles.ProfileService;
import org.xowl.platform.services.community.profiles.ProfileServiceProvider;

/**
 * The provider of profile services for the xOWL platform
 */
public class XOWLProfileServiceProvider implements ProfileServiceProvider {

    @Override
    public String getIdentifier() {
        return XOWLProfileServiceProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Profile Service Provider";
    }

    @Override
    public ProfileService instantiate(String identifier, IniSection configuration) {
        if (XOWLProfileServiceLocalImpl.class.getCanonicalName().equals(identifier))
            return new XOWLProfileServiceLocalImpl(configuration);
        return null;
    }
}
