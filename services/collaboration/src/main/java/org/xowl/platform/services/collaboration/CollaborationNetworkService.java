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
     * @param identifier The identifier of the collaboration to retrieve
     * @return The collaboration, or null if it cannot be found
     */
    RemoteCollaboration getNeighbour(String identifier);

    /**
     * Spawns a new collaboration
     *
     * @param specification The specification for the collaboration
     * @return The protocol reply
     */
    XSPReply spawn(CollaborationSpecification specification);
}
