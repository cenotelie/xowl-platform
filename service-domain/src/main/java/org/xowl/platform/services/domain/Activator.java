/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.domain;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.kernel.JobFactory;
import org.xowl.platform.services.domain.impl.XOWLDomainDirectoryService;
import org.xowl.platform.services.domain.impl.XOWLGenericConnectorFactory;
import org.xowl.platform.services.domain.jobs.DomainJobFactory;

/**
 * Activator for the domain bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    /**
     * The directory service
     */
    private XOWLDomainDirectoryService directory;
    /**
     * The tracker of the connector factories
     */
    private ServiceTracker factoryTracker;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        directory = new XOWLDomainDirectoryService();
        bundleContext.registerService(HttpAPIService.class, directory, null);
        bundleContext.registerService(DomainDirectoryService.class, directory, null);

        factoryTracker = new ServiceTracker<DomainConnectorFactory, DomainConnectorFactory>(bundleContext, DomainConnectorFactory.class, null) {
            @Override
            public void removedService(ServiceReference reference, DomainConnectorFactory service) {
                directory.onFactoryOffline(service);
            }

            @Override
            public DomainConnectorFactory addingService(ServiceReference reference) {
                DomainConnectorFactory factory = (DomainConnectorFactory) bundleContext.getService(reference);
                directory.onFactoryOnline(factory);
                return factory;
            }
        };
        factoryTracker.open();

        bundleContext.registerService(DomainConnectorFactory.class, new XOWLGenericConnectorFactory(), null);
        bundleContext.registerService(JobFactory.class, new DomainJobFactory(), null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        factoryTracker.close();
    }
}
