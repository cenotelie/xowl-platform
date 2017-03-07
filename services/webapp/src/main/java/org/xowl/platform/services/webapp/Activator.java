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
import org.osgi.service.http.HttpService;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.RegisterWaiter;
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
     * The directory for the UI contributions
     */
    private ContributionDirectory contributionDirectory;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        contributionDirectory = new XOWLContributionDirectory();
        bundleContext.registerService(Service.class, contributionDirectory, null);
        bundleContext.registerService(ContributionDirectory.class, contributionDirectory, null);

        Register.waitFor(PlatformHttp.class, new RegisterWaiter<PlatformHttp>() {
            @Override
            public void onAvailable(BundleContext bundleContext, PlatformHttp component) {
                XOWLWebModuleDirectory moduleDirectory = new XOWLWebModuleDirectory();
                bundleContext.registerService(Service.class, moduleDirectory, null);
                bundleContext.registerService(HttpApiService.class, moduleDirectory, null);

                bundleContext.registerService(WebUIContribution.class, new XOWLMainContribution(), null);
                bundleContext.registerService(WebModule.class, new XOWLWebModuleCore(), null);
                bundleContext.registerService(WebModule.class, new XOWLWebModuleCollaboration(), null);
                bundleContext.registerService(WebModule.class, new XOWLWebModuleAdmin(), null);

                Register.waitFor(HttpService.class, new RegisterWaiter<HttpService>() {
                    @Override
                    public void onAvailable(BundleContext bundleContext, HttpService component) {
                        try {
                            component.registerResources(PlatformHttp.getUriPrefixWeb(), PlatformHttp.getUriPrefixWeb(), new XOWLHttpContext(component, contributionDirectory));
                        } catch (Exception exception) {
                            Logging.get().error(exception);
                        }
                    }
                }, bundleContext);
            }
        }, bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }
}
