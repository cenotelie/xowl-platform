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
import org.xowl.infra.utils.logging.Logging;
import org.xowl.infra.utils.product.Product;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.platform.ProductBase;
import org.xowl.platform.services.collaboration.CollaborationNetworkService;
import org.xowl.platform.services.collaboration.CollaborationSpecification;
import org.xowl.platform.services.collaboration.CollaborationStatus;
import org.xowl.platform.services.collaboration.RemoteCollaboration;

import java.io.*;
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
     * The comparator for platform instances
     */
    private static final Comparator<FSCollaborationInstance> COMPARATOR = new Comparator<FSCollaborationInstance>() {
        @Override
        public int compare(FSCollaborationInstance instance1, FSCollaborationInstance instance2) {
            return Integer.compare(instance1.getPort(), instance2.getPort());
        }
    };

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
     * The managed platform instances
     */
    private final List<FSCollaborationInstance> instances;
    /**
     * The managed collaboration instances
     */
    private final Map<String, RemoteCollaborationBase> collaborations;
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
     */
    public MasterNetworkService() {
        ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
        Configuration configuration = configurationService.getConfigFor(MasterNetworkService.class.getCanonicalName());
        File storage = new File(System.getenv(Env.ROOT), configuration.get("storage"));
        this.storageDistributions = new File(storage, "platforms");
        this.storageInstances = new File(storage, "instances");
        this.platforms = new ArrayList<>();
        this.instances = new ArrayList<>();
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
                        FSCollaborationInstance instance = new FSCollaborationInstance(definition);
                        instances.add(instance);
                        collaborations.put(instance.getIdentifier(), new RemoteCollaborationBase(instance.getIdentifier(), instance.getName(), instance.getEndpoint(), this));
                    } catch (IOException exception) {
                        Logging.getDefault().error(exception);
                    }
                }
            }
        }
        Collections.sort(instances, COMPARATOR);
    }

    @Override
    public String getIdentifier() {
        return MasterNetworkService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - Collaboration Network Service (Master)";
    }

    @Override
    public Collection<RemoteCollaboration> getNeighbours() {
        return Collections.emptyList();
    }

    @Override
    public RemoteCollaboration getNeighbour(String collaborationId) {
        return null;
    }

    @Override
    public CollaborationStatus getNeighbourStatus(String collaborationId) {
        return CollaborationStatus.Invalid;
    }

    @Override
    public XSPReply getNeighbourManifest(String collaborationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply getNeighbourInputsFor(String collaborationId, String specificationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply getNeighbourOutputsFor(String collaborationId, String specificationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public Collection<ArtifactSpecification> getKnownIOSpecifications() {
        return Collections.emptyList();
    }

    @Override
    public XSPReply spawn(CollaborationSpecification specification) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply archive(String collaborationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply restart(String collaborationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply delete(String collaborationId) {
        return XSPReplyNotFound.instance();
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
        FSCollaborationInstance instance = provisionCreateInstance(specification);
        RemoteCollaborationBase collaboration = new RemoteCollaborationBase(instance.getIdentifier(), instance.getName(), instance.getEndpoint(), this);
        collaborations.put(instance.getIdentifier(), collaboration);
        // extract the distribution
        reply = provisionExtractDistribution(product.getIdentifier(), instance.getIdentifier());
        if (!reply.isSuccess())
            return reply;
        // deploy the configuration
        reply = provisionDeployConfiguration(instance);
        if (!reply.isSuccess())
            return reply;
        // launch the platform
        reply = provisionLaunchPlatform(instance);
        if (!reply.isSuccess())
            return reply;
        // write the instance descriptor file
        instance.setStatus(CollaborationStatus.Running);
        reply = provisionWriteDescriptor(instance);
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
    private FSCollaborationInstance provisionCreateInstance(CollaborationSpecification specification) {
        synchronized (instances) {
            int port = provisionReservePort();
            FSCollaborationInstance instance = new FSCollaborationInstance(
                    UUID.randomUUID().toString(),
                    specification.getName(),
                    "https://localhost:" + Integer.toString(port) + "/api",
                    CollaborationStatus.Provisioning,
                    port);
            instances.add(instance);
            Collections.sort(instances, COMPARATOR);
            return instance;
        }
    }

    /**
     * Reserves a port for a new platform instance
     *
     * @return The port
     */
    private int provisionReservePort() {
        if (instances.isEmpty())
            return portMin;
        int max = instances.get(instances.size() - 1).getPort();
        if (max != portMax)
            return max + 1;
        int min = instances.get(0).getPort();
        if (min != portMin)
            return portMin;
        int current = portMin;
        for (int i = 1; i != instances.size() - 1; i++) {
            int x = instances.get(i).getPort();
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
    private XSPReply provisionWriteDescriptor(FSCollaborationInstance instance) {
        File fileDescriptor = new File(storageInstances, instance.getIdentifier() + ".json");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileDescriptor), Files.CHARSET)) {
            writer.write(instance.serializedJSON());
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
            return XSPReplyNotFound.instance();
        File targetDir = new File(storageInstances, instanceId);
        if (targetDir.exists())
            Files.deleteFolder(targetDir);
        if (!targetDir.mkdirs()) {
            Logging.getDefault().error("Failed to create directory " + targetDir.getAbsolutePath());
            return XSPReplyNotFound.instance();
        }
        try {
            provisionExtractTarGz(distributionFile, targetDir);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }


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
     * @param instance The instance being provisioned
     * @return The protocol reply
     */
    private XSPReply provisionDeployConfiguration(FSCollaborationInstance instance) {
        return XSPReplySuccess.instance();
    }

    /**
     * Launches the platform for a new platform instance
     *
     * @param instance The instance being provisioned
     * @return The protocol reply
     */
    private XSPReply provisionLaunchPlatform(FSCollaborationInstance instance) {
        return XSPReplySuccess.instance();
    }
}
