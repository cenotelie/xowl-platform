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
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;

import java.util.Collection;

/**
 * Represents a remote collaboration in a network of collaboration
 *
 * @author Laurent Wouters
 */
public interface RemoteCollaboration extends Identifiable, Serializable {
    /**
     * Gets the status of this collaboration
     *
     * @return The status of this collaboration
     */
    CollaborationStatus getStatus();

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
     * Gets the expected inputs for this collaboration
     *
     * @return The expected inputs
     */
    Collection<ArtifactSpecification> getInputSpecifications();

    /**
     * Gets the expected outputs for this collaboration
     *
     * @return The expected outputs
     */
    Collection<ArtifactSpecification> getOutputSpecifications();

    /**
     * Gets the available artifacts for a specific input
     *
     * @param specificationId The identifier for the specification of the input
     * @return The associated artifacts
     */
    XSPReply getInputFor(String specificationId);

    /**
     * Gets the available artifacts for a specific output
     *
     * @param specificationId The identifier for the specification of the output
     * @return The associated artifacts
     */
    XSPReply getOutputFor(String specificationId);

    /**
     * Sends a local artifact as an input to the remote collaboration
     *
     * @param specificationId The identifier of the input specification that the artifact fulfills
     * @param artifactId      The identifier of the artifact to send
     * @return The protocol reply
     */
    XSPReply sendInput(String specificationId, String artifactId);

    /**
     * Retrieves the remote artifact that fulfills an output specification and copies it locally
     *
     * @param specificationId The identifier of the output specification
     * @param artifactId      The identifier of the artifact to retrieve
     * @return The protocol reply
     */
    XSPReply retrieveOutput(String specificationId, String artifactId);
}
