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
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.platform.OSGiBundle;
import org.xowl.platform.kernel.platform.OSGiImplementation;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.statistics.Metric;
import org.xowl.platform.kernel.statistics.MetricValueScalar;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements the platform management service
 *
 * @author Laurent Wouters
 */
public class XOWLPlatformManagementService implements PlatformManagementService {
    /**
     * The URIs for this service
     */
    private static final String[] URIS = new String[]{
            "services/admin/platform",
            "services/admin/platform/bundles"
    };

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
    public Serializable update(Metric metric) {
        if (metric == METRIC_USED_MEMORY)
            return new MetricValueScalar<>(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        if (metric == METRIC_FREE_MEMORY)
            return new MetricValueScalar<>(Runtime.getRuntime().freeMemory());
        if (metric == METRIC_TOTAL_MEMORY)
            return new MetricValueScalar<>(Runtime.getRuntime().totalMemory());
        if (metric == METRIC_MAX_MEMORY)
            return new MetricValueScalar<>(Runtime.getRuntime().maxMemory());
        return null;
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIS);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        if (uri.equals("services/admin/platform/bundles")) {
            return XSPReplyUtils.toHttpResponse(new XSPReplyResultCollection<>(getPlatformBundles()), null);
        }

        String[] actions = parameters.get("action");
        if (actions == null || actions.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyResult<>(getOSGiImplementation()), null);

        switch (actions[0]) {
            case "shutdown":
                return XSPReplyUtils.toHttpResponse(shutdown(), null);
            case "restart":
                return XSPReplyUtils.toHttpResponse(restart(), null);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
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
