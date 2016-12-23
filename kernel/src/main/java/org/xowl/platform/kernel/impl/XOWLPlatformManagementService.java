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
import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.ApiError;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.SSLGenerator;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
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
import org.xowl.platform.kernel.platform.*;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
     * API error - The addon is already installed
     */
    public static final ApiError ERROR_ADDON_ALREADY_INSTALLED = new ApiError(0x0021,
            "The addon is already installed.",
            ERROR_HELP_PREFIX + "0x0021.html");
    /**
     * API error - The addon is not installed
     */
    public static final ApiError ERROR_ADDON_NOT_INSTALLED = new ApiError(0x0022,
            "The addon is not installed.",
            ERROR_HELP_PREFIX + "0x0022.html");
    /**
     * API error - The provided addon package is invalid
     */
    public static final ApiError ERROR_INVALID_ADDON_PACKAGE = new ApiError(0x0023,
            "The provided addon package is invalid.",
            ERROR_HELP_PREFIX + "0x0023.html");

    /**
     * The cache of bundles
     */
    private final List<Bundle> bundles;
    /**
     * The product descriptor
     */
    private final Product product;
    /**
     * The cache of addons
     */
    private final List<Addon> addons;
    /**
     * The cache to the addons
     */
    private final File addonsCache;

    /**
     * Initializes this service
     *
     * @param configurationService The current configuration service
     * @param executionService     The current execution service
     */
    public XOWLPlatformManagementService(ConfigurationService configurationService, JobExecutionService executionService) {
        Configuration configuration = configurationService.getConfigFor(this);
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
        this.addons = new ArrayList<>();
        this.addonsCache = new File(System.getenv(Env.ROOT), configuration.get("addonsStorage"));
        if (this.addonsCache.exists())
            loadAddonsCache();
        enforceHttpConfigFelix(configuration, executionService);
    }

    /**
     * Loads addon descriptors in the addons cache
     */
    private void loadAddonsCache() {
        File[] files = addonsCache.listFiles();
        if (files != null) {
            for (int i = 0; i != files.length; i++) {
                if (files[i].getName().endsWith(".descriptor")) {
                    try (Reader reader = Files.getReader(files[i].getAbsolutePath())) {
                        String content = Files.read(reader);
                        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
                        if (definition == null) {
                            Logging.getDefault().error("Failed to parse the descriptor " + files[i].getAbsolutePath());
                            return;
                        }
                        Addon addon = new Addon(definition);
                        addon.setInstalled();
                        addons.add(addon);
                    } catch (IOException exception) {
                        Logging.getDefault().error(exception);
                    }
                }
            }
        }
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
        } else if (request.getUri().equals(URI_API + "/addons")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (Addon addon : getAddons()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(addon.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else if (request.getUri().startsWith(URI_API + "/addons")) {
            String rest = request.getUri().substring(URI_API.length() + "/addons".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String addonId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index >= 0)
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    for (Addon addon : addons) {
                        if (Objects.equals(addonId, addon.getIdentifier()))
                            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, addon.serializedJSON());
                    }
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                }
                case HttpConstants.METHOD_DELETE: {
                    return XSPReplyUtils.toHttpResponse(uninstallAddon(addonId), null);
                }
                case HttpConstants.METHOD_PUT: {
                    byte[] content = request.getContent();
                    if (content == null || content.length == 0)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                    return XSPReplyUtils.toHttpResponse(installAddon(addonId, new ByteArrayInputStream(content)), null);
                }
                default:
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
            }
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
    public Collection<Addon> getAddons() {
        return Collections.unmodifiableCollection(addons);
    }

    @Override
    public XSPReply installAddon(String identifier, InputStream packageStream) {
        for (Addon addon : addons) {
            if (Objects.equals(addon.getIdentifier(), identifier))
                return new XSPReplyApiError(ERROR_ADDON_ALREADY_INSTALLED);
        }

        File directory = new File(addonsCache, UUID.randomUUID().toString());
        if (!directory.mkdirs()) {
            Logging.getDefault().error("Failed to create directory " + directory.getAbsolutePath());
            return new XSPReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Failed to unpack the addon.");
        }
        try {
            unpackAddon(packageStream, directory);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Failed to unpack the addon.");
        }
        File fileDescriptor = new File(directory, "descriptor.json");
        if (!fileDescriptor.exists()) {
            // delete the directory
            Files.deleteFolder(directory);
            return new XSPReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "No descriptor found.");
        }

        Addon descriptor;
        try (InputStream stream = new FileInputStream(fileDescriptor)) {
            String content = Files.read(stream, Files.CHARSET);
            ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
            descriptor = new Addon(definition);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            Files.deleteFolder(directory);
            return new XSPReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Failed to read the descriptor.");
        }

        // check the identifier
        if (!Objects.equals(descriptor.getIdentifier(), identifier)) {
            Files.deleteFolder(directory);
            return new XSPReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Descriptor does not match the provided identifier.");
        }
        // check the presence of the bundles
        Collection<File> fileBundles = new ArrayList<>();
        for (AddonBundle bundle : descriptor.getBundles()) {
            File fileBundle = new File(directory, bundle.serializedString() + ".jar");
            if (!fileBundle.exists()) {
                Files.deleteFolder(directory);
                return new XSPReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Addon package does not contain bundle " + fileBundle.getName());
            }
            fileBundles.add(fileBundle);
        }

        addons.add(descriptor);
        // move the content
        try {
            File felixBundles = new File(new File(System.getenv(Env.ROOT), "felix"), "bundle");
            java.nio.file.Files.move(fileDescriptor.toPath(), (new File(addonsCache, identifier + ".descriptor")).toPath(), REPLACE_EXISTING);
            for (File file : fileBundles) {
                java.nio.file.Files.move(file.toPath(), (new File(felixBundles, file.getName())).toPath(), REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            Files.deleteFolder(directory);
            return new XSPReplyException(exception);
        }
        // delete the folder
        Files.deleteFolder(directory);
        descriptor.setInstalled();
        // TODO: fire installation event
        return new XSPReplyResult<>(descriptor);
    }

    /**
     * Unpacks an addon into a directory
     *
     * @param packageStream the stream for the addon package
     * @param directory     The directory to unpack to
     */
    private void unpackAddon(InputStream packageStream, File directory) throws IOException {
        byte[] buffer = new byte[8192];
        try (ZipInputStream zipInputStream = new ZipInputStream(packageStream)) {
            while (zipInputStream.available() > 0) {
                ZipEntry entry = zipInputStream.getNextEntry();
                File target = new File(directory, entry.getName());
                int total = 0;
                try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
                    while (total < entry.getSize()) {
                        int read = zipInputStream.read(buffer, 0, buffer.length);
                        total += read;
                        fileOutputStream.write(buffer, 0, read);
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }

    @Override
    public XSPReply uninstallAddon(String identifier) {
        Addon descriptor = null;
        for (Addon addon : addons) {
            if (Objects.equals(addon.getIdentifier(), identifier)) {
                descriptor = addon;
                break;
            }
        }
        if (descriptor == null)
            return new XSPReplyApiError(ERROR_ADDON_NOT_INSTALLED);

        return XSPReplyUnsupported.instance();
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
