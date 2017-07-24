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

package org.xowl.platform.kernel.remote;

import org.xowl.platform.kernel.Register;

import java.util.Collection;

/**
 * Implements a factory that relies on the OSGi service register to discover the factories
 *
 * @author Laurent Wouters
 */
public class PlatformApiDeserializerForOSGi extends PlatformApiDeserializer {
    @Override
    protected Collection<PlatformApiFactory> getParts() {
        return Register.getComponents(PlatformApiFactory.class);
    }
}
