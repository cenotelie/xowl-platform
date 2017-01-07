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

package org.xowl.platform.services.collaboration.network.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.infra.utils.product.Product;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.platform.ProductBase;
import org.xowl.platform.services.collaboration.network.CollaborationInstance;
import org.xowl.platform.services.collaboration.network.CollaborationProvisioner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a collaboration provisioner that directly manipulates the file system
 *
 * @author Laurent Wouters
 */
public class FileSystemProvisioner implements CollaborationProvisioner {
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
    private final Map<String, Product> platforms;
    /**
     * The managed instances
     */
    private final Map<String, FileSystemCollaborationInstance> instances;
    /**
     * The min in the available port range
     */
    private final int portMin;
    /**
     * The max in the available port range
     */
    private final int portMax;

    /**
     * Initializes this provisioner
     */
    public FileSystemProvisioner() {
        ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
        Configuration configuration = configurationService.getConfigFor(FileSystemProvisioner.class.getCanonicalName());
        File storage = new File(System.getenv(Env.ROOT), configuration.get("storage"));
        this.storageDistributions = new File(storage, "platforms");
        this.storageInstances = new File(storage, "instances");
        this.platforms = new HashMap<>();
        this.instances = new HashMap<>();
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
                        platforms.put(product.getIdentifier(), product);
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
                        FileSystemCollaborationInstance instance = new FileSystemCollaborationInstance(definition);
                        instances.put(instance.getIdentifier(), instance);
                    } catch (IOException exception) {
                        Logging.getDefault().error(exception);
                    }
                }
            }
        }
    }

    @Override
    public String getIdentifier() {
        return FileSystemProvisioner.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - File System Collaboration Provisioner";
    }

    @Override
    public Collection<Product> getAvailablePlatforms() {
        return Collections.unmodifiableCollection(platforms.values());
    }

    @Override
    public Collection<CollaborationInstance> getInstances() {
        return (Collection) Collections.unmodifiableCollection(instances.values());
    }

    @Override
    public XSPReply provision(String platformId) {
        return null;
    }

    @Override
    public XSPReply terminate(String instanceId) {
        return null;
    }
}
