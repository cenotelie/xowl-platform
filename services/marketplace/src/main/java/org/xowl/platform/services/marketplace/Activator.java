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

package org.xowl.platform.services.marketplace;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.RegisterWaiter;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.marketplace.impl.XOWLMarketplaceProvider;
import org.xowl.platform.services.marketplace.impl.XOWLMarketplaceService;
import org.xowl.platform.services.marketplace.jobs.MarketplaceJobFactory;

/**
 * Activator for the server service bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        Register.waitFor(PlatformHttp.class, new RegisterWaiter<PlatformHttp>() {
            @Override
            public void onAvailable(BundleContext bundleContext, PlatformHttp component) {
                XOWLMarketplaceService marketplaceService = new XOWLMarketplaceService();
                bundleContext.registerService(Service.class, marketplaceService, null);
                bundleContext.registerService(SecuredService.class, marketplaceService, null);
                bundleContext.registerService(HttpApiService.class, marketplaceService, null);
                bundleContext.registerService(MarketplaceService.class, marketplaceService, null);

                bundleContext.registerService(MarketplaceProvider.class, new XOWLMarketplaceProvider(), null);
                bundleContext.registerService(JobFactory.class, new MarketplaceJobFactory(), null);
            }
        }, bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
