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

package org.xowl.platform.kernel.statistics;

import org.xowl.infra.store.IOUtils;

/**
 * Base implementation of a metric
 *
 * @author Laurent Wouters
 */
public class MetricBase implements Metric {
    /**
     * The metric's unique identifier
     */
    private final String identifier;
    /**
     * The metric's human readable name
     */
    private final String name;

    /**
     * Initializes this metric
     *
     * @param identifier The metric's unique identifier
     * @param name       The metric's human readable name
     */
    public MetricBase(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \""
                + IOUtils.escapeStringJSON(Metric.class.getCanonicalName())
                + "\", \"identifier\": \""
                + IOUtils.escapeStringJSON(identifier)
                + "\", \"name\":\""
                + IOUtils.escapeStringJSON(name)
                + "\"}";
    }
}
