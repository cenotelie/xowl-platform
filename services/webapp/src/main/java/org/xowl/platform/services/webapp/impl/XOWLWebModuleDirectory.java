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

package org.xowl.platform.services.webapp.impl;

import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.services.webapp.WebModule;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Implements a directory of registered web modules
 *
 * @author Laurent Wouters
 */
public class XOWLWebModuleDirectory implements HttpAPIService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/webapp/modules"
    };

    /**
     * The registered modules
     */
    private final Collection<WebModule> modules;

    /**
     * Initializes this directory
     */
    public XOWLWebModuleDirectory() {
        this.modules = new ArrayList<>();
    }

    /**
     * Registers a web module
     *
     * @param module The web module to register
     */
    public void register(WebModule module) {
        this.modules.add(module);
    }

    /**
     * Unregisters a web module
     *
     * @param module The web module to unregister
     */
    public void unregister(WebModule module) {
        this.modules.remove(module);
    }

    @Override
    public String getIdentifier() {
        return XOWLWebModuleDirectory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Web Modules Directory";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (WebModule module : modules) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(module.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }
}
