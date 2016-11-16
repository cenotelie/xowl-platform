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
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricProvider;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.kernel.statistics.StatisticsService;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
     * Initializes this provider
     */
    public XOWLStatisticsService() {
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
        String[] polls = parameters.get("poll");
        if (polls != null && polls.length > 0)
            onMessageGetMetricValues(polls);
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return onMessageGetMetricList();
        return onMessageGetMetric(ids[0]);
    }

    /**
     * Responds to a request for the list of available metrics
     *
     * @return The metrics
     */
    private HttpResponse onMessageGetMetricList() {
        // get all the metrics
        boolean first = true;
        StringBuilder builder = new StringBuilder("[");
        for (Metric metric : getMetrics()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(metric.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for the definition of a metric
     *
     * @param identifier The identifier of the requested metric
     * @return The metrics
     */
    private HttpResponse onMessageGetMetric(String identifier) {
        for (MetricProvider provider : ServiceUtils.getServices(MeasurableService.class)) {
            for (Metric metric : provider.getMetrics()) {
                if (metric.getIdentifier().equals(identifier))
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, metric.serializedJSON());
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Responds to a request for the values of metrics
     *
     * @param ids The requested metrics
     * @return The metrics' values
     */
    private HttpResponse onMessageGetMetricValues(String[] ids) {
        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        boolean first = true;
        StringBuilder builder = new StringBuilder("[");
        for (MetricProvider provider : ServiceUtils.getServices(MeasurableService.class)) {
            for (Metric metric : provider.getMetrics()) {
                for (int i = 0; i != ids.length; i++) {
                    if (metric.getIdentifier().equals(ids[i])) {
                        Serializable value = provider.pollMetric(metric);
                        if (value == null)
                            continue;
                        if (!first)
                            builder.append(", ");
                        first = false;
                        builder.append("{\"metric\": ");
                        builder.append(metric.serializedJSON());
                        builder.append(", \"value\": ");
                        builder.append(value.serializedJSON());
                        builder.append("}");
                        break;
                    }
                }
            }
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    @Override
    public Collection<Metric> getMetrics() {
        Collection<Metric> result = new ArrayList<>();
        for (MetricProvider provider : ServiceUtils.getServices(MeasurableService.class)) {
            result.addAll(provider.getMetrics());
        }
        return result;
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        for (MetricProvider provider : ServiceUtils.getServices(MeasurableService.class)) {
            MetricSnapshot result = provider.pollMetric(metric);
            if (result != null)
                return result;
        }
        return null;
    }

    @Override
    public MetricSnapshot pollMetric(String metricId) {
        for (MetricProvider provider : ServiceUtils.getServices(MeasurableService.class)) {
            for (Metric metric : provider.getMetrics()) {
                if (metric.getIdentifier().equals(metricId))
                    return provider.pollMetric(metric);
            }
        }
        return null;
    }
}
