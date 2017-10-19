/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.kernel.platform;

import fr.cenotelie.commons.utils.Serializable;
import fr.cenotelie.commons.utils.TextUtils;

import java.text.DateFormat;
import java.util.Date;

/**
 * Represents a message in a log for the platform
 *
 * @author Laurent Wouters
 */
public class PlatformLogMessage implements Serializable {
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
    public PlatformLogMessage(Object content, String level) {
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
            return "{\"type\": \"" +
                    TextUtils.escapeStringJSON(PlatformLogMessage.class.getCanonicalName()) +
                    "\", \"level\": \"" +
                    TextUtils.escapeStringJSON(level) +
                    "\", \"date\": \"" +
                    TextUtils.escapeStringJSON(DateFormat.getDateTimeInstance().format(date)) +
                    "\", \"content\": \"" +
                    TextUtils.escapeStringJSON(((Throwable) content).getClass().getCanonicalName()) +
                    "\"}";
        } else if (content instanceof Serializable) {
            return "{\"type\": \"" +
                    TextUtils.escapeStringJSON(PlatformLogMessage.class.getCanonicalName()) +
                    "\", \"level\": \"" +
                    TextUtils.escapeStringJSON(level) +
                    "\", \"date\": \"" +
                    TextUtils.escapeStringJSON(DateFormat.getDateTimeInstance().format(date)) +
                    "\", \"content\": " +
                    ((Serializable) content).serializedJSON() +
                    "}";
        }
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(PlatformLogMessage.class.getCanonicalName()) +
                "\", \"level\": \"" +
                TextUtils.escapeStringJSON(level) +
                "\", \"date\": \"" +
                TextUtils.escapeStringJSON(DateFormat.getDateTimeInstance().format(date)) +
                "\", \"content\": \"" +
                TextUtils.escapeStringJSON(content.toString()) +
                "\"}";
    }
}
