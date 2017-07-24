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

import org.xowl.infra.utils.api.Reply;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.Collection;

/**
 * Represents a service that manages the current collaboration that takes place on this instance of the xOWL Collaboration Platform
 *
 * @author Laurent Wouters
 */
public interface CollaborationService extends SecuredService, CollaborationLocalService, CollaborationNetworkService {
    /**
     * Service action to archive this collaboration
     */
    SecuredAction ACTION_ARCHIVE = new SecuredAction(CollaborationService.class.getCanonicalName() + ".Archive", "Collaboration Service - Archive Collaboration");
    /**
     * Service action to delete this collaboration
     */
    SecuredAction ACTION_DELETE = new SecuredAction(CollaborationService.class.getCanonicalName() + ".Delete", "Collaboration Service - Delete Collaboration");

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_ARCHIVE,
            ACTION_DELETE,
            ACTION_ADD_INPUT_SPEC,
            ACTION_REMOVE_INPUT_SPEC,
            ACTION_ADD_OUTPUT_SPEC,
            ACTION_REMOVE_OUTPUT_SPEC,
            ACTION_REGISTER_INPUT,
            ACTION_UNREGISTER_INPUT,
            ACTION_REGISTER_OUTPUT,
            ACTION_UNREGISTER_OUTPUT,
            ACTION_ADD_ROLE,
            ACTION_REMOVE_ROLE,
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
     * Stops and archive this collaboration
     *
     * @return The protocol reply
     */
    Reply archive();

    /**
     * Stops this collaboration and delete all its data
     *
     * @return The protocol reply
     */
    Reply delete();

    /**
     * Gets the known collaboration patterns
     *
     * @return The known collaboration patterns
     */
    Collection<CollaborationPatternDescriptor> getKnownPatterns();
}
