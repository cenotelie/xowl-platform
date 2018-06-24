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

package org.xowl.platform.kernel.stdimpl;

import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.SSLGenerator;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.*;
import fr.cenotelie.commons.utils.http.HttpConstants;
import fr.cenotelie.commons.utils.http.HttpResponse;
import fr.cenotelie.commons.utils.http.URIUtils;
import fr.cenotelie.commons.utils.ini.IniDocument;
import fr.cenotelie.commons.utils.json.Json;
import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.commons.utils.metrics.Metric;
import fr.cenotelie.commons.utils.metrics.MetricSnapshot;
import fr.cenotelie.commons.utils.metrics.MetricSnapshotLong;
import fr.cenotelie.commons.utils.product.Product;
import fr.cenotelie.hime.redist.ASTNode;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.platform.*;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Implements the platform management service
 *
 * @author Laurent Wouters
 */
public class KernelPlatformManagementService implements PlatformManagementService, HttpApiService {
    /**
     * The name of the descriptor file in a distribution
     */
    private static final String DESCRIPTOR_FILE = "descriptor.json";

    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(KernelPlatformManagementService.class, "/org/xowl/platform/kernel/stdimpl/api_platform.raml", "Platform Management Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(KernelPlatformManagementService.class, "/org/xowl/platform/kernel/stdimpl/api_platform.html", "Platform Management Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The URI for the API services
     */
    private final String apiUri;
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
     */
    public KernelPlatformManagementService(ConfigurationService configurationService) {
        IniDocument configuration = configurationService.getConfigFor(PlatformManagementService.class.getCanonicalName());
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/kernel/platform";
        this.bundles = new ArrayList<>();
        this.product = loadProductDescriptor();
        this.addons = new ArrayList<>();
        this.addonsCache = PlatformUtils.resolve(configuration.get("addonsStorage"));
        if (this.addonsCache.exists())
            loadAddonsCache();
    }

    /**
     * Loads the descriptor for the platform product
     *
     * @return The descriptor
     */
    private Product loadProductDescriptor() {
        File fileDescriptor = PlatformUtils.resolve(DESCRIPTOR_FILE);
        try (Reader reader = IOUtils.getReader(fileDescriptor)) {
            ASTNode definition = Json.parse(Logging.get(), reader);
            return new ProductBase(definition);
        } catch (IOException exception) {
            Logging.get().error(exception);
            return null;
        }
    }

    /**
     * Loads addon descriptors in the addons cache
     */
    private void loadAddonsCache() {
        File[] files = addonsCache.listFiles();
        if (files != null) {
            for (int i = 0; i != files.length; i++) {
                if (files[i].getName().endsWith(".descriptor")) {
                    try (Reader reader = IOUtils.getReader(files[i].getAbsolutePath())) {
                        String content = IOUtils.read(reader);
                        ASTNode definition = Json.parse(Logging.get(), content);
                        if (definition == null) {
                            Logging.get().error("Failed to parse the descriptor " + files[i].getAbsolutePath());
                            return;
                        }
                        Addon addon = new Addon(definition);
                        addon.setInstalled();
                        addons.add(addon);
                    } catch (IOException exception) {
                        Logging.get().error(exception);
                    }
                }
            }
        }
    }

    @Override
    public String getIdentifier() {
        return KernelPlatformManagementService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Platform Management Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
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
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public boolean requireAuth(HttpApiRequest request) {
        return true;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (request.getUri().equals(apiUri + "/product")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            Product product = getPlatformProduct();
            if (product == null)
                return ReplyUtils.toHttpResponse(ReplyNotFound.instance());
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, product.serializedJSON());
        } else if (request.getUri().equals(apiUri + "/shutdown")) {
            if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
            return ReplyUtils.toHttpResponse(shutdown());
        } else if (request.getUri().equals(apiUri + "/restart")) {
            if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
            return ReplyUtils.toHttpResponse(restart());
        } else if (request.getUri().equals(apiUri + "/bundles")) {
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
        } else if (request.getUri().equals(apiUri + "/addons")) {
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
        } else if (request.getUri().startsWith(apiUri + "/addons")) {
            String rest = request.getUri().substring(apiUri.length() + "/addons".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String addonId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index >= 0)
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    for (Addon addon : getAddons()) {
                        if (Objects.equals(addonId, addon.getIdentifier()))
                            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, addon.serializedJSON());
                    }
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                }
                case HttpConstants.METHOD_DELETE: {
                    return ReplyUtils.toHttpResponse(uninstallAddon(addonId));
                }
                case HttpConstants.METHOD_PUT: {
                    byte[] content = request.getContent();
                    if (content == null || content.length == 0)
                        return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT));
                    return ReplyUtils.toHttpResponse(installAddon(addonId, new ByteArrayInputStream(content)));
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
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        Reply reply = securityService.checkAction(ACTION_GET_PRODUCT);
        if (!reply.isSuccess())
            return null;
        return product;
    }

    @Override
    public Collection<Bundle> getPlatformBundles() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return Collections.emptyList();
        Reply reply = securityService.checkAction(ACTION_GET_BUNDLES);
        if (!reply.isSuccess())
            return Collections.emptyList();
        if (bundles.isEmpty()) {
            Bundle[] bundles = FrameworkUtil.getBundle(KernelPlatformManagementService.class).getBundleContext().getBundles();
            for (int i = 0; i != bundles.length; i++) {
                this.bundles.add(bundles[i]);
            }
        }
        return Collections.unmodifiableCollection(bundles);
    }

    @Override
    public Collection<Addon> getAddons() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return Collections.emptyList();
        Reply reply = securityService.checkAction(ACTION_GET_ADDONS);
        if (!reply.isSuccess())
            return Collections.emptyList();
        return Collections.unmodifiableCollection(addons);
    }

    @Override
    public Reply installAddon(String identifier, InputStream packageStream) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_INSTALL_ADDON);
        if (!reply.isSuccess())
            return reply;

        synchronized (addons) {
            for (Addon addon : addons) {
                if (Objects.equals(addon.getIdentifier(), identifier))
                    return new ReplyApiError(ERROR_ADDON_ALREADY_INSTALLED);
            }

            File directory = new File(addonsCache, UUID.randomUUID().toString());
            if (!directory.mkdirs()) {
                Logging.get().error("Failed to create directory " + directory.getAbsolutePath());
                return new ReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Failed to unpack the addon.");
            }
            try {
                unpackAddon(packageStream, directory);
            } catch (IOException exception) {
                Logging.get().error(exception);
                return new ReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Failed to unpack the addon.");
            }
            File fileDescriptor = new File(directory, "descriptor.json");
            if (!fileDescriptor.exists()) {
                // delete the directory
                IOUtils.deleteFolder(directory);
                return new ReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "No descriptor found.");
            }

            Addon descriptor;
            try (Reader reader = IOUtils.getReader(fileDescriptor)) {
                ASTNode definition = Json.parse(Logging.get(), reader);
                if (definition == null) {
                    IOUtils.deleteFolder(directory);
                    return new ReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Failed to read the descriptor.");
                }
                descriptor = new Addon(definition);
            } catch (IOException exception) {
                Logging.get().error(exception);
                IOUtils.deleteFolder(directory);
                return new ReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Failed to read the descriptor.");
            }

            // check the identifier
            if (!Objects.equals(descriptor.getIdentifier(), identifier)) {
                IOUtils.deleteFolder(directory);
                return new ReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Descriptor does not match the provided identifier.");
            }
            // check the presence of the bundles
            Collection<File> fileBundles = new ArrayList<>();
            for (AddonBundle bundle : descriptor.getBundles()) {
                File fileBundle = new File(directory, bundle.serializedString() + ".jar");
                if (!fileBundle.exists()) {
                    IOUtils.deleteFolder(directory);
                    return new ReplyApiError(ERROR_INVALID_ADDON_PACKAGE, "Addon package does not contain bundle " + fileBundle.getName());
                }
                fileBundles.add(fileBundle);
            }

            addons.add(descriptor);
            // move the content
            try {
                File felixBundles = PlatformUtils.resolve("felix/bundle");
                Files.move(fileDescriptor.toPath(), (new File(addonsCache, identifier + ".descriptor")).toPath(), REPLACE_EXISTING);
                for (File file : fileBundles) {
                    Files.move(file.toPath(), (new File(felixBundles, file.getName())).toPath(), REPLACE_EXISTING);
                }
            } catch (IOException exception) {
                Logging.get().error(exception);
                IOUtils.deleteFolder(directory);
                return new ReplyException(exception);
            }
            // delete the folder
            IOUtils.deleteFolder(directory);
            descriptor.setInstalled();
            EventService eventService = Register.getComponent(EventService.class);
            if (eventService != null)
                eventService.onEvent(new AddonInstalledEvent(this, descriptor));
            return new ReplyResult<>(descriptor);
        }
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
            while (true) {
                ZipEntry entry = zipInputStream.getNextEntry();
                if (entry == null)
                    break;
                File target = new File(directory, entry.getName());
                try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
                    int read = 0;
                    while (read >= 0) {
                        read = zipInputStream.read(buffer, 0, buffer.length);
                        if (read > 0)
                            fileOutputStream.write(buffer, 0, read);
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }

    @Override
    public Reply uninstallAddon(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_UNINSTALL_ADDON);
        if (!reply.isSuccess())
            return reply;

        Addon descriptor = null;
        synchronized (addons) {
            for (Addon addon : addons) {
                if (Objects.equals(addon.getIdentifier(), identifier)) {
                    descriptor = addon;
                    break;
                }
            }
            if (descriptor == null)
                return new ReplyApiError(ERROR_ADDON_NOT_INSTALLED);

            File felixBundles = PlatformUtils.resolve("felix/bundle");
            for (AddonBundle bundle : descriptor.getBundles()) {
                File fileBundle = new File(felixBundles, bundle.serializedString() + ".jar");
                if (!fileBundle.delete())
                    Logging.get().error("Failed to delete file " + fileBundle.getAbsolutePath());
            }
            // delete the descriptor file
            File fileDescriptor = new File(addonsCache, identifier + ".descriptor");
            if (!fileDescriptor.delete())
                Logging.get().error("Failed to delete file " + fileDescriptor.getAbsolutePath());
            EventService eventService = Register.getComponent(EventService.class);
            if (eventService != null)
                eventService.onEvent(new AddonUninstalledEvent(this, descriptor));
            addons.remove(descriptor);
        }
        return new ReplyResult<>(descriptor);
    }

    @Override
    public Reply shutdown() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_SHUTDOWN);
        if (!reply.isSuccess())
            return reply;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.exit(PLATFORM_EXIT_NORMAL);
            }
        }, KernelPlatformManagementService.class.getCanonicalName() + ".ThreadShutdown");
        thread.start();
        return ReplySuccess.instance();
    }

    @Override
    public Reply restart() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_RESTART);
        if (!reply.isSuccess())
            return reply;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.exit(PLATFORM_EXIT_RESTART);
            }
        }, KernelPlatformManagementService.class.getCanonicalName() + ".ThreadRestart");
        thread.start();
        return ReplySuccess.instance();
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
     * @param platformHttp The expected configuration for the platform
     * @return Whether the platform should reboot
     */
    public static boolean enforceHttpConfigFelix(PlatformHttp platformHttp) {
        File confFile = PlatformUtils.resolve("felix/conf/config.properties");
        IniDocument felixConfiguration = new IniDocument();
        try {
            felixConfiguration.load(confFile);
        } catch (IOException exception) {
            Logging.get().error(exception);
            return false;
        }
        boolean mustReboot = false;

        // Felix default for org.apache.felix.http.enable is true
        boolean httpEnabled = false;
        String valueReal = felixConfiguration.get("org.apache.felix.http.enable");
        if (platformHttp.isHttpEnabled()) {
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
        valueReal = felixConfiguration.get("org.apache.felix.https.enable");
        if (platformHttp.isHttpsEnabled()) {
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

        if (httpEnabled || httpsEnabled) {
            // Felix default for org.apache.felix.http.host is null
            String valueTarget = platformHttp.getHttpHost();
            valueReal = felixConfiguration.get("org.apache.felix.http.host");
            if (!Objects.equals(valueReal, valueTarget)) {
                // must update bound address
                felixConfiguration.set("org.apache.felix.http.host", valueTarget);
                mustReboot = true;
            }
        }

        if (httpEnabled) {
            // Felix default for org.osgi.service.http.port is 8080
            int valueTarget = platformHttp.getHttpPort();
            valueReal = felixConfiguration.get("org.osgi.service.http.port");
            if ((valueReal == null && valueTarget != 8080)
                    || (!Objects.equals(valueReal, Integer.toString(valueTarget)))) {
                // must update port
                felixConfiguration.set("org.osgi.service.http.port", Integer.toString(valueTarget));
                mustReboot = true;
            }
        }

        if (httpsEnabled) {
            // Felix default for org.osgi.service.http.port.secure is 8443
            int portTarget = platformHttp.getHttpsPort();
            valueReal = felixConfiguration.get("org.osgi.service.http.port.secure");
            if ((valueReal == null && portTarget != 8443)
                    || (!Objects.equals(valueReal, Integer.toString(portTarget)))) {
                // must update port
                felixConfiguration.set("org.osgi.service.http.port.secure", Integer.toString(portTarget));
                mustReboot = true;
            }

            String valueTarget = platformHttp.getTlsKeyStore();
            valueReal = felixConfiguration.get("org.apache.felix.https.keystore");
            if (valueTarget == null || valueTarget.isEmpty()) {
                // key store is auto-generated
                if (valueReal == null || valueReal.isEmpty()) {
                    // not generated yet
                    File keyStoreFile = PlatformUtils.resolve("felix/conf/keystore.jks");
                    String password = SSLGenerator.generateKeyStore(keyStoreFile, "platform.xowl.org");
                    if (password == null) {
                        Logging.get().error("Failed to generate the keystore");
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
                valueTarget = platformHttp.getTlsKeyPassword();
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
                felixConfiguration.save(confFile);
            } catch (IOException exception) {
                Logging.get().error(exception);
                return true;
            }
        }
        return mustReboot;
    }
}
