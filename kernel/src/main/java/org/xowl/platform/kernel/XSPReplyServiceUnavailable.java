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

import org.xowl.infra.server.xsp.XSPReplyFailure;

/**
 * An XSP reply when a required service is not available
 *
 * @author Laurent Wouters
 */
public class XSPReplyServiceUnavailable extends XSPReplyFailure {
    /**
     * The singleton instance
     */
    private static XSPReplyFailure INSTANCE = null;

    /**
     * Gets the default instance
     *
     * @return The default instance
     */
    public synchronized static XSPReplyFailure instance() {
        if (INSTANCE == null)
            return new XSPReplyServiceUnavailable();
        return INSTANCE;
    }

    /**
     * Initializes this reply
     */
    public XSPReplyServiceUnavailable() {
        super("FAILED: A required service is not available");
    }
}
