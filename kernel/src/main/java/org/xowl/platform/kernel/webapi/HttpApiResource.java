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

package org.xowl.platform.kernel.webapi;

import fr.cenotelie.commons.utils.Identifiable;
import fr.cenotelie.commons.utils.Serializable;

import java.net.URL;

/**
 * Represents a resource for the documentation of an HTTP API
 *
 * @author Laurent Wouters
 */
public interface HttpApiResource extends Identifiable, Serializable {
    /**
     * MIME type for HTML resources
     */
    String MIME_HTML = "text/html";
    /**
     * MIME type for RAML resources
     */
    String MIME_RAML = "application/raml+yaml";

    /**
     * Gets the MIME type for the resource
     *
     * @return The MIME type for the resource
     */
    String getMimeType();

    /**
     * Gets the name of the file
     *
     * @return The name of the file
     */
    String getFileName();

    /**
     * Gets the URL to the resource's content
     *
     * @return The URL to the resource's content
     */
    URL getResourceURL();
}
