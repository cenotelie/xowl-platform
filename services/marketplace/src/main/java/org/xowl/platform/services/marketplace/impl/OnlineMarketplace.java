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

package org.xowl.platform.services.marketplace.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConnection;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.platform.Addon;
import org.xowl.platform.services.marketplace.MarketplaceDescriptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Implements a static marketplace that is online
 *
 * @author Laurent Wouters
 */
class OnlineMarketplace extends StaticMarketplace {
    /**
     * The URL location
     */
    private final String location;
    /**
     * The HTTP connection to use
     */
    private final HttpConnection connection;

    /**
     * Initializes this marketplace
     *
     * @param configuration The configuration for this marketplace
     */
    public OnlineMarketplace(Section configuration) {
        String url = configuration.get("url");
        this.location = url.endsWith("/") ? url : (url + "/");
        this.connection = new HttpConnection(
                this.location,
                configuration.get("login"),
                configuration.get("password")
        );
    }

    @Override
    protected MarketplaceDescriptor loadMarketplaceDescriptor() {
        HttpResponse response = connection.request(MARKETPLACE_DESCRIPTOR, "GET", HttpConstants.MIME_JSON);
        if (response.getCode() != HttpURLConnection.HTTP_OK) {
            Logging.get().error("Cannot find marketplace descriptor " + location + MARKETPLACE_DESCRIPTOR + " (" + response.getCode() + ")");
            return null;
        }
        String content = response.getBodyAsString();
        if (content == null) {
            Logging.get().error("Marketplace descriptor is empty " + location + MARKETPLACE_DESCRIPTOR);
            return null;
        }
        ASTNode definition = JsonLoader.parseJson(Logging.get(), content);
        if (definition == null) {
            Logging.get().error("Failed to parse marketplace descriptor " + location + MARKETPLACE_DESCRIPTOR);
            return null;
        }
        return new MarketplaceDescriptor(definition);
    }

    @Override
    protected Addon loadAddonDescriptor(String identifier) {
        HttpResponse response = connection.request(identifier + ".descriptor", "GET", HttpConstants.MIME_JSON);
        if (response.getCode() != HttpURLConnection.HTTP_OK) {
            Logging.get().error("Cannot find addon descriptor " + location + identifier + ".descriptor (" + response.getCode() + ")");
            return null;
        }
        String content = response.getBodyAsString();
        if (content == null) {
            Logging.get().error("Addon descriptor is empty " + location + identifier + ".descriptor");
            return null;
        }
        ASTNode definition = JsonLoader.parseJson(Logging.get(), content);
        if (definition == null) {
            Logging.get().error("Failed to parse addon descriptor  " + location + identifier + ".descriptor");
            return null;
        }
        return new Addon(definition);
    }

    @Override
    public InputStream getAddonPackage(String identifier) {
        loadContent();
        Proxy proxy = addons.get(identifier);
        if (proxy == null)
            return null;
        HttpResponse response = connection.request(identifier + ".zip", "GET", HttpConstants.MIME_JSON);
        if (response.getCode() != HttpURLConnection.HTTP_OK) {
            Logging.get().error("Cannot find addon package " + location + identifier + ".zip (" + response.getCode() + ")");
            return null;
        }
        byte[] content = response.getBodyAsBytes();
        return new ByteArrayInputStream(content);
    }
}
