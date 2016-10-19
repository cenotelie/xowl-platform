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

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.xowl.platform.kernel.Identifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The descriptor of the platform
 *
 * @author Laurent Wouters
 */
public class PlatformDescriptor implements Identifiable {
    /**
     * The singleton instance
     */
    public static final PlatformDescriptor INSTANCE = new PlatformDescriptor();

    /**
     * The cache of bundles
     */
    private final List<OSGiBundle> bundles;

    /**
     * Initializes the descriptor
     */
    private PlatformDescriptor() {
        bundles = new ArrayList<>();
    }

    @Override
    public String getIdentifier() {
        return PlatformDescriptor.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform";
    }

    /**
     * Gets the description of the bundles on this platform
     *
     * @return The bundle on this platform
     */
    public Collection<OSGiBundle> getPlatformBundles() {
        if (bundles.isEmpty()) {
            Bundle[] bundles = FrameworkUtil.getBundle(OSGiBundle.class).getBundleContext().getBundles();
            for (int i = 0; i != bundles.length; i++) {
                this.bundles.add(new OSGiBundle(bundles[i]));
            }
        }
        return Collections.unmodifiableCollection(bundles);
    }
}
