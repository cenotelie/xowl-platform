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

import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;

/**
 * Represents a rich string for a message with possible links to objects
 * This class is expected to be used to represented rich messages in a user interface.
 *
 * @author Laurent Wouters
 */
public class RichString implements Serializable {
    /**
     * The parts of this string
     */
    private final Object[] parts;

    /**
     * Initializes this string
     *
     * @param parts The parts of this string
     */
    public RichString(Object... parts) {
        this.parts = parts;
    }

    @Override
    public String serializedString() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i != parts.length; i++) {
            if (parts[i] instanceof Serializable)
                buffer.append(((Serializable) parts[i]).serializedString());
            else
                buffer.append(parts[i].toString());
        }
        return buffer.toString();
    }

    @Override
    public String serializedJSON() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{\"type\": \"");
        buffer.append(TextUtils.escapeStringJSON(RichString.class.getCanonicalName()));
        buffer.append("\", \"parts\": [");
        for (int i = 0; i != parts.length; i++) {
            if (i != 0)
                buffer.append(", ");
            if (parts[i] instanceof Serializable)
                buffer.append(((Serializable) parts[i]).serializedJSON());
            else {
                buffer.append("\"");
                buffer.append(TextUtils.escapeStringJSON(parts[i].toString()));
                buffer.append("\"");
            }
        }
        buffer.append("]}");
        return buffer.toString();
    }

    @Override
    public String toString() {
        return serializedString();
    }
}
