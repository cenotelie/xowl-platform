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

import java.net.URL;

/**
 * Represents a contribution to the web interface
 *
 * @author Laurent Wouters
 */
public interface UIContribution extends Service {
    /**
     * The URI prefix for web connections
     */
    String URI_WEB = "/web";

    /**
     * Gets the contribution-specific prefix of the URI
     * This essentially is what identifies this contribution
     *
     * @return The contribution-specific prefix of the URI
     */
    String getPrefix();

    /**
     * Gets the priority of this prefix
     * Greater has more priority
     *
     * @return The priority of this prefix
     */
    int getPriority();

    /**
     * Gets the URL for the requested resource
     *
     * @param resource The requested resource, local to this contribution
     * @return The URL for the requested resource
     */
    URL getResource(String resource);
}
