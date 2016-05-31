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
import org.xowl.platform.services.webapp.impl.XOWLCoreModule;
import org.xowl.platform.services.webapp.impl.XOWLHttpContext;
import org.xowl.platform.services.webapp.impl.XOWLModuleDirectory;

/**
 * The activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    /**
     * The URI prefix for web connections
     */
    public static final String URI_WEB = "/web";
    /**
     * The root resource for the web app files
     */
    public static final String WEBAPP_RESOURCE_ROOT = "/org/xowl/platform/services/webapp";

    /**
     * The tracker of the HTTP service
     */
    private ServiceTracker httpTracker;
    /**
     * The tracker of web module services
     */
    private ServiceTracker moduleTracker;
    /**
     * The directory of modules
     */
    private XOWLModuleDirectory directory;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        directory = new XOWLModuleDirectory();
        directory.register(new XOWLCoreModule());
        httpTracker = new ServiceTracker<HttpService, HttpService>(bundleContext, HttpService.class, null) {
            public void removedService(ServiceReference reference, HttpService service) {
                try {
                    service.unregister(URI_WEB);
                } catch (IllegalArgumentException exception) {
                    // ignore this
                }
            }

            public HttpService addingService(ServiceReference reference) {
                HttpService httpService = (HttpService) bundleContext.getService(reference);
                try {
                    httpService.registerServlet(URI_WEB + "/modules/index.json", directory, null, null);
                    httpService.registerResources(URI_WEB, WEBAPP_RESOURCE_ROOT, new XOWLHttpContext(httpService, directory));
                } catch (Exception exception) {
                    Logging.getDefault().error(exception);
                }
                return httpService;
            }
        };
        httpTracker.open();

        moduleTracker = new ServiceTracker<WebModuleService, WebModuleService>(bundleContext, WebModuleService.class, null) {
            public void removedService(ServiceReference reference, WebModuleService service) {
                directory.unregister(service);
            }

            public WebModuleService addingService(ServiceReference reference) {
                WebModuleService moduleService = (WebModuleService) bundleContext.getService(reference);
                directory.register(moduleService);
                return moduleService;
            }
        };
        moduleTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        httpTracker.close();
        moduleTracker.close();
    }
}
