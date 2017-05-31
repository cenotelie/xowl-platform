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

package org.xowl.platform.kernel.impl;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.SHA1;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Kernel implementation of a manager of secured resources
 *
 * @author Laurent Wouters
 */
class KernelSecuredResourceManager implements SecuredResourceManager {
    /**
     * The storage for the configuration
     */
    private final File storage;
    /**
     * The map of descriptors
     */
    private Map<String, KernelDescriptor> descriptors;

    /**
     * Represents a descriptor for a secured resource
     */
    private class KernelDescriptor extends SecuredResourceDescriptor {
        /**
         * Initializes this descriptor
         *
         * @param resource The associated secured resource
         */
        public KernelDescriptor(SecuredResource resource) {
            super(resource);
        }

        /**
         * Initializes this resource
         *
         * @param node The descriptor node to load from
         */
        public KernelDescriptor(ASTNode node) {
            super(node);
        }

        /**
         * Writes the descriptor to the storage
         *
         * @return The protocol reply
         */
        public XSPReply writeDescriptor() {
            if (!storage.exists() && !storage.mkdirs())
                return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to write descriptor in storage");
            File fileDescriptor = new File(storage, getFileName());
            try (Writer writer = IOUtils.getWriter(fileDescriptor)) {
                writer.write(serializedJSON());
                writer.flush();
            } catch (IOException exception) {
                Logging.get().error(exception);
                return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to write descriptor in storage");
            }
            return XSPReplySuccess.instance();
        }

        /**
         * Gets the descriptor file name for the document
         *
         * @return The file name
         */
        private String getFileName() {
            return SHA1.hashSHA1(getIdentifier()) + ".json";
        }

        @Override
        protected XSPReply onOwnerChanged(PlatformUser user, boolean added) {
            return writeDescriptor();
        }

        @Override
        protected XSPReply onSharingChanged(SecuredResourceSharing sharing, boolean added) {
            return writeDescriptor();
        }
    }

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
    private Map<String, KernelDescriptor> getDescriptors() {
        synchronized (this) {
            if (descriptors == null) {
                descriptors = new HashMap<>();
                if (storage.exists()) {
                    File[] files = storage.listFiles();
                    if (files != null) {
                        for (int i = 0; i != files.length; i++) {
                            KernelDescriptor descriptor = loadDescriptor(files[i]);
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
    private KernelDescriptor loadDescriptor(File file) {
        try (Reader reader = IOUtils.getReader(file.getAbsolutePath())) {
            String content = IOUtils.read(reader);
            ASTNode definition = JsonLoader.parseJson(Logging.get(), content);
            if (definition == null) {
                Logging.get().error("Failed to parse the descriptor " + file);
                return null;
            }
            return new KernelDescriptor(definition);
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
        KernelDescriptor descriptor = getDescriptors().get(resource.getIdentifier());
        if (descriptor != null)
            return XSPReplyNotFound.instance();

        descriptor = new KernelDescriptor(resource);
        XSPReply reply = descriptor.writeDescriptor();
        if (!reply.isSuccess())
            return reply;
        getDescriptors().put(resource.getIdentifier(), descriptor);
        return new XSPReplyResult<>(reply);
    }

    @Override
    public XSPReply getDescriptorFor(SecuredResource resource) {
        return checkIsResourceOwner(resource);
    }

    @Override
    public XSPReply deleteDescriptorFor(SecuredResource resource) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            return XSPReplyUnauthenticated.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_MANAGE_RESOURCE_DESCRIPTOR, this);
        if (!reply.isSuccess())
            return reply;
        KernelDescriptor descriptor = getDescriptors().get(resource.getIdentifier());
        File fileDescriptor = new File(storage, descriptor.getFileName());
        if (fileDescriptor.exists() && !fileDescriptor.delete())
            Logging.get().error("Failed to delete file " + fileDescriptor.getAbsolutePath());
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply checkIsResourceOwner(SecuredResource resource) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            return XSPReplyUnauthenticated.instance();
        KernelDescriptor descriptor = getDescriptors().get(resource.getIdentifier());
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        if (descriptor.getOwners().contains(user.getIdentifier()))
            return new XSPReplyResult<>(descriptor);
        return XSPReplyUnauthorized.instance();
    }

    @Override
    public XSPReply checkIsInSharing(SecuredResource resource) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        PlatformUser user = securityService.getCurrentUser();
        if (user == null)
            return XSPReplyUnauthenticated.instance();
        KernelDescriptor descriptor = getDescriptors().get(resource.getIdentifier());
        if (descriptor == null)
            return XSPReplyNotFound.instance();
        if (descriptor.getOwners().contains(user.getIdentifier()))
            // resource owner can access the resource
            return XSPReplySuccess.instance();
        // look for a sharing of the resource matching the requesting user
        for (SecuredResourceSharing sharing : descriptor.getSharings()) {
            if (sharing.isAllowedAccess(securityService, user))
                return XSPReplySuccess.instance();
        }
        return XSPReplyUnauthorized.instance();
    }
}
