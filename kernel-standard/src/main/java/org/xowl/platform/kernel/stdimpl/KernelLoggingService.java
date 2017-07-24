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

package org.xowl.platform.kernel.stdimpl;

import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.api.ReplyUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.ConsoleLogger;
import org.xowl.infra.utils.logging.DispatchLogger;
import org.xowl.infra.utils.logging.FileLogger;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.infra.utils.metrics.MetricSnapshotInt;
import org.xowl.platform.kernel.LoggingService;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.events.Event;
import org.xowl.platform.kernel.platform.PlatformLogBuffer;
import org.xowl.platform.kernel.platform.PlatformLogMessage;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements the logging service for the platform
 *
 * @author Laurent Wouters
 */
public class KernelLoggingService extends DispatchLogger implements LoggingService, HttpApiService {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(KernelLoggingService.class, "/org/xowl/platform/kernel/stdimpl/api_log.raml", "Logging Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(KernelLoggingService.class, "/org/xowl/platform/kernel/stdimpl/api_log.html", "Logging Service - Documentation", HttpApiResource.MIME_HTML);


    /**
     * The size of the buffer for the last messages
     */
    private static final int BUFFER_SIZE = 128;

    /**
     * The URI for the API services
     */
    private final String apiUri;
    /**
     * The buffer of the last messages
     */
    private final PlatformLogBuffer buffer;
    /**
     * The number of errors since the platform started
     */
    private final AtomicInteger errorsCount;
    /**
     * The total number of messages
     */
    private final AtomicInteger totalMessages;

    /**
     * Initializes this service
     */
    public KernelLoggingService() {
        super(new FileLogger(PlatformUtils.resolve("platform.log")), new ConsoleLogger());
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/kernel/log";
        this.buffer = new PlatformLogBuffer(BUFFER_SIZE);
        this.errorsCount = new AtomicInteger(0);
        this.totalMessages = new AtomicInteger(0);
    }

    @Override
    public String getIdentifier() {
        return KernelLoggingService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Logging Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public Collection<Metric> getMetrics() {
        return Arrays.asList(METRIC_TOTAL_MESSAGES, METRIC_ERRORS_COUNT);
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        if (metric == METRIC_TOTAL_MESSAGES)
            return new MetricSnapshotInt(totalMessages.get());
        if (metric == METRIC_ERRORS_COUNT)
            return new MetricSnapshotInt(errorsCount.get());
        return null;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public boolean requireAuth(HttpApiRequest request) {
        return true;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        Reply reply = securityService.checkAction(ACTION_GET_LOG);
        if (!reply.isSuccess())
            return ReplyUtils.toHttpResponse(reply, null);

        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");

        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (PlatformLogMessage message : buffer.getMessages()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(message.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
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

    @Override
    public void debug(Object message) {
        if (message instanceof Event)
            message = ((Event) message).getDescription();
        buffer.debug(message);
        super.debug(message);
    }

    @Override
    public void info(Object message) {
        if (message instanceof Event)
            message = ((Event) message).getDescription();
        buffer.info(message);
        super.info(message);
    }

    @Override
    public void warning(Object message) {
        if (message instanceof Event)
            message = ((Event) message).getDescription();
        buffer.warning(message);
        super.warning(message);
    }

    @Override
    public void error(Object message) {
        if (message instanceof Event)
            message = ((Event) message).getDescription();
        buffer.error(message);
        super.error(message);
    }
}
