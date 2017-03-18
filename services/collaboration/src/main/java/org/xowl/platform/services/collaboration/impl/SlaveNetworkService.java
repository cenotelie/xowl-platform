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
import org.xowl.infra.server.xsp.XSPReplyNetworkError;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.utils.config.Section;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.remote.RemotePlatformAccess;
import org.xowl.platform.kernel.remote.RemotePlatformAccessProvider;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.services.collaboration.CollaborationNetworkService;
import org.xowl.platform.services.collaboration.CollaborationSpecification;
import org.xowl.platform.services.collaboration.CollaborationStatus;
import org.xowl.platform.services.collaboration.RemoteCollaboration;

import java.util.Collection;
import java.util.Collections;

/**
 * Implements a collaboration network service that delegates the network management to a master platform
 *
 * @author Laurent Wouters
 */
public class SlaveNetworkService implements CollaborationNetworkService {
    /**
     * Initializes this service
     *
     * @param configuration The configuration for this service
     */
    public SlaveNetworkService(Section configuration) {
    }

    @Override
    public String getIdentifier() {
        return SlaveNetworkService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Collaboration Network Service (Slave)";
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
        if (!(securityService.getRealm() instanceof RemotePlatformAccessProvider))
            return Collections.emptyList();
        RemotePlatformAccess remotePlatform = ((RemotePlatformAccessProvider) securityService.getRealm()).getAccess(securityService.getCurrentUser().getIdentifier());
        if (remotePlatform == null)
            return Collections.emptyList();
        reply = remotePlatform.getCollaborationNeighbours();
        if (!reply.isSuccess())
            return Collections.emptyList();
        return ((XSPReplyResultCollection<RemoteCollaboration>) reply).getData();
    }

    @Override
    public RemoteCollaboration getNeighbour(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOURS);
        if (!reply.isSuccess())
            return null;
        if (!(securityService.getRealm() instanceof RemotePlatformAccessProvider))
            return null;
        RemotePlatformAccess remotePlatform = ((RemotePlatformAccessProvider) securityService.getRealm()).getAccess(securityService.getCurrentUser().getIdentifier());
        if (remotePlatform == null)
            return null;
        reply = remotePlatform.getCollaborationNeighbour(collaborationId);
        if (!reply.isSuccess())
            return null;
        return ((XSPReplyResult<RemoteCollaboration>) reply).getData();
    }

    @Override
    public CollaborationStatus getNeighbourStatus(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return CollaborationStatus.Invalid;
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOURS);
        if (!reply.isSuccess())
            return CollaborationStatus.Invalid;
        return CollaborationStatus.Invalid;
    }

    @Override
    public XSPReply getNeighbourManifest(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOUR_MANIFEST);
        if (!reply.isSuccess())
            return reply;
        if (!(securityService.getRealm() instanceof RemotePlatformAccessProvider))
            return XSPReplyNetworkError.instance();
        RemotePlatformAccess remotePlatform = ((RemotePlatformAccessProvider) securityService.getRealm()).getAccess(securityService.getCurrentUser().getIdentifier());
        if (remotePlatform == null)
            return XSPReplyNetworkError.instance();
        return remotePlatform.getCollaborationNeighbourManifest(collaborationId);
    }

    @Override
    public XSPReply getNeighbourInputsFor(String collaborationId, String specificationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOUR_INPUTS);
        if (!reply.isSuccess())
            return reply;
        if (!(securityService.getRealm() instanceof RemotePlatformAccessProvider))
            return XSPReplyNetworkError.instance();
        RemotePlatformAccess remotePlatform = ((RemotePlatformAccessProvider) securityService.getRealm()).getAccess(securityService.getCurrentUser().getIdentifier());
        if (remotePlatform == null)
            return XSPReplyNetworkError.instance();
        return remotePlatform.getCollaborationNeighbourInputs(collaborationId, specificationId);
    }

    @Override
    public XSPReply getNeighbourOutputsFor(String collaborationId, String specificationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_NEIGHBOUR_OUTPUTS);
        if (!reply.isSuccess())
            return reply;
        if (!(securityService.getRealm() instanceof RemotePlatformAccessProvider))
            return XSPReplyNetworkError.instance();
        RemotePlatformAccess remotePlatform = ((RemotePlatformAccessProvider) securityService.getRealm()).getAccess(securityService.getCurrentUser().getIdentifier());
        if (remotePlatform == null)
            return XSPReplyNetworkError.instance();
        return remotePlatform.getCollaborationNeighbourOutputs(collaborationId, specificationId);
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
        if (!(securityService.getRealm() instanceof RemotePlatformAccessProvider))
            return XSPReplyNetworkError.instance();
        RemotePlatformAccess remotePlatform = ((RemotePlatformAccessProvider) securityService.getRealm()).getAccess(securityService.getCurrentUser().getIdentifier());
        if (remotePlatform == null)
            return XSPReplyNetworkError.instance();
        return remotePlatform.spawnCollaboration(specification);
    }

    @Override
    public XSPReply archive(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_NETWORK_ARCHIVE);
        if (!reply.isSuccess())
            return reply;
        if (!(securityService.getRealm() instanceof RemotePlatformAccessProvider))
            return XSPReplyNetworkError.instance();
        RemotePlatformAccess remotePlatform = ((RemotePlatformAccessProvider) securityService.getRealm()).getAccess(securityService.getCurrentUser().getIdentifier());
        if (remotePlatform == null)
            return XSPReplyNetworkError.instance();
        return remotePlatform.archiveCollaborationNeighbour(collaborationId);
    }

    @Override
    public XSPReply restart(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_NETWORK_RESTART);
        if (!reply.isSuccess())
            return reply;
        if (!(securityService.getRealm() instanceof RemotePlatformAccessProvider))
            return XSPReplyNetworkError.instance();
        RemotePlatformAccess remotePlatform = ((RemotePlatformAccessProvider) securityService.getRealm()).getAccess(securityService.getCurrentUser().getIdentifier());
        if (remotePlatform == null)
            return XSPReplyNetworkError.instance();
        return remotePlatform.restartCollaborationNeighbour(collaborationId);
    }

    @Override
    public XSPReply delete(String collaborationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_NETWORK_DELETE);
        if (!reply.isSuccess())
            return reply;
        if (!(securityService.getRealm() instanceof RemotePlatformAccessProvider))
            return XSPReplyNetworkError.instance();
        RemotePlatformAccess remotePlatform = ((RemotePlatformAccessProvider) securityService.getRealm()).getAccess(securityService.getCurrentUser().getIdentifier());
        if (remotePlatform == null)
            return XSPReplyNetworkError.instance();
        return remotePlatform.deleteCollaborationNeighbour(collaborationId);
    }

    @Override
    public int getShutdownPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void close() {
        // do nothing
    }
}
