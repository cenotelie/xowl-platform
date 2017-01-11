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

package org.xowl.platform.services.collaboration.network;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.services.collaboration.CollaborationSpecification;
import org.xowl.platform.services.collaboration.RemoteCollaboration;

import java.util.Collection;

/**
 * Represents a service for the management of a network of collaborations.
 * Each collaboration is implemented as an instance of the xOWL Collaboration Platform.
 *
 * @author Laurent Wouters
 */
public interface CollaborationNetworkService extends Service {
    /**
     * Gets the known collaborations
     *
     * @return The known collaborations
     */
    Collection<RemoteCollaboration> getCollaborations();

    /**
     * Gets a collaboration
     *
     * @param identifier The identifier of the collaboration to retrieve
     * @return The collaboration, or null if it cannot be found
     */
    RemoteCollaboration getCollaboration(String identifier);

    /**
     * Spawns a new collaboration
     *
     * @param specification The specification for the collaboration
     * @return The protocol reply
     */
    XSPReply spawn(CollaborationSpecification specification);

    /**
     * Stops and archive a collaboration
     *
     * @param collaborationId The identifier of the collaboration
     * @return The protocol reply
     */
    XSPReply archive(String collaborationId);

    /**
     * Un-archives and re-starts a collaboration
     *
     * @param collaborationId The identifier of the collaboration
     * @return The protocol reply
     */
    XSPReply restart(String collaborationId);

    /**
     * Stops a collaboration and delete all its data
     *
     * @param collaborationId The identifier of the collaboration
     * @return The protocol reply
     */
    XSPReply delete(String collaborationId);
}
