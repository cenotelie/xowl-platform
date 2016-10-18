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

import org.xowl.infra.store.Serializable;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
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
        if (ids == null || ids.length == 0) {
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
        Serializable value = update(ids[0]);
        if (value == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, value.serializedJSON());
    }

    @Override
    public void registerProvider(MetricProvider provider) {
        for (Metric metric : provider.getMetrics()) {
            metrics.put(metric.getIdentifier(), metric);
            providers.put(metric.getIdentifier(), provider);
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
