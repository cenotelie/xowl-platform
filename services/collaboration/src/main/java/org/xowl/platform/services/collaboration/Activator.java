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

package org.xowl.platform.services.collaboration;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.RegisterWaiter;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.remote.DeserializerFactory;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.collaboration.impl.XOWLCollaborationService;
import org.xowl.platform.services.collaboration.impl.XOWLNetworkProvider;
import org.xowl.platform.services.collaboration.impl.XOWLPatternProvider;
import org.xowl.platform.services.collaboration.jobs.CollaborationJobFactory;

/**
 * Activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        Register.waitFor(PlatformHttp.class, new RegisterWaiter<PlatformHttp>() {
            @Override
            public void onAvailable(BundleContext bundleContext, PlatformHttp component) {
                bundleContext.registerService(DeserializerFactory.class, new DeserializerFactoryForCollaboration(), null);

                bundleContext.registerService(JobFactory.class, new CollaborationJobFactory(), null);

                XOWLCollaborationService collaborationService = new XOWLCollaborationService();
                bundleContext.registerService(Service.class, collaborationService, null);
                bundleContext.registerService(SecuredService.class, collaborationService, null);
                bundleContext.registerService(HttpApiService.class, collaborationService, null);
                bundleContext.registerService(CollaborationService.class, collaborationService, null);
                bundleContext.registerService(CollaborationLocalService.class, collaborationService, null);
                bundleContext.registerService(CollaborationNetworkService.class, collaborationService, null);

                XOWLNetworkProvider networkProvider = new XOWLNetworkProvider();
                bundleContext.registerService(CollaborationNetworkServiceProvider.class, networkProvider, null);

                XOWLPatternProvider patternProvider = new XOWLPatternProvider();
                bundleContext.registerService(CollaborationPatternProvider.class, patternProvider, null);
            }
        }, bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }
}
