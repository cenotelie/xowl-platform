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

package org.xowl.platform.kernel;

import fr.cenotelie.commons.utils.collections.Couple;
import fr.cenotelie.commons.utils.logging.Logger;
import fr.cenotelie.commons.utils.metrics.Metric;
import fr.cenotelie.commons.utils.metrics.MetricBase;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.statistics.MeasurableService;

/**
 * Represents the logging service for the platform
 *
 * @author Laurent Wouters
 */
public interface LoggingService extends SecuredService, MeasurableService, Logger {
    /**
     * The metric for the number of errors
     */
    Metric METRIC_ERRORS_COUNT = new MetricBase(LoggingService.class.getCanonicalName() + ".ErrorsCount",
            "Logging Service - Errors count",
            "errors",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));
    /**
     * The metric for the total number of messages
     */
    Metric METRIC_TOTAL_MESSAGES = new MetricBase(LoggingService.class.getCanonicalName() + ".TotalMessages",
            "Logging Service - Total messages",
            "messages",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));

    /**
     * Service action to get the current log events
     */
    SecuredAction ACTION_GET_LOG = new SecuredAction(LoggingService.class.getCanonicalName() + ".GetLog", "Logging Service - Get Log");

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_GET_LOG
    };
}
