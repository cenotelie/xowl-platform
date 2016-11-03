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

import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;

/**
 * Implements a scalar value for a metric
 *
 * @param <T> The type of the metric
 * @author Laurent Wouters
 */
public class MetricValueScalar<T> implements Serializable {
    /**
     * The encapsulated value
     */
    private final T value;

    /**
     * Initializes this value
     *
     * @param value The encapsulated value
     */
    public MetricValueScalar(T value) {
        this.value = value;
    }

    @Override
    public String serializedString() {
        return value.toString();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(MetricValueScalar.class.getCanonicalName()) +
                "\", \"value\": \"" +
                TextUtils.escapeStringJSON(value.toString()) +
                "\"}";
    }
}
