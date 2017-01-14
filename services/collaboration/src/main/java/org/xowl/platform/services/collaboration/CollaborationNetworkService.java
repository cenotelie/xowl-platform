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
import org.xowl.platform.kernel.Service;

import java.util.Collection;

/**
 * Represents a service for the local access to a network of collaborations
 *
 * @author Laurent Wouters
 */
public interface CollaborationNetworkService extends Service {
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
     * Lookups, across the collaboration network, input and output specifications that matches in the specified input string
     *
     * @param input The string to match
     * @return The protocol reply
     */
    XSPReply lookupSpecifications(String input);

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
