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

package org.xowl.platform.kernel;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.artifacts.*;
import org.xowl.platform.kernel.remote.PlatformApiFactory;
import org.xowl.platform.kernel.remote.PlatformApiFactoryForKernel;

/**
 * Activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        // register the lifecycle component
        bundleContext.addFrameworkListener(PlatformLifecycle.getInstance());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                PlatformLifecycle.getInstance().onPlatformShutdown();
            }
        }, PlatformLifecycle.class.getCanonicalName() + ".onPlatformShutdown"));

        PlatformLocator locator = new PlatformLocatorImpl(bundleContext.getBundle().getLocation());
        bundleContext.registerService(Service.class, locator, null);
        bundleContext.registerService(PlatformLocator.class, locator, null);

        bundleContext.registerService(PlatformApiFactory.class, new PlatformApiFactoryForKernel(), null);

        bundleContext.registerService(ArtifactSchema.class, ArtifactSchemaKernel.INSTANCE, null);
        bundleContext.registerService(ArtifactSchema.class, ArtifactSchemaRDFS.INSTANCE, null);
        bundleContext.registerService(ArtifactArchetype.class, ArtifactArchetypeSchema.INSTANCE, null);
        bundleContext.registerService(ArtifactArchetype.class, ArtifactArchetypeFree.INSTANCE, null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        PlatformLifecycle.getInstance().onPlatformShutdown();
    }
}
