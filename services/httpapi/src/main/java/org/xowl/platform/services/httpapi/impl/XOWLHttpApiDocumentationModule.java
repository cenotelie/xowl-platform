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

package org.xowl.platform.services.httpapi.impl;

import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ui.WebUIContribution;
import org.xowl.platform.kernel.webapi.HttpApiDiscoveryService;
import org.xowl.platform.kernel.webapi.HttpApiResource;

import java.net.URL;

/**
 * Implements a web module for the documentation of the HTTP APIs
 *
 * @author Laurent Wouters
 */
public class XOWLHttpApiDocumentationModule implements WebUIContribution {
    /**
     * The URI prefix for this contribution
     */
    private final String prefix;

    /**
     * Initializes this contribution
     */
    public XOWLHttpApiDocumentationModule() {
        this.prefix = PlatformHttp.getUriPrefixWeb() + "/contributions/documentation";
    }

    @Override
    public String getIdentifier() {
        return XOWLHttpApiDocumentationModule.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - API Documentation Contribution";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public URL getResource(String resource) {
        String rest = resource.substring(prefix.length() + 1);
        HttpApiDiscoveryService discoveryService = Register.getComponent(HttpApiDiscoveryService.class);
        if (discoveryService == null)
            return null;
        for (HttpApiResource apiResource : discoveryService.getResources()) {
            if (apiResource.getFileName().equals(rest))
                return apiResource.getResourceURL();
        }
        return null;
    }
}
