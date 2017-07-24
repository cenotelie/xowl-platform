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

import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.api.Reply;

/**
 * An XSP reply when a required service is not available
 *
 * @author Laurent Wouters
 */
public class ReplyServiceUnavailable implements Reply {
    /**
     * The singleton instance
     */
    private static ReplyServiceUnavailable INSTANCE = null;

    /**
     * Gets the singleton instance
     *
     * @return The singleton instance
     */
    public synchronized static ReplyServiceUnavailable instance() {
        if (INSTANCE == null)
            INSTANCE = new ReplyServiceUnavailable();
        return INSTANCE;
    }

    /**
     * Initializes this instance
     */
    private ReplyServiceUnavailable() {
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String getMessage() {
        return "SERVICE UNAVAILABLE";
    }

    @Override
    public String serializedString() {
        return "SERVICE UNAVAILABLE";
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(Reply.class.getCanonicalName()) +
                "\", \"kind\": \"" +
                TextUtils.escapeStringJSON(ReplyServiceUnavailable.class.getSimpleName()) +
                "\", \"isSuccess\": false," +
                "\"message\": \"SERVICE UNAVAILABLE\"}";
    }
}