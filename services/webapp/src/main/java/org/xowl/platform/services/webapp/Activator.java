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

package org.xowl.platform.services.webapp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.xowl.infra.utils.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * The activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator, WebModuleDirectory {
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
     * The current web module services
     */
    private Map<String, WebModuleService> moduleServices;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        moduleServices = new HashMap<>();
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
                    httpService.registerResources(URI_WEB, WEBAPP_RESOURCE_ROOT, new HttpDefaultContext(httpService, Activator.this));
                } catch (Exception exception) {
                    Logger.DEFAULT.error(exception);
                }
                return httpService;
            }
        };
        httpTracker.open();

        moduleTracker = new ServiceTracker<WebModuleService, WebModuleService>(bundleContext, WebModuleService.class, null) {
            public void removedService(ServiceReference reference, WebModuleService service) {
                moduleServices.remove(service.getURI());
            }

            public WebModuleService addingService(ServiceReference reference) {
                WebModuleService moduleService = (WebModuleService) bundleContext.getService(reference);
                moduleServices.put(moduleService.getURI(), moduleService);
                return moduleService;
            }
        };
        moduleTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        httpTracker.close();
        moduleTracker.close();
        moduleServices.clear();
    }

    @Override
    public WebModuleService getServiceFor(String uri) {
        return moduleServices.get(uri);
    }
}
