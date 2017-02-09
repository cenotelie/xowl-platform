/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

import org.xowl.infra.utils.config.Configuration;

/**
 * Manages the HTTP configuration for the platform
 *
 * @author Laurent Wouters
 */
public class PlatformHttp implements Registrable {
    /**
     * The URI suffix for the web API
     */
    private static final String URI_API = "/api";
    /**
     * The URI suffix for the web resources
     */
    private static final String URI_WEB = "/web";

    /**
     * The singleton instance of this structure
     */
    private static PlatformHttp INSTANCE = null;

    /**
     * Initializes this structure
     *
     * @param configurationService The configuration service
     * @return The singleton instance
     */
    public static PlatformHttp initialize(ConfigurationService configurationService) {
        if (INSTANCE == null)
            INSTANCE = new PlatformHttp(configurationService);
        return INSTANCE;
    }

    /**
     * Gets the singleton instance of this structure
     *
     * @return The singleton instance
     */
    public static PlatformHttp instance() {
        return INSTANCE;
    }

    /**
     * Gets the uri prefix for the API endpoint
     *
     * @return The uri prefix for the API endpoint
     */
    public static String getUriPrefixApi() {
        return INSTANCE.getHttpUriPrefix() + URI_API;
    }

    /**
     * Gets the uri prefix for the web resources
     *
     * @return The uri prefix for the web resources
     */
    public static String getUriPrefixWeb() {
        return INSTANCE.getHttpUriPrefix() + URI_WEB;
    }


    /**
     * Whether HTTP shall be enabled
     */
    private final boolean httpEnabled;
    /**
     * Whether HTTPS shall be enabled
     */
    private final boolean httpsEnabled;
    /**
     * The host name or IP address of the interface to listen on
     */
    private final String httpHost;
    /**
     * The port for un-secured HTTP
     */
    private final int httpPort;
    /**
     * The port for secured HTTP
     */
    private final int httpsPort;
    /**
     * The HTTP URI prefix for all connections to this instance
     */
    private final String httpURIPrefix;
    /**
     * The path from the distribution's root to the TLS key store (if any)
     */
    private final String tlsKeyStore;
    /**
     * The password for the TLS key store (if any)
     */
    private final String tlsKeyPassword;
    /**
     * The advertised public URI for the platform
     */
    private final String publicUri;

    /**
     * Gets whether HTTP shall be enabled
     *
     * @return Whether HTTP shall be enabled
     */
    public boolean isHttpEnabled() {
        return httpEnabled;
    }

    /**
     * Gets whether HTTPS shall be enabled
     *
     * @return Whether HTTPS shall be enabled
     */
    public boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    /**
     * Gets the host name or IP address of the interface to listen on
     *
     * @return The host name or IP address of the interface to listen on
     */
    public String getHttpHost() {
        return httpHost;
    }

    /**
     * Gets the port for un-secured HTTP
     *
     * @return The port for un-secured HTTP
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Gets the port for secured HTTP
     *
     * @return The port for secured HTTP
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * Gets the HTTP URI prefix for all connections to this instance
     *
     * @return The HTTP URI prefix for all connections to this instance
     */
    public String getHttpUriPrefix() {
        return httpURIPrefix;
    }

    /**
     * Gets the path from the distribution's root to the TLS key store (if any)
     *
     * @return The path from the distribution's root to the TLS key store (if any)
     */
    public String getTlsKeyStore() {
        return tlsKeyStore;
    }

    /**
     * Gets the password for the TLS key store (if any)
     *
     * @return The password for the TLS key store (if any)
     */
    public String getTlsKeyPassword() {
        return tlsKeyPassword;
    }

    /**
     * Gets the advertised public URI for the platform
     *
     * @return The advertised public URI for the platform
     */
    public String getPublicUri() {
        return publicUri;
    }

    /**
     * Initializes this structure
     *
     * @param configurationService The configuration service
     */
    private PlatformHttp(ConfigurationService configurationService) {
        Configuration configuration = configurationService.getConfigFor(PlatformHttp.class.getCanonicalName());
        this.httpEnabled = Boolean.parseBoolean(configuration.get("httpEnabled"));
        this.httpsEnabled = Boolean.parseBoolean(configuration.get("httpsEnabled"));
        this.httpHost = configuration.get("httpHost");
        this.httpPort = Integer.parseInt(configuration.get("httpPort"));
        this.httpsPort = Integer.parseInt(configuration.get("httpsPort"));
        this.httpURIPrefix = configuration.get("httpURIPrefix");
        this.tlsKeyStore = configuration.get("tlsKeyStore");
        this.tlsKeyPassword = configuration.get("tlsKeyPassword");
        String publicUri = configuration.get("publicUri");
        if (publicUri == null || publicUri.isEmpty()) {
            if (httpsEnabled)
                publicUri = "https://" + httpHost + ":" + httpPort + httpURIPrefix;
            else if (httpEnabled)
                publicUri = "http://" + httpHost + ":" + httpPort + httpURIPrefix;
            else
                publicUri = null;
        }
        this.publicUri = publicUri;
    }

    @Override
    public String getIdentifier() {
        return PlatformHttp.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Platform HTTP Configuration";
    }
}
