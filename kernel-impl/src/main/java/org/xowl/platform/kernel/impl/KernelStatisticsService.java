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
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.security.SecuredAction;
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
public class KernelStatisticsService implements StatisticsService, HttpApiService {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(KernelStatisticsService.class, "/org/xowl/platform/kernel/impl/api_statistics.raml", "Statistics Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(KernelStatisticsService.class, "/org/xowl/platform/kernel/impl/api_statistics.html", "Statistics Service - Documentation", HttpApiResource.MIME_HTML);


    /**
     * The URI for the API services
     */
    private static final String apiUri = PlatformHttp.getUriPrefixApi() + "/kernel/statistics";

    /**
     * Initializes this provider
     */
    public KernelStatisticsService() {
    }

    @Override
    public String getIdentifier() {
        return KernelStatisticsService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Statistics Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");

        if (request.getUri().equals(apiUri + "/metrics"))
            return onMessageGetMetricList(securityService);

        if (request.getUri().startsWith(apiUri + "/metrics")) {
            String rest = request.getUri().substring(apiUri.length() + "/metrics".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String metricId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0)
                return onMessageGetMetric(securityService, metricId);
            else if (rest.substring(index).equals("/snapshot"))
                return onMessageGetMetricValue(securityService, metricId);
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
     * @param securityService The current security service
     * @return The metrics
     */
    private HttpResponse onMessageGetMetricList(SecurityService securityService) {
        XSPReply reply = securityService.checkAction(ACTION_GET_METRICS);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

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
     * @param securityService The current security service
     * @param identifier      The identifier of the requested metric
     * @return The metrics
     */
    private HttpResponse onMessageGetMetric(SecurityService securityService, String identifier) {
        XSPReply reply = securityService.checkAction(ACTION_GET_METRICS);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        for (MetricProvider provider : Register.getComponents(MeasurableService.class)) {
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
     * @param securityService The current security service
     * @param identifier      The requested metric
     * @return The metrics' values
     */
    private HttpResponse onMessageGetMetricValue(SecurityService securityService, String identifier) {
        XSPReply reply = securityService.checkAction(ACTION_POLL);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        MetricSnapshot value = pollMetric(identifier);
        if (value == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, value.serializedJSON());
    }

    @Override
    public Collection<Metric> getMetrics() {
        Collection<Metric> result = new ArrayList<>();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return result;
        if (!securityService.checkAction(ACTION_GET_METRICS).isSuccess())
            return result;

        for (MetricProvider provider : Register.getComponents(MeasurableService.class)) {
            result.addAll(provider.getMetrics());
        }
        return result;
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        if (!securityService.checkAction(ACTION_GET_METRICS).isSuccess())
            return null;

        for (MetricProvider provider : Register.getComponents(MeasurableService.class)) {
            MetricSnapshot result = provider.pollMetric(metric);
            if (result != null)
                return result;
        }
        return null;
    }

    @Override
    public MetricSnapshot pollMetric(String metricId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        if (!securityService.checkAction(ACTION_GET_METRICS).isSuccess())
            return null;

        for (MetricProvider provider : Register.getComponents(MeasurableService.class)) {
            for (Metric metric : provider.getMetrics()) {
                if (metric.getIdentifier().equals(metricId))
                    return provider.pollMetric(metric);
            }
        }
        return null;
    }
}
