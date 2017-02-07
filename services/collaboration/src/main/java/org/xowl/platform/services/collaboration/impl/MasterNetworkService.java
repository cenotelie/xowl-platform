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

package org.xowl.platform.services.collaboration.impl;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.infra.utils.product.Product;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.ProductBase;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.services.collaboration.*;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Implements a collaboration network service that manages its own network of collaborations
 *
 * @author Laurent Wouters
 */
public class MasterNetworkService implements CollaborationNetworkService {
    /**
     * File mode for executable files in a tar package
     */
    private static final int EXECUTABLE_MODE = 0100755;
    /**
     * The login for the master manager
     */
    private static final String MASTER_LOGIN = "admin";

    /**
     * The storage area for the platform distributions
     */
    private final File storageDistributions;
    /**
     * The storage area for the instances
     */
    private final File storageInstances;
    /**
     * The known platforms
     */
    private final Collection<Product> platforms;
    /**
     * The managed collaboration instances
     */
    private final Map<String, RemoteCollaborationManaged> collaborations;
    /**
     * The min in the available port range
     */
    private final int portMin;
    /**
     * The max in the available port range
     */
    private final int portMax;

    /**
     * Initializes this service
     *
     * @param configuration The configuration for this service
     */
    public MasterNetworkService(Section configuration) {
        File storage = new File(System.getenv(Env.ROOT), configuration.get("storage"));
        this.storageDistributions = new File(storage, "platforms");
        this.storageInstances = new File(storage, "instances");
        this.platforms = new ArrayList<>();
        this.collaborations = new HashMap<>();
        this.portMin = Integer.parseInt(configuration.get("portMin"));
        this.portMax = Integer.parseInt(configuration.get("portMax"));
        File[] files = storageDistributions.listFiles();
        if (files != null) {
            for (int i = 0; i != files.length; i++) {
                if (files[i].getName().endsWith(".json")) {
                    try (InputStream stream = new FileInputStream(files[i])) {
                        String content = Files.read(stream, Files.CHARSET);
                        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
                        Product product = new ProductBase(definition);
                        platforms.add(product);
                    } catch (IOException exception) {
                        Logging.getDefault().error(exception);
                    }
                }
            }
        }
        files = storageInstances.listFiles();
        if (files != null) {
            for (int i = 0; i != files.length; i++) {
                if (files[i].getName().endsWith(".json")) {
                    try (InputStream stream = new FileInputStream(files[i])) {
                        String content = Files.read(stream, Files.CHARSET);
                        ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
                        RemoteCollaborationManagedDescriptor descriptor = new RemoteCollaborationManagedDescriptor(definition);
                        collaborations.put(descriptor.getIdentifier(), new RemoteCollaborationManaged(this, descriptor));
                    } catch (IOException exception) {
                        Logging.getDefault().error(exception);
                    }
                }
            }
        }
    }

    @Override
    public String getIdentifier() {
        return MasterNetworkService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Collaboration Network Service (Master)";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS_NETWORK;
    }

