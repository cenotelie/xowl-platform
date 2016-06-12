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

package org.xowl.platform.services.webapp;

import org.xowl.platform.kernel.UIContribution;

import java.net.URL;

/**
 * Represents a directory of the registered UI contributions
 *
 * @author Laurent Wouters
 */
public interface ContributionDirectory {
    /**
     * Registers a contribution
     *
     * @param contribution The contribution to register
     */
    void register(UIContribution contribution);

    /**
     * Unregisters a contribution
     *
     * @param contribution The contribution to unregister
     */
    void unregister(UIContribution contribution);

    /**
     * Resolves the resource URL for the specified requested URI
     *
     * @param uri The requested URI
     * @return The resolved URL, or null if it cannot be resolve
     */
    URL resolveResource(String uri);
}
