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

import fr.cenotelie.commons.utils.TextUtils;

import java.net.URL;

/**
 * Base implementation of an HTTP API resource
 *
 * @author Laurent Wouters
 */
public class HttpApiResourceBase implements HttpApiResource {
    /**
     * The type of the parent service
     */
    private final Class<? extends HttpApiService> serviceType;
    /**
     * The corresponding embedded resource in the jar
     */
    private final String resource;
    /**
     * The file name for the resource
     */
    private final String fileName;
    /**
     * The human readable name for the resource
     */
    private final String name;
    /**
     * The MIME type for the resource
     */
    private final String mimeType;

    /**
     * Initializes this resource
     *
     * @param serviceType The type of the parent service
     * @param resource    The corresponding embedded resource in the jar
     * @param name        The human readable name for the resource
     * @param mimeType    The MIME type for the resource
     */
    public HttpApiResourceBase(Class<? extends HttpApiService> serviceType, String resource, String name, String mimeType) {
        this.serviceType = serviceType;
        this.resource = resource;
        this.name = name;
        this.mimeType = mimeType;
        int index = resource.lastIndexOf("/");
        this.fileName = index < 0 ? resource : resource.substring(index + 1);
    }

    @Override
    public String getIdentifier() {
        return resource;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String serializedString() {
        return resource;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(HttpApiResource.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(resource) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"fileName\": \"" +
                TextUtils.escapeStringJSON(fileName) +
                "\", \"mimeType\": \"" +
                TextUtils.escapeStringJSON(mimeType) +
                "\"}";
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public URL getResourceURL() {
        return serviceType.getResource(resource);
    }
}
