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
import org.osgi.service.http.HttpService;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.RegisterWaiter;
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
     * The server
     */
    private XOWLMainHTTPServer server;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        server = new XOWLMainHTTPServer();
        bundleContext.registerService(Service.class, server, null);
        bundleContext.registerService(HTTPServerService.class, server, new Hashtable<String, Object>());
        bundleContext.registerService(WebUIContribution.class, new XOWLHttpApiDocumentationModule(), null);

        Register.waitFor(PlatformHttp.class, new RegisterWaiter<PlatformHttp>() {
            @Override
            public void onAvailable(BundleContext bundleContext, PlatformHttp component) {
                Register.waitFor(HttpService.class, new RegisterWaiter<HttpService>() {
                    @Override
                    public void onAvailable(BundleContext bundleContext, HttpService component) {
                        try {
                            component.registerServlet(PlatformHttp.getUriPrefixApi(), server, null, new XOWLMainHTTPContext(component));
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
