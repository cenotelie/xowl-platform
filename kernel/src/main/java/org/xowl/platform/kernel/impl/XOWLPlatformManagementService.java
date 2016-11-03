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

package org.xowl.platform.kernel.impl;

import org.xowl.infra.server.xsp.*;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.SSLGenerator;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.OSGiBundle;
import org.xowl.platform.kernel.platform.PlatformDescriptor;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Implements the platform management service
 *
 * @author Laurent Wouters
 */
public class XOWLPlatformManagementService implements PlatformManagementService {
    /**
     * Represents the details of a specific OSGi implementation
     */
    private interface PlatformImpl extends Identifiable {
        /**
         * Setups the configuration for using a TLS certificate
         *
         * @param keyStore The key store with the private key to use
         * @param password The password to the key store
         * @return Whether the operation succeeded
         */
        XSPReply setupTSL(File keyStore, String password);
    }

    /**
     * The Felix implementation of OSGi
     */
    private static final class PlatformImplFelix implements PlatformImpl {

        @Override
        public String getIdentifier() {
            return PlatformImplFelix.class.getCanonicalName();
        }

        @Override
        public String getName() {
            return "Apache Felix";
        }

        @Override
        public XSPReply setupTSL(File keyStore, String password) {
            File confDir = new File(System.getProperty(Env.CONF_DIR));
            Configuration platformConfig = new Configuration();
            try {
                platformConfig.load(confDir.getAbsolutePath(), Files.CHARSET);
                String value = platformConfig.get("org.osgi.service.http.port.secure");
                if (value == null)
                    platformConfig.set("org.osgi.service.http.port.secure", "8443");
                value = platformConfig.get("org.apache.felix.https.enable");
                if (value == null || "false".equalsIgnoreCase(value))
                    platformConfig.set("org.apache.felix.https.enable", "true");
                platformConfig.set("org.apache.felix.https.keystore", keyStore.getAbsolutePath());
                platformConfig.set("org.apache.felix.https.keystore.password", password);
                platformConfig.set("org.apache.felix.https.keystore.key.password", password);
                platformConfig.save(confDir.getAbsolutePath(), Files.CHARSET);
                return XSPReplySuccess.instance();
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
                return new XSPReplyFailure(exception.getMessage());
            }
        }
    }

    /**
     * The URIs for this service
     */
    private static final String[] URIS = new String[]{
            "services/admin/platform"
    };

    /**
     * The details of the OSGi implementation we are running on
     */
    private final PlatformImpl osgiImpl;

    /**
     * Initializes this service
     */
    public XOWLPlatformManagementService() {
        osgiImpl = new PlatformImplFelix();
    }

    @Override
    public String getIdentifier() {
        return XOWLSecurityService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Platform Descriptor Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIS);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        String[] actions = parameters.get("action");
        if (actions == null || actions.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyResultCollection<>(getPlatformBundles()), null);

        switch (actions[0]) {
            case "shutdown":
                return XSPReplyUtils.toHttpResponse(shutdown(), null);
            case "restart":
                return XSPReplyUtils.toHttpResponse(restart(), null);
            case "regenerateTLS": {
                String[] aliases = parameters.get("alias");
                if (aliases == null || aliases.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected alias parameter");
                return XSPReplyUtils.toHttpResponse(regenerateTLSConfig(aliases[0]), null);
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Override
    public Collection<OSGiBundle> getPlatformBundles() {
        return PlatformDescriptor.INSTANCE.getPlatformBundles();
    }

    @Override
    public XSPReply regenerateTLSConfig(String alias) {
        File confDir = new File(System.getProperty(Env.CONF_DIR));
        File targetKeyStore = new File(confDir, "keystore.jks");
        String password = SSLGenerator.generateKeyStore(targetKeyStore, alias);
        if (password == null)
            return new XSPReplyFailure("Failed to generate TLS certificate");
        return osgiImpl.setupTSL(targetKeyStore, password);
    }

    @Override
    public XSPReply shutdown() {
        System.exit(PLATFORM_EXIT_NORMAL);
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply restart() {
        System.exit(PLATFORM_EXIT_RESTART);
        return XSPReplySuccess.instance();
    }
}
