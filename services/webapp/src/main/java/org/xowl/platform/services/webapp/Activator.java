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

package org.xowl.platform.services.webapp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.ui.WebUIContribution;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.webapp.impl.*;

/**
 * The activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    /**
     * The tracker of the HTTP service
     */
    private ServiceTracker httpTracker;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        final ContributionDirectory contributionDirectory = new XOWLContributionDirectory();
        bundleContext.registerService(Service.class, contributionDirectory, null);
        bundleContext.registerService(ContributionDirectory.class, contributionDirectory, null);

        XOWLWebModuleDirectory moduleDirectory = new XOWLWebModuleDirectory();
        bundleContext.registerService(Service.class, moduleDirectory, null);
        bundleContext.registerService(HttpApiService.class, moduleDirectory, null);

        bundleContext.registerService(WebUIContribution.class, new XOWLMainContribution(), null);
        bundleContext.registerService(WebModule.class, new XOWLWebModuleCore(), null);
        bundleContext.registerService(WebModule.class, new XOWLWebModuleCollaboration(), null);
        bundleContext.registerService(WebModule.class, new XOWLWebModuleAdmin(), null);

        httpTracker = new ServiceTracker<HttpService, HttpService>(bundleContext, HttpService.class, null) {
            public void removedService(ServiceReference reference, HttpService service) {
                try {
                    service.unregister(PlatformHttp.getUriPrefixWeb());
                } catch (IllegalArgumentException exception) {
                    // ignore this
                }
            }

            public HttpService addingService(ServiceReference reference) {
                HttpService httpService = (HttpService) bundleContext.getService(reference);
                try {
                    httpService.registerResources(PlatformHttp.getUriPrefixWeb(), PlatformHttp.getUriPrefixWeb(), new XOWLHttpContext(httpService, contributionDirectory));
                } catch (Exception exception) {
                    Logging.getDefault().error(exception);
                }
                return httpService;
            }
        };
        httpTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        httpTracker.close();
    }
}
