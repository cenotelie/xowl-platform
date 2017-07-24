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

import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.api.ReplyUnsupported;
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
    public synchronized RemotePlatformAccess getAccess() {
        if (remotePlatform == null) {
            remotePlatform = descriptor.newAccess();
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
    public Reply getManifest() {
        return getAccess().getCollaborationManifest();
    }

    @Override
    public Reply getArtifactsForInput(String specificationId) {
        return getAccess().getArtifactsForCollaborationInput(specificationId);
    }

    @Override
    public Reply getArtifactsForOutput(String specificationId) {
        return getAccess().getArtifactsForCollaborationOutput(specificationId);
    }

    @Override
    public Reply archive() {
        return networkService.archive(identifier);
    }

    @Override
    public Reply restart() {
        return networkService.restart(identifier);
    }

    @Override
    public Reply delete() {
        return networkService.delete(identifier);
    }

    @Override
    public Reply retrieveOutput(String specificationId, String artifactId) {
        return ReplyUnsupported.instance();
    }
}
