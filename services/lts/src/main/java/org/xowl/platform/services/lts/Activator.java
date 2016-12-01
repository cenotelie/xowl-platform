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

package org.xowl.platform.services.lts;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.HttpApiService;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.services.lts.impl.XOWLStoreService;
import org.xowl.platform.services.lts.jobs.StorageJobFactory;

/**
 * Activator for the triple store service
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    /**
     * The store service registered by this bundle
     */
    private XOWLStoreService storeService;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        storeService = new XOWLStoreService();
        bundleContext.registerService(TripleStoreService.class, storeService, null);
        bundleContext.registerService(ArtifactStorageService.class, storeService, null);
        bundleContext.registerService(HttpApiService.class, storeService, null);
        bundleContext.registerService(MeasurableService.class, storeService, null);

        bundleContext.registerService(JobFactory.class, new StorageJobFactory(), null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (storeService != null)
            storeService.close();
    }
}
