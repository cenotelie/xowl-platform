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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.security.SecurityRealmProvider;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    /**
     * The created security realms
     */
    private static Collection<XOWLInternalRealm> realms = new ArrayList<>();

    /**
     * Register an internal security realm
     *
     * @param realm The realm to register
     */
    public static synchronized void register(XOWLInternalRealm realm) {
        realms.add(realm);
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(SecurityRealmProvider.class, new XOWLInternalSecurityProvider(), null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        for (XOWLInternalRealm realm : realms)
            realm.onStop();
    }
}