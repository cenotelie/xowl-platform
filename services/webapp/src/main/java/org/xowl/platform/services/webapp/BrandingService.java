/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.webapp;

import org.xowl.platform.kernel.Service;

import java.net.URL;

/**
 * Represents a service for branding the web application
 *
 * @author Laurent Wouters
 */
public interface BrandingService extends Service {
    /**
     * The branding folder
     */
    String BRANDING = "/branding/";
    /**
     * The name of the title branding resource
     */
    String BRANDING_TITLE = "title.html";
    /**
     * The name of the favicon resource
     */
    String BRANDING_FAVICON = "favicon.png";

    /**
     * Gets the URL for a branding resource
     *
     * @param name The name of the branding resource, for example title.html
     * @return The corresponding URL
     */
    URL getResource(String name);

    /**
     * Gets the resource for the favicon
     *
     * @return The resource for the favicon
     */
    URL getFavicon();

    /**
     * Gets the header for page titles
     *
     * @return The header for page titles
     */
    String getTitleHeader();
}
