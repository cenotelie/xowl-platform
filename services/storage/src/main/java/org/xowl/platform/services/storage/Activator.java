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

package org.xowl.platform.services.storage;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.RegisterWaiter;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.storage.impl.XOWLStorageService;
import org.xowl.platform.services.storage.jobs.StorageJobFactory;

/**
 * Activator for the triple store service
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    /**
     * The store service registered by this bundle
     */
    private XOWLStorageService storeService;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Register.waitFor(PlatformHttp.class, new RegisterWaiter<PlatformHttp>() {
            @Override
            public void onAvailable(BundleContext bundleContext, PlatformHttp component) {
                storeService = new XOWLStorageService();
                bundleContext.registerService(Service.class, storeService, null);
                bundleContext.registerService(SecuredService.class, storeService, null);
                bundleContext.registerService(HttpApiService.class, storeService, null);
                bundleContext.registerService(MeasurableService.class, storeService, null);
                bundleContext.registerService(StorageService.class, storeService, null);
                bundleContext.registerService(ArtifactStorageService.class, storeService, null);

                bundleContext.registerService(JobFactory.class, new StorageJobFactory(), null);
            }
        }, bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (storeService != null)
            storeService.close();
    }
}
