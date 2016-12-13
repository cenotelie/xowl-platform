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

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkUtil;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.SSLGenerator;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.infra.utils.metrics.MetricSnapshotLong;
import org.xowl.infra.utils.product.Product;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.PlatformRebootJob;
import org.xowl.platform.kernel.platform.PlatformRoleAdmin;
import org.xowl.platform.kernel.platform.PlatformStartupEvent;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements the platform management service
 *
 * @author Laurent Wouters
 */
public class XOWLPlatformManagementService implements PlatformManagementService, FrameworkListener {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/kernel/platform";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_platform.raml", "Platform Management Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLPlatformManagementService.class, "/org/xowl/platform/kernel/api_platform.html", "Platform Management Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The cache of bundles
     */
    private final List<Bundle> bundles;
    /**
     * The product descriptor
     */
    private final Product product;

    /**
     * Initializes this service
     *
     * @param configurationService The current configuration service
     * @param executionService     The current execution service
     */
    public XOWLPlatformManagementService(ConfigurationService configurationService, JobExecutionService executionService) {
        bundles = new ArrayList<>();
        Product product = null;
        try {
            product = new Product(
                    "org.xowl.platform.XOWLFederationPlatform",
                    "xOWL Federation Platform",
                    XOWLPlatformManagementService.class);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
        }
        this.product = product;
        enforceHttpConfigFelix(configurationService.getConfigFor(this), executionService);
    }

    @Override
    public String getIdentifier() {
        return XOWLPlatformManagementService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Platform Management Service";
    }

