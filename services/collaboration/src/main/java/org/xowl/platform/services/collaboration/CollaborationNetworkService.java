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

package org.xowl.platform.services.collaboration;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.utils.ApiError;
import org.xowl.platform.kernel.ManagedService;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.Collection;

/**
 * Represents a service for the local access to a network of collaborations
 *
 * @author Laurent Wouters
 */
public interface CollaborationNetworkService extends SecuredService, ManagedService {
    /**
     * Service action to get the neighbour collaborations
     */
    SecuredAction ACTION_GET_NEIGHBOURS = new SecuredAction(CollaborationNetworkService.class.getCanonicalName() + ".GetNeighbours", "Collaboration Network Service - Get Neighbour Collaborations");
    /**
     * Service action to get the manifest for a neighbour collaboration
     */
    SecuredAction ACTION_GET_NEIGHBOUR_MANIFEST = new SecuredAction(CollaborationNetworkService.class.getCanonicalName() + ".GetNeighbourManifest", "Collaboration Network Service - Get Neighbour Manifest");
    /**
     * Service action to get the input artifacts for a neighbour collaboration
     */
    SecuredAction ACTION_GET_NEIGHBOUR_INPUTS = new SecuredAction(CollaborationNetworkService.class.getCanonicalName() + ".GetNeighbourInputs", "Collaboration Network Service - Get Neighbour Inputs");
    /**
     * Service action to get the output artifacts for a neighbour collaboration
     */
    SecuredAction ACTION_GET_NEIGHBOUR_OUTPUTS = new SecuredAction(CollaborationNetworkService.class.getCanonicalName() + ".GetNeighbourOutputs", "Collaboration Network Service - Get Neighbour Outputs");
    /**
     * Service action to spawn a new collaboration in the network of collaborations
     */
    SecuredAction ACTION_NETWORK_SPAWN = new SecuredAction(CollaborationNetworkService.class.getCanonicalName() + ".NetworkSpawn", "Collaboration Network Service - Network Spawn Collaboration");
    /**
     * Service action to archive an existing collaboration in the network of collaborations
     */
    SecuredAction ACTION_NETWORK_ARCHIVE = new SecuredAction(CollaborationNetworkService.class.getCanonicalName() + ".NetworkArchive", "Collaboration Network Service - Network Archive Collaboration");
    /**
     * Service action to restart an archived collaboration in the network of collaborations
     */
    SecuredAction ACTION_NETWORK_RESTART = new SecuredAction(CollaborationNetworkService.class.getCanonicalName() + ".NetworkRestart", "Collaboration Network Service - Network Restart Collaboration");
    /**
     * Service action to delete an existing collaboration in the network of collaborations
     */
    SecuredAction ACTION_NETWORK_DELETE = new SecuredAction(CollaborationNetworkService.class.getCanonicalName() + ".NetworkDelete", "Collaboration Network Service - Network Delete Collaboration");

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS_NETWORK = new SecuredAction[]{
            ACTION_GET_NEIGHBOURS,
            ACTION_GET_NEIGHBOUR_MANIFEST,
            ACTION_GET_NEIGHBOUR_INPUTS,
            ACTION_GET_NEIGHBOUR_OUTPUTS,
            ACTION_NETWORK_SPAWN,
            ACTION_NETWORK_ARCHIVE,
            ACTION_NETWORK_RESTART,
            ACTION_NETWORK_DELETE
    };

    /**
     * API error - Another operation is already on-going for the collaboration
     */
    ApiError ERROR_COLLABORATION_BUSY = new ApiError(0x00000111,
            "Another operation is already on-going for the collaboration.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000111.html");

    /**
     * Gets the known neighbour collaborations
     *
     * @return The known neighbour collaborations
     */
    Collection<RemoteCollaboration> getNeighbours();

    /**
     * Gets a neighbour collaboration
     *
     * @param collaborationId The identifier of the collaboration to retrieve
     * @return The collaboration, or null if it cannot be found
     */
    RemoteCollaboration getNeighbour(String collaborationId);

    /**
     * Gets the status of a neighbour collaboration
     *
     * @param collaborationId The identifier of the collaboration
     * @return The collaboration's status
     */
    CollaborationStatus getNeighbourStatus(String collaborationId);

    /**
     * Gets the manifest for a neighbour collaboration
     *
     * @param collaborationId The identifier of the collaboration
     * @return The protocol reply
     */
    XSPReply getNeighbourManifest(String collaborationId);

    /**
     * Gets the available artifacts for a specific input, for a neighbour collaboration
     *
     * @param collaborationId The identifier of the collaboration
     * @param specificationId The identifier of the input specification
     * @return The associated artifacts
     */
    XSPReply getNeighbourInputsFor(String collaborationId, String specificationId);

    /**
     * Gets the available artifacts for a specific output, for a neighbour collaboration
     *
     * @param collaborationId The identifier of the collaboration
     * @param specificationId The identifier of the output specification
     * @return The associated artifacts
     */
    XSPReply getNeighbourOutputsFor(String collaborationId, String specificationId);

    /**
     * Gets the known specifications for collaboration inputs and outputs across the network of collaborations
     *
     * @return The known specifications
     */
    Collection<ArtifactSpecification> getKnownIOSpecifications();

    /**
     * Spawns a new collaboration
     *
     * @param specification The specification for the collaboration
     * @return The protocol reply
     */
    XSPReply spawn(CollaborationSpecification specification);

    /**
     * Archives a running collaboration
     *
     * @param collaborationId The identifier of a collaboration
     * @return the protocol reply
     */
    XSPReply archive(String collaborationId);

    /**
     * Restarts an archived collaboration
     *
     * @param collaborationId The identifier of a collaboration
     * @return the protocol reply
     */
    XSPReply restart(String collaborationId);

    /**
     * Deletes a collaboration and its data
     *
     * @param collaborationId The identifier of a collaboration
     * @return the protocol reply
     */
    XSPReply delete(String collaborationId);
}
