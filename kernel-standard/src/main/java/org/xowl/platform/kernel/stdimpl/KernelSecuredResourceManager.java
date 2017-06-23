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

package org.xowl.platform.kernel.stdimpl;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecuredResource;
import org.xowl.platform.kernel.security.SecuredResourceManager;
import org.xowl.platform.kernel.security.SecuredResourceSharing;
import org.xowl.platform.kernel.security.SecurityService;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Kernel implementation of a manager of secured resources
 *
 * @author Laurent Wouters
 */
public class KernelSecuredResourceManager implements SecuredResourceManager {
    /**
     * The storage for the configuration
     */
    private final File storage;
    /**
     * The map of descriptors
     */
    private Map<String, KernelSecuredResourceDescriptor> descriptors;

    /**
     * Initializes this manager
     *
     * @param configuration The configuration for the manager
     */
    public KernelSecuredResourceManager(Section configuration) {
        this.storage = PlatformUtils.resolve(configuration.get("storage"));
    }

    /**
     * Resolves the descriptors for this manager
     *
     * @return The descriptors
     */
    private Map<String, KernelSecuredResourceDescriptor> getDescriptors() {
        synchronized (this) {
            if (descriptors == null) {
                descriptors = new HashMap<>();
                if (storage.exists()) {
                    File[] files = storage.listFiles();
                    if (files != null) {
                        for (int i = 0; i != files.length; i++) {
                            KernelSecuredResourceDescriptor descriptor = loadDescriptor(files[i]);
                            if (descriptor != null)
                                descriptors.put(descriptor.getIdentifier(), descriptor);
                        }
                    }
                }
            }
            return descriptors;
        }
    }

    /**
     * Loads a descriptor form the specified file
     *
     * @param file A descriptor file
     * @return The descriptor
     */
    private KernelSecuredResourceDescriptor loadDescriptor(File file) {
        try (Reader reader = IOUtils.getReader(file.getAbsolutePath())) {
            String content = IOUtils.read(reader);
            ASTNode definition = JsonLoader.parseJson(Logging.get(), content);
            if (definition == null) {
                Logging.get().error("Failed to parse the descriptor " + file);
                return null;
            }
            return new KernelSecuredResourceDescriptor(definition, storage);
        } catch (IOException exception) {
            Logging.get().error(exception);
            return null;
        }
    }

    @Override
    public String getIdentifier() {
        return KernelSecuredResourceManager.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Secured Resource Manager";
    }

    @Override
    public XSPReply createDescriptorFor(SecuredResource resource) {
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource.getIdentifier());
        if (descriptor != null)
            return XSPReplyNotFound.instance();

        descriptor = new KernelSecuredResourceDescriptor(resource, storage);
        XSPReply reply = descriptor.writeDescriptor();
        if (!reply.isSuccess())
            return reply;
        getDescriptors().put(resource.getIdentifier(), descriptor);
        return new XSPReplyResult<>(reply);
    }

    @Override
    public XSPReply getDescriptorFor(String resource) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_GET_DESCRIPTOR, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        return new XSPReplyResult<>(descriptor);
    }

    @Override
    public XSPReply addOwner(String resource, String user) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        return descriptor.addOwner(user);
    }

    @Override
    public XSPReply removeOwner(String resource, String user) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        return descriptor.removeOwner(user);
    }

    @Override
    public XSPReply addSharing(String resource, SecuredResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        return descriptor.addSharing(sharing);
    }

    @Override
    public XSPReply removeSharing(String resource, SecuredResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        return descriptor.removeSharing(sharing);
    }

    @Override
    public XSPReply deleteDescriptorFor(String resource) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().remove(resource);
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        return descriptor.deleteDescriptor();
    }

    @Override
    public XSPReply checkIsResourceOwner(SecurityService securityService, PlatformUser user, String resource) {
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        if (descriptor.getOwners().contains(user.getIdentifier()))
            return new XSPReplyResult<>(descriptor);
        return XSPReplyUnauthorized.instance();
    }

    @Override
    public XSPReply checkIsInSharing(SecurityService securityService, PlatformUser user, String resource) {
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        if (descriptor.getOwners().contains(user.getIdentifier()))
            // resource owner can access the resource
            return XSPReplySuccess.instance();
        // look for a sharing of the resource matching the requesting user
        for (SecuredResourceSharing sharing : descriptor.getSharing()) {
            if (sharing.isAllowedAccess(securityService, user))
                return XSPReplySuccess.instance();
        }
        return XSPReplyUnauthorized.instance();
    }
}
