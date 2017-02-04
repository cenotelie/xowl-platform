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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.platform.kernel.remote.RemotePlatformAccess;
import org.xowl.platform.services.collaboration.CollaborationNetworkService;
import org.xowl.platform.services.collaboration.CollaborationStatus;

/**
 * Represents a remote collaboration managed by the current master platform
 *
 * @author Laurent Wouters
 */
public class RemoteCollaborationManaged extends RemoteCollaborationBase {
    /**
     * The parent network service
     */
    private final CollaborationNetworkService networkService;
    /**
     * The descriptor for this remote platform
     */
    private final RemoteCollaborationManagedDescriptor descriptor;
    /**
     * The remote platform endpoint
     */
    private RemotePlatformAccess remotePlatform;

    /**
     * Gets the descriptor for this remote platform
     *
     * @return The descriptor for this remote platform
     */
    public RemoteCollaborationManagedDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Gets the remote platform instance so that API call can be performed
     *
     * @return The remote platform
     */
    public synchronized RemotePlatformAccess getRemotePlatform() {
        if (remotePlatform == null) {
            remotePlatform = descriptor.createRemotePlatform();
        }
        return remotePlatform;
    }

    /**
     * Initializes this remote collaboration
     *
     * @param networkService The parent network service
     * @param descriptor     The descriptor for this remote platform
     */
    public RemoteCollaborationManaged(CollaborationNetworkService networkService, RemoteCollaborationManagedDescriptor descriptor) {
        super(descriptor.getIdentifier(), descriptor.getName(), descriptor.getEndpoint());
        this.networkService = networkService;
        this.descriptor = descriptor;
    }

    @Override
    public CollaborationStatus getStatus() {
        return descriptor.getStatus();
    }

    @Override
    public XSPReply getManifest() {
        return getRemotePlatform().getCollaborationManifest();
    }

    @Override
    public XSPReply getArtifactsForInput(String specificationId) {
        return getRemotePlatform().getArtifactsForCollaborationInput(specificationId);
    }

    @Override
    public XSPReply getArtifactsForOutput(String specificationId) {
        return getRemotePlatform().getArtifactsForCollaborationOutput(specificationId);
    }

    @Override
    public XSPReply archive() {
        return networkService.archive(identifier);
    }

    @Override
    public XSPReply restart() {
        return networkService.restart(identifier);
    }

    @Override
    public XSPReply delete() {
        return networkService.delete(identifier);
    }

    @Override
    public XSPReply retrieveOutput(String specificationId, String artifactId) {
        return XSPReplyUnsupported.instance();
    }
}
