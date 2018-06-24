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

import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.api.*;
import fr.cenotelie.commons.utils.ini.IniSection;
import fr.cenotelie.commons.utils.json.Json;
import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ReplyServiceUnavailable;
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
    public KernelSecuredResourceManager(IniSection configuration) {
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
            ASTNode definition = Json.parse(Logging.get(), content);
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
    public Reply createDescriptorFor(SecuredResource resource) {
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource.getIdentifier());
        if (descriptor != null)
            return ReplyNotFound.instance();

        descriptor = new KernelSecuredResourceDescriptor(resource, storage);
        Reply reply = descriptor.writeDescriptor();
        if (!reply.isSuccess())
            return reply;
        getDescriptors().put(resource.getIdentifier(), descriptor);
        return new ReplyResult<>(reply);
    }

    @Override
    public Reply getDescriptorFor(String resource) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_GET_DESCRIPTOR, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return ReplyNotFound.instance();
        return new ReplyResult<>(descriptor);
    }

    @Override
    public Reply addOwner(String resource, String user) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return ReplyNotFound.instance();
        return descriptor.addOwner(user);
    }

    @Override
    public Reply removeOwner(String resource, String user) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return ReplyNotFound.instance();
        return descriptor.removeOwner(user);
    }

    @Override
    public Reply addSharing(String resource, SecuredResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return ReplyNotFound.instance();
        return descriptor.addSharing(sharing);
    }

    @Override
    public Reply removeSharing(String resource, SecuredResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return ReplyNotFound.instance();
        return descriptor.removeSharing(sharing);
    }

    @Override
    public Reply deleteDescriptorFor(String resource) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(SecurityService.ACTION_RESOURCE_MANAGE, resource);
        if (!reply.isSuccess())
            return reply;
        KernelSecuredResourceDescriptor descriptor = getDescriptors().remove(resource);
        if (descriptor == null)
            return ReplyNotFound.instance();
        return descriptor.deleteDescriptor();
    }

    @Override
    public Reply checkIsResourceOwner(SecurityService securityService, PlatformUser user, String resource) {
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return ReplyNotFound.instance();
        if (descriptor.getOwners().contains(user.getIdentifier()))
            return new ReplyResult<>(descriptor);
        return ReplyUnauthorized.instance();
    }

    @Override
    public Reply checkIsInSharing(SecurityService securityService, PlatformUser user, String resource) {
        KernelSecuredResourceDescriptor descriptor = getDescriptors().get(resource);
        if (descriptor == null)
            return ReplyNotFound.instance();
        if (descriptor.getOwners().contains(user.getIdentifier()))
            // resource owner can access the resource
            return ReplySuccess.instance();
        // look for a sharing of the resource matching the requesting user
        for (SecuredResourceSharing sharing : descriptor.getSharing()) {
            if (sharing.isAllowedAccess(securityService, user))
                return ReplySuccess.instance();
        }
        return ReplyUnauthorized.instance();
    }
}
