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

import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.platform.OSGiImplementation;

/**
 * Represents a specific OSGi implementation the platform is running on
 *
 * @author Laurent Wouters
 */
abstract class OSGiImpl implements OSGiImplementation {
    /**
     * Enforces the HTTP/HTTPS configuration specified in the platform's configuration
     *
     * @param configuration    The platform's configuration
     * @param executionService The current execution service
     */
    public abstract void enforceHttpConfig(Configuration configuration, JobExecutionService executionService);

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \""
                + TextUtils.escapeStringJSON(OSGiImplementation.class.getCanonicalName())
                + "\", \"identifier\": \""
                + TextUtils.escapeStringJSON(getIdentifier())
                + "\", \"name\":\""
                + TextUtils.escapeStringJSON(getName())
                + "\"}";
    }
}
