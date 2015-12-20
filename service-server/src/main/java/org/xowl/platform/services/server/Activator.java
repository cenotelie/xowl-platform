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

package org.xowl.platform.services.server;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.xowl.platform.services.server.impl.XOWLMainServer;
import org.xowl.utils.logging.Logger;

import java.util.Hashtable;

/**
 * Activator for the server service bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    /**
     * The tracker of the HTTP service
     */
    private ServiceTracker httpTracker;
    /**
     * The server
     */
    private XOWLMainServer server;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        httpTracker = new ServiceTracker<HttpService, HttpService>(bundleContext, HttpService.class, null) {
            public void removedService(ServiceReference reference, HttpService service) {
                try {
                    service.unregister("/");
                } catch (IllegalArgumentException exception) {
                    // ignore this
                }
            }

            public HttpService addingService(ServiceReference reference) {
                HttpService httpService = (HttpService) bundleContext.getService(reference);
                try {
                    httpService.registerServlet("/", server, null, null);
                } catch (Exception exception) {
                    Logger.DEFAULT.error(exception);
                }
                return httpService;
            }
        };
        server = new XOWLMainServer();
        httpTracker.open();
        bundleContext.registerService(ServerService.class, server, new Hashtable<String, Object>());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        httpTracker.close();
    }
}
