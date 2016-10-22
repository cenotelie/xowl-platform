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

import java.util.Collection;
import java.util.Collections;

/**
 * The root user for the platform
 *
 * @author Laurent Wouters
 */
public class PlatformUserRoot extends PlatformUserBase {
    /**
     * The singleton instance
     */
    private static final PlatformUser INSTANCE = new PlatformUserRoot();

    /**
     * Initializes this user
     */
    private PlatformUserRoot() {
        super("root", "Platform root user");
    }

    @Override
    public Collection<PlatformRole> getRoles() {
        return Collections.singletonList(PlatformRoleAdmin.INSTANCE);
    }
}