    @Override
    public Collection<RemoteCollaboration> getNeighbours() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return Collections.emptyList();
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOURS);
        if (!reply.isSuccess())
            return Collections.emptyList();
        return Collections.unmodifiableCollection((Collection) collaborations.values());
    }

    @Override
    public RemoteCollaboration getNeighbour(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOURS);
        if (!reply.isSuccess())
            return null;
        return collaborations.get(collaborationId);
    }

    @Override
    public CollaborationStatus getNeighbourStatus(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return CollaborationStatus.Invalid;
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOURS);
        if (!reply.isSuccess())
            return CollaborationStatus.Invalid;
        RemoteCollaborationManaged neighbour = collaborations.get(collaborationId);
        if (neighbour == null)
            return CollaborationStatus.Invalid;
        return neighbour.getStatus();
    }

    @Override
    public XSPReply getNeighbourManifest(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOUR_MANIFEST);
        if (!reply.isSuccess())
            return reply;
        RemoteCollaborationManaged neighbour = collaborations.get(collaborationId);
        if (neighbour == null)
            return XSPReplyNotFound.instance();
        return neighbour.getManifest();
    }

    @Override
    public XSPReply getNeighbourInputsFor(String collaborationId, String specificationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOUR_INPUTS);
        if (!reply.isSuccess())
            return reply;
        RemoteCollaborationManaged neighbour = collaborations.get(collaborationId);
        if (neighbour == null)
            return XSPReplyNotFound.instance();
        return neighbour.getArtifactsForInput(specificationId);
    }

    @Override
    public XSPReply getNeighbourOutputsFor(String collaborationId, String specificationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOUR_OUTPUTS);
        if (!reply.isSuccess())
            return reply;
        RemoteCollaborationManaged neighbour = collaborations.get(collaborationId);
        if (neighbour == null)
            return XSPReplyNotFound.instance();
        return neighbour.getArtifactsForOutput(specificationId);
    }

    @Override
    public Collection<ArtifactSpecification> getKnownIOSpecifications() {
        return Collections.emptyList();
    }

    @Override
    public XSPReply spawn(CollaborationSpecification specification) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_NETWORK_SPAWN);
        if (!reply.isSuccess())
            return reply;
        return provision(specification);
    }

    @Override
    public XSPReply archive(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_NETWORK_ARCHIVE);
        if (!reply.isSuccess())
            return reply;
        RemoteCollaborationManaged neighbour = collaborations.get(collaborationId);
        if (neighbour == null)
            return XSPReplyNotFound.instance();
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply restart(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_NETWORK_RESTART);
        if (!reply.isSuccess())
            return reply;
        RemoteCollaborationManaged neighbour = collaborations.get(collaborationId);
        if (neighbour == null)
            return XSPReplyNotFound.instance();
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply delete(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_NETWORK_DELETE);
        if (!reply.isSuccess())
            return reply;
        RemoteCollaborationManaged neighbour = collaborations.get(collaborationId);
        if (neighbour == null)
            return XSPReplyNotFound.instance();
        return XSPReplyUnsupported.instance();
    }

    /**
     * Provision o platform instance for a collaboration
     *
     * @param specification The specification for the collaboration
     * @return The protocol reply
     */
    public XSPReply provision(CollaborationSpecification specification) {
        XSPReply reply = provisionChooseProductFor(specification);
        if (!reply.isSuccess())
            return reply;
        Product product = ((XSPReplyResult<Product>) reply).getData();
        // provision the instance objects
        RemoteCollaborationManaged collaboration = provisionCreateInstance(specification);
        // extract the distribution
        reply = provisionExtractDistribution(product.getIdentifier(), collaboration.getIdentifier());
        if (!reply.isSuccess())
            return reply;
        // deploy the configuration
        reply = provisionDeployConfiguration(collaboration, specification);
        if (!reply.isSuccess())
            return reply;
        // launch the platform
        reply = provisionLaunchPlatform(collaboration);
        if (!reply.isSuccess())
            return reply;
        // write the instance descriptor file
        collaboration.getDescriptor().setStatus(CollaborationStatus.Running);
        reply = provisionWriteDescriptor(collaboration);
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(collaboration);
    }

    /**
     * Chooses the platform that will implementation the collaboration
     *
     * @param specification The specification for the collaboration
     * @return The protocol reply
     */
    private XSPReply provisionChooseProductFor(CollaborationSpecification specification) {
        if (platforms.isEmpty())
            return XSPReplyNotFound.instance();
        return new XSPReplyResult<>(platforms.iterator().next());
    }

    /**
     * Creates the platform instance object
     *
     * @param specification The specification for the collaboration
     * @return The instance
     */
    private RemoteCollaborationManaged provisionCreateInstance(CollaborationSpecification specification) {
        String masterPassword = "admin";
        synchronized (collaborations) {
            int port = provisionReservePort();
            RemoteCollaborationManagedDescriptor descriptor = new RemoteCollaborationManagedDescriptor(
                    UUID.randomUUID().toString(),
                    specification.getName(),
                    "https://localhost:" + Integer.toString(port) + "/api",
                    port,
                    MASTER_LOGIN,
                    masterPassword);
            RemoteCollaborationManaged collaboration = new RemoteCollaborationManaged(this, descriptor);
            collaborations.put(descriptor.getIdentifier(), collaboration);
            return collaboration;
        }
    }

    /**
     * Reserves a port for a new platform instance
     *
     * @return The port
     */
    private int provisionReservePort() {
        List<RemoteCollaborationManaged> instances = new ArrayList<>(collaborations.values());
        if (instances.isEmpty())
            return portMin;
        int max = instances.get(instances.size() - 1).getDescriptor().getPort();
        if (max != portMax)
            return max + 1;
        int min = instances.get(0).getDescriptor().getPort();
        if (min != portMin)
            return portMin;
        int current = portMin;
        for (int i = 1; i != instances.size() - 1; i++) {
            int x = instances.get(i).getDescriptor().getPort();
            if (x > current + 1)
                return current + 1;
            current = x;
        }
        return portMin;
    }

    /**
     * Writes the platform instance descriptor
     *
     * @param instance The platform instance
     * @return The protocol reply
     */
    private XSPReply provisionWriteDescriptor(RemoteCollaborationManaged instance) {
        File fileDescriptor = new File(storageInstances, instance.getIdentifier() + ".json");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileDescriptor), Files.CHARSET)) {
            writer.write(instance.getDescriptor().serializedJSON());
            writer.flush();
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
        return XSPReplySuccess.instance();
    }

    /**
     * Extract a platform distribution
     *
     * @param productId  The identifier of the product distribution
     * @param instanceId The identifier of the instance being provisioned
     * @return The protocol reply
     */
    private XSPReply provisionExtractDistribution(String productId, String instanceId) {
        File distributionFile = new File(storageDistributions, productId + ".tar.gz");
        if (!distributionFile.exists())
            return new XSPReplyFailure("Failed to find the distribution " + productId);
        File extractionDirectory = new File(storageInstances, instanceId + "_provision");
        if (extractionDirectory.exists())
            Files.deleteFolder(extractionDirectory);
        if (!extractionDirectory.mkdirs()) {
            Logging.getDefault().error("Failed to create directory " + extractionDirectory.getAbsolutePath());
            return XSPReplyNotFound.instance();
        }
        try {
            provisionExtractTarGz(distributionFile, extractionDirectory);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
        File[] children = extractionDirectory.listFiles();
        if (children == null || children.length != 1) {
            Logging.getDefault().error("Unexpected distribution content for " + productId);
            return XSPReplyNotFound.instance();
        }
        File target = new File(storageInstances, instanceId);
        if (target.exists())
            Files.deleteFolder(extractionDirectory);
        try {
            java.nio.file.Files.move(children[0].toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
        Files.deleteFolder(extractionDirectory);
        return XSPReplySuccess.instance();
    }

    /**
     * Extracts a tar.gz file
     *
     * @param input  The input tar.gz file
     * @param output The output directory
     */
    private void provisionExtractTarGz(File input, File output) throws IOException {
        byte[] buffer = new byte[8192];
        try (TarArchiveInputStream inputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(input)))) {
            while (true) {
                TarArchiveEntry entry = inputStream.getNextTarEntry();
                if (entry == null)
                    break;
                if (entry.isDirectory()) {
                    File directory = new File(output, entry.getName());
                    if (!directory.mkdirs())
                        throw new IOException("Failed to extract " + input.getAbsolutePath());
                } else {
                    File target = new File(output, entry.getName());
                    File directory = target.getParentFile();
                    if (!directory.exists() && !directory.mkdirs())
                        throw new IOException("Failed to extract " + input.getAbsolutePath());
                    try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
                        int read = 0;
                        while (read >= 0) {
                            read = inputStream.read(buffer, 0, buffer.length);
                            if (read > 0)
                                fileOutputStream.write(buffer, 0, read);
                        }
                    }
                    if (entry.getMode() == EXECUTABLE_MODE) {
                        if (!target.setExecutable(true, false))
                            throw new IOException("Failed to set executable bit on " + target.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Deploys the configuration for a new platform instance
     *
     * @param instance      The instance being provisioned
     * @param specification The specification for the collaboration
     * @return The protocol reply
     */
    private XSPReply provisionDeployConfiguration(RemoteCollaborationManaged instance, CollaborationSpecification specification) {
        File instanceDirectory = new File(storageInstances, instance.getIdentifier());

        // write the collaboration manifest
        File collaborationManifest = new File(instanceDirectory, "collaboration.json");
        CollaborationManifest manifest = new CollaborationManifest(instance.getIdentifier(), specification);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(collaborationManifest), Files.CHARSET)) {
            writer.write(manifest.serializedJSON());
            writer.flush();
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }

        // write the platform management service configuration
        File instanceConfigDir = new File(instanceDirectory, "config");
        File servicePlatformConfigFile = new File(instanceConfigDir, PlatformManagementService.class.getCanonicalName() + ".ini");
        Configuration configuration = new Configuration();
        try {
            configuration.load(servicePlatformConfigFile);
            configuration.set("httpsPort", Integer.toString(instance.getDescriptor().getPort()));
            configuration.save(servicePlatformConfigFile);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }

        // write the collaboration service configuration
        File serviceCollabConfigFile = new File(instanceConfigDir, CollaborationService.class.getCanonicalName() + ".ini");
        configuration = new Configuration();
        try {
            configuration.load(serviceCollabConfigFile);
            configuration.set("manifest", "collaboration.json");
            configuration.set("network", "service", SlaveNetworkService.class.getCanonicalName());
            configuration.set("network", "master", "https://localhost:8443/api");
            configuration.save(serviceCollabConfigFile);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }

        // write the security service configuration
        File serviceSecurityConfigFile = new File(instanceConfigDir, SecurityService.class.getCanonicalName() + ".ini");
        configuration = new Configuration();
        try {
            configuration.load(serviceSecurityConfigFile);
            configuration.set("realm", "type", "org.xowl.platform.services.security.internal.XOWLSubordinateRealm");
            configuration.set("realm", "location", "users");
            configuration.set("realm", "master", "https://localhost:8443/api");
            configuration.save(serviceSecurityConfigFile);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
        return XSPReplySuccess.instance();
    }

    /**
     * Launches the platform for a new platform instance
     *
     * @param instance The instance being provisioned
     * @return The protocol reply
     */
    private XSPReply provisionLaunchPlatform(RemoteCollaborationManaged instance) {
        File instanceDirectory = new File(storageInstances, instance.getIdentifier());
        File adminScript = new File(instanceDirectory, "admin.sh");
        ProcessBuilder processBuilder = new ProcessBuilder("sh", adminScript.getAbsolutePath(), "start");
        processBuilder.directory(instanceDirectory);
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
        return XSPReplySuccess.instance();
    }
}
