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
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Serializable;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.utils.logging.ConsoleLogger;
import org.xowl.infra.utils.logging.DispatchLogger;
import org.xowl.infra.utils.logging.FileLogger;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.LoggingService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformUserRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.statistics.Metric;
import org.xowl.platform.kernel.statistics.MetricValueScalar;

import java.io.File;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements the logging service for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLLoggingService extends DispatchLogger implements LoggingService {
    /**
     * Represents a message in a log
     */
    private static class Msg implements Serializable {
        /**
         * The content of the message
         */
        public final Object content;
        /**
         * The log level for the message
         */
        public final String level;
        /**
         * The date for the message
         */
        public final Date date;

        /**
         * Initializes this message
         *
         * @param content The content of the message
         * @param level   The log level for the message
         */
        public Msg(Object content, String level) {
            this.content = content;
            this.level = level;
            this.date = new Date();
        }

        @Override
        public String serializedString() {
            return serializedJSON();
        }

        @Override
        public String serializedJSON() {
            if (content instanceof Throwable) {
                return "{\"level\": \"" +
                        IOUtils.escapeStringJSON(level) +
                        "\", \"date\": \"" +
                        IOUtils.escapeStringJSON(DateFormat.getDateTimeInstance().format(date)) +
                        "\", \"content\": \"" +
                        IOUtils.escapeStringJSON(((Throwable) content).getMessage()) +
                        "\"}";
            } else if (content instanceof Serializable) {
                return "{\"level\": \"" +
                        IOUtils.escapeStringJSON(level) +
                        "\", \"date\": \"" +
                        IOUtils.escapeStringJSON(DateFormat.getDateTimeInstance().format(date)) +
                        "\", \"content\": " +
                        ((Serializable) content).serializedJSON() +
                        "}";
            }
            return "{\"level\": \"" +
                    IOUtils.escapeStringJSON(level) +
                    "\", \"date\": \"" +
                    IOUtils.escapeStringJSON(DateFormat.getDateTimeInstance().format(date)) +
                    "\", \"content\": \"" +
                    IOUtils.escapeStringJSON(content.toString()) +
                    "\"}";
        }
    }

    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/admin/log"
    };
    /**
     * The size of the buffer for the last messages
     */
    private static final int BUFFER_SIZE = 128;

    /**
     * The buffer of the last messages
     */
    private final Msg[] messages;
    /**
     * The head of the buffer of messages
     */
    private final AtomicInteger head;
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
    public XOWLLoggingService() {
        super(new FileLogger(new File(System.getenv(Env.ROOT), "platform.log")), new ConsoleLogger());
        this.messages = new Msg[BUFFER_SIZE];
        this.head = new AtomicInteger(-1);
        this.errorsCount = new AtomicInteger(0);
        this.totalMessages = new AtomicInteger(0);
    }

    @Override
    public String getIdentifier() {
        return XOWLLoggingService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Logging Service";
    }

    @Override
    public Collection<Metric> getMetrics() {
        return Arrays.asList(METRIC_TOTAL_MESSAGES, METRIC_ERRORS_COUNT);
    }

    @Override
    public Serializable update(Metric metric) {
        if (metric == METRIC_TOTAL_MESSAGES)
            return new MetricValueScalar<>(totalMessages.get());
        if (metric == METRIC_ERRORS_COUNT)
            return new MetricValueScalar<>(errorsCount.get());
        return null;
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformUserRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        return XSPReplyUtils.toHttpResponse(new XSPReplyResultCollection<>(getMessages()), null);
    }

    @Override
    public void debug(Object message) {
        onLogMessage("DEBUG", message, false);
        super.debug(message);
    }

    @Override
    public void info(Object message) {
        onLogMessage("INFO", message, false);
        super.info(message);
    }

    @Override
    public void warning(Object message) {
        onLogMessage("WARNING", message, false);
        super.warning(message);
    }

    @Override
    public void error(Object message) {
        onLogMessage("ERROR", message, true);
        super.error(message);
    }

    /**
     * When a message is received
     *
     * @param level   The log level for the message
     * @param content The message's content
     * @param isError Whether this is an error message
     */
    private void onLogMessage(String level, Object content, boolean isError) {
        Msg message = new Msg(content, level);
        while (true) {
            int headValue = head.get();
            int insertion = headValue + 1;
            if (insertion >= BUFFER_SIZE)
                insertion = 0;
            if (head.compareAndSet(headValue, insertion)) {
                messages[insertion] = message;
                break;
            }
        }
        totalMessages.incrementAndGet();
        if (isError)
            errorsCount.incrementAndGet();
    }

    /**
     * Gets the current messages
     *
     * @return The current messages
     */
    private List<Msg> getMessages() {
        int current = head.get();
        if (current == -1)
            return Collections.emptyList();
        List<Msg> result = new ArrayList<>();
        for (int i = 0; i != BUFFER_SIZE; i++) {
            if (messages[current] == null)
                return result;
            result.add(messages[current]);
            current--;
            if (current == -1)
                current = BUFFER_SIZE - 1;
        }
        return result;
    }
}