    @Override
    public Collection<Metric> getMetrics() {
        return Arrays.asList(METRIC_USED_MEMORY, METRIC_FREE_MEMORY, METRIC_TOTAL_MEMORY, METRIC_MAX_MEMORY);
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        if (metric == METRIC_USED_MEMORY)
            return new MetricSnapshotLong(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        if (metric == METRIC_FREE_MEMORY)
            return new MetricSnapshotLong(Runtime.getRuntime().freeMemory());
        if (metric == METRIC_TOTAL_MEMORY)
            return new MetricSnapshotLong(Runtime.getRuntime().totalMemory());
        if (metric == METRIC_MAX_MEMORY)
            return new MetricSnapshotLong(Runtime.getRuntime().maxMemory());
        return null;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        // check for platform admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkCurrentHasRole(PlatformRoleAdmin.INSTANCE.getIdentifier());
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);

        if (request.getUri().equals(URI_API + "/product")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, product.serializedJSON());
        } else if (request.getUri().equals(URI_API + "/shutdown")) {
            if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
            return XSPReplyUtils.toHttpResponse(shutdown(), null);
        } else if (request.getUri().equals(URI_API + "/restart")) {
            if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
            return XSPReplyUtils.toHttpResponse(restart(), null);
        } else if (request.getUri().equals(URI_API + "/bundles")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (Bundle bundle : getPlatformBundles()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(serializeBundle(bundle));
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Override
    public HttpApiResource getApiSpecification() {
        return RESOURCE_SPECIFICATION;
    }

    @Override
    public HttpApiResource getApiDocumentation() {
        return RESOURCE_DOCUMENTATION;
    }

    @Override
    public HttpApiResource[] getApiOtherResources() {
        return null;
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(HttpApiService.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"specification\": " +
                RESOURCE_SPECIFICATION.serializedJSON() +
                ", \"documentation\": " +
                RESOURCE_DOCUMENTATION.serializedJSON() +
                "}";
    }

    @Override
    public Product getPlatformProduct() {
        return product;
    }

    @Override
    public Collection<Bundle> getPlatformBundles() {
        if (bundles.isEmpty()) {
            Bundle[] bundles = FrameworkUtil.getBundle(XOWLPlatformManagementService.class).getBundleContext().getBundles();
            for (int i = 0; i != bundles.length; i++) {
                this.bundles.add(bundles[i]);
            }
        }
        return Collections.unmodifiableCollection(bundles);
    }

    @Override
    public XSPReply shutdown() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.exit(PLATFORM_EXIT_NORMAL);
            }
        }, XOWLPlatformManagementService.class.getCanonicalName() + ".ThreadShutdown");
        thread.start();
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply restart() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.exit(PLATFORM_EXIT_RESTART);
            }
        }, XOWLPlatformManagementService.class.getCanonicalName() + ".ThreadRestart");
        thread.start();
        return XSPReplySuccess.instance();
    }

    @Override
    public void frameworkEvent(FrameworkEvent frameworkEvent) {
        if (frameworkEvent.getType() == FrameworkEvent.STARTED) {
            // the framework has started
            EventService eventService = ServiceUtils.getService(EventService.class);
            if (eventService != null)
                eventService.onEvent(new PlatformStartupEvent(this));
        }
    }

    /**
     * Serializes in JSON the information about an OSGi bundle
     *
     * @param bundle The bundle to serialize
     * @return The serialized form
     */
    private static String serializeBundle(Bundle bundle) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(Bundle.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        if (bundle.getSymbolicName() != null)
            builder.append(TextUtils.escapeStringJSON(bundle.getSymbolicName()));
        builder.append("\", \"name\": \"");
        String value = (String) bundle.getHeaders().get("Bundle-Name");
        if (value != null) {
            if (value.startsWith("%"))
                builder.append(TextUtils.escapeStringJSON(value.substring(1)));
            else
                builder.append(TextUtils.escapeStringJSON(value));
        }
        builder.append("\", \"description\": \"");
        value = (String) bundle.getHeaders().get("Bundle-Description");
        if (value != null) {
            if (value.startsWith("%"))
                builder.append(TextUtils.escapeStringJSON(value.substring(1)));
            else
                builder.append(TextUtils.escapeStringJSON(value));
        }
        builder.append("\", \"vendor\": \"");
        value = (String) bundle.getHeaders().get("Bundle-Vendor");
        if (value != null) {
            if (value.startsWith("%"))
                builder.append(TextUtils.escapeStringJSON(value.substring(1)));
            else
                builder.append(TextUtils.escapeStringJSON(value));
        }
        builder.append("\", \"version\": \"");
        builder.append(TextUtils.escapeStringJSON(bundle.getVersion().toString()));
        builder.append("\", \"state\": \"");
        switch (bundle.getState()) {
            case Bundle.UNINSTALLED:
                builder.append("UNINSTALLED");
                break;
            case Bundle.INSTALLED:
                builder.append("INSTALLED");
                break;
            case Bundle.RESOLVED:
                builder.append("RESOLVED");
                break;
            case Bundle.STARTING:
                builder.append("STARTING");
                break;
            case Bundle.STOPPING:
                builder.append("STOPPING");
                break;
            case Bundle.ACTIVE:
                builder.append("ACTIVE");
                break;
        }
        builder.append("\"}");
        return builder.toString();
    }

    /**
     * Enforces the configuration of the HTTP service provided by Felix when it is the current OSGi framework
     *
     * @param platformConfiguration The expected configuration for the platform
     * @param executionService      The job execution service
     */
    private static void enforceHttpConfigFelix(Configuration platformConfiguration, JobExecutionService executionService) {
        File root = new File(System.getProperty(Env.ROOT));
        File confFile = new File(new File(new File(root, "felix"), "conf"), "config.properties");
        Configuration felixConfiguration = new Configuration();
        try {
            felixConfiguration.load(confFile.getAbsolutePath(), Files.CHARSET);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return;
        }
        boolean mustReboot = false;

        // Felix default for org.apache.felix.http.enable is true
        boolean httpEnabled = false;
        String valueTarget = platformConfiguration.get("httpEnabled");
        String valueReal = felixConfiguration.get("org.apache.felix.http.enable");
        if ("true".equalsIgnoreCase(valueTarget)) {
            httpEnabled = true;
            if (valueReal != null && "false".equalsIgnoreCase(valueReal)) {
                // must enable HTTP
                felixConfiguration.set("org.apache.felix.http.enable", "true");
                mustReboot = true;
            }
        } else {
            if (valueReal == null || "true".equalsIgnoreCase(valueReal)) {
                // must disable HTTP
                felixConfiguration.set("org.apache.felix.http.enable", "false");
                mustReboot = true;
            }
        }

        // Felix default for org.apache.felix.https.enable is false
        boolean httpsEnabled = false;
        valueTarget = platformConfiguration.get("httpsEnabled");
        valueReal = felixConfiguration.get("org.apache.felix.https.enable");
        if ("true".equalsIgnoreCase(valueTarget)) {
            httpsEnabled = true;
            if (valueReal == null || "false".equalsIgnoreCase(valueReal)) {
                // must enable HTTPS
                felixConfiguration.set("org.apache.felix.https.enable", "true");
                mustReboot = true;
            }
        } else {
            if (valueReal != null && "true".equalsIgnoreCase(valueReal)) {
                // must disable HTTPS
                felixConfiguration.set("org.apache.felix.https.enable", "false");
                mustReboot = true;
            }
        }

        if (httpEnabled) {
            // Felix default for org.osgi.service.http.port is 8080
            valueTarget = platformConfiguration.get("httpPort");
            valueReal = felixConfiguration.get("org.osgi.service.http.port");
            if ((valueReal == null && !"8080".equals(valueTarget))
                    || (!Objects.equals(valueReal, valueTarget))) {
                // must update port
                felixConfiguration.set("org.osgi.service.http.port", valueTarget);
                mustReboot = true;
            }
        }

        if (httpsEnabled) {
            // Felix default for org.osgi.service.http.port.secure is 8443
            valueTarget = platformConfiguration.get("httpsPort");
            valueReal = felixConfiguration.get("org.osgi.service.http.port.secure");
            if ((valueReal == null && !"8443".equals(valueTarget))
                    || (!Objects.equals(valueReal, valueTarget))) {
                // must update port
                felixConfiguration.set("org.osgi.service.http.port.secure", valueTarget);
                mustReboot = true;
            }

            valueTarget = platformConfiguration.get("tlsKeyStore");
            valueReal = felixConfiguration.get("org.apache.felix.https.keystore");
            if (valueTarget == null || valueTarget.isEmpty()) {
                // key store is auto-generated
                if (valueReal == null || valueReal.isEmpty()) {
                    // not generated yet
                    File keyStoreFile = new File(new File(new File(root, "felix"), "conf"), "keystore.jks");
                    String password = SSLGenerator.generateKeyStore(keyStoreFile, "platform.xowl.org");
                    if (password == null) {
                        Logging.getDefault().error("Failed to generate the keystore");
                    } else {
                        felixConfiguration.set("org.apache.felix.https.keystore", keyStoreFile.getAbsolutePath());
                        felixConfiguration.set("org.apache.felix.https.keystore.password", password);
                        felixConfiguration.set("org.apache.felix.https.keystore.key.password", password);
                        mustReboot = true;
                    }
                }
            } else {
                // key store is forced
                if (!Objects.equals(valueReal, valueTarget)) {
                    felixConfiguration.set("org.apache.felix.https.keystore", valueTarget);
                    mustReboot = true;
                }
                valueTarget = platformConfiguration.get("tlsKeyPassword");
                valueReal = felixConfiguration.get("org.apache.felix.https.keystore.password");
                if (!Objects.equals(valueReal, valueTarget)) {
                    felixConfiguration.set("org.apache.felix.https.keystore.password", valueTarget);
                    felixConfiguration.set("org.apache.felix.https.keystore.key.password", valueTarget);
                    mustReboot = true;
                }
            }
        }

        if (mustReboot) {
            try {
                felixConfiguration.save(confFile.getAbsolutePath(), Files.CHARSET);
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
                return;
            }
            executionService.schedule(new PlatformRebootJob());
        }
    }
}
