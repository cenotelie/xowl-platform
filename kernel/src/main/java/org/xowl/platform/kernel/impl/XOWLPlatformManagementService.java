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

package org.xowl.platform.kernel.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.infra.utils.metrics.MetricSnapshotLong;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.platform.OSGiBundle;
import org.xowl.platform.kernel.platform.OSGiImplementation;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements the platform management service
 *
 * @author Laurent Wouters
 */
public class XOWLPlatformManagementService implements PlatformManagementService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/kernel/platform";

    /**
     * The details of the OSGi implementation we are running on
     */
    private final OSGiImpl osgiImpl;
    /**
     * The cache of bundles
     */
    private final List<OSGiBundle> bundles;

    /**
     * Initializes this service
     *
     * @param configurationService The current configuration service
     * @param executionService     The current execution service
     */
    public XOWLPlatformManagementService(ConfigurationService configurationService, JobExecutionService executionService) {
        bundles = new ArrayList<>();
        osgiImpl = getCurrentOSGIImpl();
        osgiImpl.enforceHttpConfig(configurationService.getConfigFor(this), executionService);
    }

    /**
     * Gets the current OSGi implementation we are running on
     *
     * @return The current OSGi implementation
     */
    private static OSGiImpl getCurrentOSGIImpl() {
        return new OSGiImplFelix();
    }

    @Override
    public String getIdentifier() {
        return XOWLPlatformManagementService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Platform Management Service";
    }

    @Override
    public Collection<Metric> getMetrics() {
        return Arrays.asList(METRIC_USED_MEMORY, METRIC_FREE_MEMORY, METRIC_TOTAL_MEMORY, METRIC_MAX_MEMORY);
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        if (metric == METRIC_USED_MEMORY)
            return new MetricSnapshotLong(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        if (metric == METRIC_FREE_MEMORY)
            return new MetricSnapshotLong(Runtime.getRuntime().freeMemory());
        if (metric == METRIC_TOTAL_MEMORY)
            return new MetricSnapshotLong(Runtime.getRuntime().totalMemory());
        if (metric == METRIC_MAX_MEMORY)
            return new MetricSnapshotLong(Runtime.getRuntime().maxMemory());
        return null;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        if (request.getUri().equals(URI_API)) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, getOSGiImplementation().serializedJSON());
        } else if (request.getUri().equals(URI_API + "/shutdown")) {
            if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
            return XSPReplyUtils.toHttpResponse(shutdown(), null);
        } else if (request.getUri().equals(URI_API + "/restart")) {
            if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
            return XSPReplyUtils.toHttpResponse(restart(), null);
        } else if (request.getUri().equals(URI_API + "/bundles")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (OSGiBundle bundle : getPlatformBundles()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(bundle.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Override
    public OSGiImplementation getOSGiImplementation() {
        return osgiImpl;
    }

    @Override
    public Collection<OSGiBundle> getPlatformBundles() {
        if (bundles.isEmpty()) {
            Bundle[] bundles = FrameworkUtil.getBundle(OSGiBundle.class).getBundleContext().getBundles();
            for (int i = 0; i != bundles.length; i++) {
                this.bundles.add(new OSGiBundle(bundles[i]));
            }
        }
        return Collections.unmodifiableCollection(bundles);
    }

    @Override
    public XSPReply shutdown() {
        System.exit(PLATFORM_EXIT_NORMAL);
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply restart() {
        System.exit(PLATFORM_EXIT_RESTART);
        return XSPReplySuccess.instance();
    }
}
