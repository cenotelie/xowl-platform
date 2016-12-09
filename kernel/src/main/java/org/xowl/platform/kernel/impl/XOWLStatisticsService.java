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
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricProvider;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.kernel.statistics.StatisticsService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The provider of statistics services
 *
 * @author Laurent Wouters
 */
public class XOWLStatisticsService implements StatisticsService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/kernel/statistics";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_statistics.raml", "Statistics Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_statistics.html", "Statistics Service - Documentation", HttpApiResource.MIME_HTML);


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
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");

        if (request.getUri().equals(URI_API + "/metrics"))
            return onMessageGetMetricList();

        if (request.getUri().startsWith(URI_API + "/metrics")) {
            String rest = request.getUri().substring(URI_API.length() + "/metrics".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String metricId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0)
                return onMessageGetMetric(metricId);
            else if (rest.substring(index).equals("/snapshot"))
                return onMessageGetMetricValue(metricId);
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Override
    public HttpApiResource getApiSpecification() {
        return RESOURCE_SPECIFICATION;
    }

    @Override
    public HttpApiResource getApiDocumentation() {
        return RESOURCE_DOCUMENTATION;
    }

    @Override
    public HttpApiResource[] getApiOtherResources() {
        return null;
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(HttpApiService.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"specification\": " +
                RESOURCE_SPECIFICATION.serializedJSON() +
                ", \"documentation\": " +
                RESOURCE_DOCUMENTATION.serializedJSON() +
                "}";
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
     * Responds to a request for the values of a metric
     *
     * @param identifier The requested metric
     * @return The metrics' values
     */
    private HttpResponse onMessageGetMetricValue(String identifier) {
        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        for (MetricProvider provider : ServiceUtils.getServices(MeasurableService.class)) {
            for (Metric metric : provider.getMetrics()) {
                if (metric.getIdentifier().equals(identifier)) {
                    MetricSnapshot value = provider.pollMetric(metric);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, value.serializedJSON());
                }
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
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
