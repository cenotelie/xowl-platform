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

package org.xowl.platform.connectors.csv;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.RegisterWaiter;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.ui.WebUIContribution;
import org.xowl.platform.services.connection.ConnectorDescriptor;
import org.xowl.platform.services.connection.ConnectorServiceFactory;
import org.xowl.platform.services.importation.Importer;

/**
 * The activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        Register.waitFor(PlatformHttp.class, new RegisterWaiter<PlatformHttp>() {
            @Override
            public void onAvailable(PlatformHttp component) {
                bundleContext.registerService(Importer.class, new CSVImporter(), null);
                bundleContext.registerService(JobFactory.class, new CSVimportationJobFactory(), null);
                bundleContext.registerService(ConnectorDescriptor.class, CSVConnectorDescriptor.INSTANCE, null);
                bundleContext.registerService(ConnectorServiceFactory.class, CSVConnectorFactory.INSTANCE, null);
                bundleContext.registerService(WebUIContribution.class, new CSVUIContribution(), null);
            }
        });
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
