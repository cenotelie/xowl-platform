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
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;

/**
 * Represents a remote collaboration in a network of collaboration
 *
 * @author Laurent Wouters
 */
public interface RemoteCollaboration extends Identifiable, Serializable {
    /**
     * Gets the API endpoint
     *
     * @return The API endpoint
     */
    String getApiEndpoint();

    /**
     * Gets the status of this collaboration
     *
     * @return The status of this collaboration
     */
    CollaborationStatus getStatus();

    /**
     * Gets the manifest for this collaboration
     *
     * @return The manifest
     */
    XSPReply getManifest();

    /**
     * Gets the available artifacts for a specific input
     *
     * @param specificationId The identifier for the specification of the input
     * @return The associated artifacts
     */
    XSPReply getArtifactsForInput(String specificationId);

    /**
     * Gets the available artifacts for a specific output
     *
     * @param specificationId The identifier for the specification of the output
     * @return The associated artifacts
     */
    XSPReply getArtifactsForOutput(String specificationId);

    /**
     * Stops and archive this collaboration
     *
     * @return The protocol reply
     */
    XSPReply archive();

    /**
     * Un-archives and re-starts this collaboration
     *
     * @return The protocol reply
     */
    XSPReply restart();

    /**
     * Stops this collaboration and delete all its data
     *
     * @return The protocol reply
     */
    XSPReply delete();

    /**
     * Copies locally an output of the remote collaboration
     *
     * @param specificationId The identifier of the output specification
     * @param artifactId      The identifier of the artifact to retrieve
     * @return The protocol reply
     */
    XSPReply retrieveOutput(String specificationId, String artifactId);
}
