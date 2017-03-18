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

package org.xowl.platform.services.connection;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.remote.DeserializerFactory;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.connection.impl.XOWLConnectionService;
import org.xowl.platform.services.connection.jobs.ConnectorJobFactory;

/**
 * Activator for the domain bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        bundleContext.registerService(DeserializerFactory.class, new DeserializerFactoryForConnection(), null);

        Register.waitFor(PlatformHttp.class, new RegisterWaiter<PlatformHttp>() {
            @Override
            public void onAvailable(BundleContext bundleContext, PlatformHttp component) {
                XOWLConnectionService connectionService = new XOWLConnectionService();
                bundleContext.registerService(Service.class, connectionService, null);
                bundleContext.registerService(SecuredService.class, connectionService, null);
                bundleContext.registerService(HttpApiService.class, connectionService, null);
                bundleContext.registerService(ManagedService.class, connectionService, null);
                bundleContext.registerService(ConnectionService.class, connectionService, null);

                bundleContext.registerService(JobFactory.class, new ConnectorJobFactory(), null);
            }
        }, bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }
}
