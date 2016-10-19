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

import org.xowl.infra.utils.logging.Logger;
import org.xowl.platform.kernel.statistics.Metric;
import org.xowl.platform.kernel.statistics.MetricBase;
import org.xowl.platform.kernel.statistics.MetricProvider;

/**
 * Represents the logging service for the platform
 *
 * @author Laurent Wouters
 */
public interface LoggingService extends Service, HttpAPIService, MetricProvider, Logger {
    /**
     * The metric for the number of errors
     */
    Metric METRIC_ERRORS_COUNT = new MetricBase(LoggingService.class.getCanonicalName() + ".ErrorsCount", "Logging Service - Errors count");
    /**
     * The metric for the total number of messages
     */
    Metric METRIC_TOTAL_MESSAGES = new MetricBase(LoggingService.class.getCanonicalName() + ".TotalMessages", "Logging Service - Total messages");
}
