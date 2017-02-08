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

package org.xowl.platform.services.httpapi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.ui.WebUIContribution;
import org.xowl.platform.services.httpapi.impl.XOWLHttpApiDocumentationModule;
import org.xowl.platform.services.httpapi.impl.XOWLMainHTTPContext;
import org.xowl.platform.services.httpapi.impl.XOWLMainHTTPServer;

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
    private XOWLMainHTTPServer server;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        httpTracker = new ServiceTracker<HttpService, HttpService>(bundleContext, HttpService.class, null) {
            public void removedService(ServiceReference reference, HttpService service) {
                try {
                    service.unregister(PlatformHttp.getUriPrefixApi());
                } catch (IllegalArgumentException exception) {
                    // ignore this
                }
            }

            public HttpService addingService(ServiceReference reference) {
                HttpService httpService = (HttpService) bundleContext.getService(reference);
                try {
                    httpService.registerServlet(PlatformHttp.getUriPrefixApi(), server, null, new XOWLMainHTTPContext(httpService));
                } catch (Exception exception) {
                    Logging.getDefault().error(exception);
                }
                return httpService;
            }
        };
        server = new XOWLMainHTTPServer();
        httpTracker.open();
        bundleContext.registerService(Service.class, server, null);
        bundleContext.registerService(HTTPServerService.class, server, new Hashtable<String, Object>());
        bundleContext.registerService(WebUIContribution.class, new XOWLHttpApiDocumentationModule(), null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        httpTracker.close();
    }
}
