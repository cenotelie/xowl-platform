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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.statistics.Metric;
import org.xowl.platform.kernel.statistics.MetricProvider;
import org.xowl.platform.kernel.statistics.StatisticsService;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The provider of statistics services
 *
 * @author Laurent Wouters
 */
public class XOWLStatisticsService implements StatisticsService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/admin/statistics"
    };

    /**
     * The known metrics
     */
    private final Map<String, Metric> metrics;
    /**
     * The metric providers
     */
    private final Map<String, MetricProvider> providers;

    /**
     * Initializes this provider
     */
    public XOWLStatisticsService() {
        this.metrics = new HashMap<>();
        this.providers = new HashMap<>();
    }

    @Override
    public String getIdentifier() {
        return XOWLStatisticsService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Statistics Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return onMessageGetStatList();
        return onMessageGetStatValues(ids);
    }

    /**
     * Responds to a request for the list of available statistics
     *
     * @return The statistics
     */
    private HttpResponse onMessageGetStatList() {
        // get all the metrics
        boolean first = true;
        StringBuilder builder = new StringBuilder("[");
        for (Metric metric : metrics.values()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(metric.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for the values of statistics
     *
     * @param ids The requested statistics
     * @return The statistics' values
     */
    private HttpResponse onMessageGetStatValues(String[] ids) {
        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        boolean first = true;
        StringBuilder builder = new StringBuilder("{");
        for (int i = 0; i != ids.length; i++) {
            Serializable value = update(ids[i]);
            if (value == null)
                continue;
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(ids[i]));
            builder.append("\": ");
            builder.append(value.serializedJSON());
        }
        builder.append("}");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    @Override
    public void registerProvider(MetricProvider provider) {
        for (Metric metric : provider.getMetrics()) {
            metrics.put(metric.getIdentifier(), metric);
            providers.put(metric.getIdentifier(), provider);
        }
    }

    @Override
    public void unregisterProvider(MetricProvider provider) {
        for (Metric metric : provider.getMetrics()) {
            metrics.remove(metric.getIdentifier());
            providers.remove(metric.getIdentifier());
        }
    }

    @Override
    public Collection<Metric> getMetrics() {
        return metrics.values();
    }

    @Override
    public Serializable update(Metric metric) {
        MetricProvider provider = providers.get(metric.getIdentifier());
        if (provider == null)
            return null;
        return provider.update(metric);
    }

    @Override
    public Serializable update(String metricId) {
        Metric metric = metrics.get(metricId);
        return metric == null ? null : update(metric);
    }
}
