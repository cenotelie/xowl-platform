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
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.SHA1;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.security.SecuredResource;
import org.xowl.platform.kernel.security.SecuredResourceDescriptorBase;
import org.xowl.platform.kernel.security.SecuredResourceManager;
import org.xowl.platform.kernel.security.SecuredResourceSharing;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * Represents the security descriptor of a secured resource for the kernel
 *
 * @author Laurent Wouters
 */
class KernelSecuredResourceDescriptor extends SecuredResourceDescriptorBase {
    /**
     * The storage location for this descriptor
     */
    private final File storage;

    /**
     * Initializes this descriptor
     *
     * @param resource The associated secured resource
     * @param storage  The storage location for this descriptor
     */
    public KernelSecuredResourceDescriptor(SecuredResource resource, File storage) {
        super(resource);
        this.storage = storage;
    }

    /**
     * Initializes this resource
     *
     * @param node    The descriptor node to load from
     * @param storage The storage location for this descriptor
     */
    public KernelSecuredResourceDescriptor(ASTNode node, File storage) {
        super(node);
        this.storage = storage;
    }

    /**
     * Adds an owner of this resource
     *
     * @param user The new owner for this resource
     * @return The protocol reply
     */
    public XSPReply addOwner(String user) {
        synchronized (owners) {
            if (owners.contains(user))
                return new XSPReplyApiError(SecuredResourceManager.ERROR_ALREADY_OWNER);
            owners.add(user);
            XSPReply reply = writeDescriptor();
            if (!reply.isSuccess())
                owners.remove(user);
            return reply;
        }
    }

    /**
     * Removes an owner of this resource
     *
     * @param user The previous owner for this resource
     * @return The protocol reply
     */
    public XSPReply removeOwner(String user) {
        synchronized (owners) {
            if (owners.size() == 1)
                return new XSPReplyApiError(SecuredResourceManager.ERROR_LAST_OWNER);
            boolean removed = owners.remove(user);
            if (!removed)
                return XSPReplyNotFound.instance();
            XSPReply reply = writeDescriptor();
            if (!reply.isSuccess())
                owners.add(user);
            return reply;
        }
    }

    /**
     * Adds a sharing for this resource
     *
     * @param sharing The sharing to add
     * @return The protocol reply
     */
    public XSPReply addSharing(SecuredResourceSharing sharing) {
        synchronized (this.sharing) {
            this.sharing.add(sharing);
            XSPReply reply = writeDescriptor();
            if (!reply.isSuccess())
                this.sharing.remove(sharing);
            return reply;
        }
    }

    /**
     * Remove a sharing for this resource
     *
     * @param sharing The sharing to remove
     * @return The protocol reply
     */
    public XSPReply removeSharing(SecuredResourceSharing sharing) {
        synchronized (this.sharing) {
            for (SecuredResourceSharing candidate : this.sharing) {
                if (candidate.equals(sharing)) {
                    this.sharing.remove(candidate);
                    XSPReply reply = writeDescriptor();
                    if (!reply.isSuccess())
                        this.sharing.add(candidate);
                    return reply;
                }
            }
        }
        return XSPReplyNotFound.instance();
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
     * Deletes the descriptor's file
     *
     * @return The protocol reply
     */
    public XSPReply deleteDescriptor() {
        File fileDescriptor = new File(storage, getFileName());
        if (fileDescriptor.exists() && !fileDescriptor.delete()) {
            Logging.get().error("Failed to delete descriptor file " + fileDescriptor.getAbsolutePath());
            return new XSPReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, "Failed to delete descriptor in storage");
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
}
