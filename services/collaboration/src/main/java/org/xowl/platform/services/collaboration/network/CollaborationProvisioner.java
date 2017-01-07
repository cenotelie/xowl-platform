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
import org.xowl.infra.utils.product.Product;
import org.xowl.platform.kernel.Service;

import java.util.Collection;

/**
 * Represents an entity that manages platform distributions
 *
 * @author Laurent Wouters
 */
public interface CollaborationProvisioner extends Service {
    /**
     * Gets a list of the available platform distribution that can be used for spawning new collaborations
     *
     * @return The list of the available platform distributions
     */
    Collection<Product> getAvailablePlatforms();

    /**
     * Gets the list of the deployed platform instances
     *
     * @return The list of the deployed platform instances
     */
    Collection<CollaborationInstance> getInstances();

    /**
     * Provisions a platform for a new collaboration
     *
     * @param platformId The identifier of the requested platform product for the collaboration
     * @return The protocol reply
     */
    XSPReply provision(String platformId);

    /**
     * Terminates a collaboration
     *
     * @param instanceId The identifier of the collaboration to terminate
     * @return The protocol reply
     */
    XSPReply terminate(String instanceId);
}
