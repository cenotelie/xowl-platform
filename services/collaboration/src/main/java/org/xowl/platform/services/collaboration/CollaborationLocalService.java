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
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.platform.PlatformRole;

import java.util.Collection;

/**
 * Represents a service that manages the current collaboration that takes place on this instance of the xOWL Collaboration Platform
 *
 * @author Laurent Wouters
 */
public interface CollaborationLocalService extends Service {
    /**
     * Gets the unique identifier of the local collaboration
     *
     * @return The collaboration's identifier
     */
    String getCollaborationIdentifier();

    /**
     * Gets the name of the local collaboration
     *
     * @return The collaboration's name
     */
    String getCollaborationName();

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
     * Adds a new specification for an input
     *
     * @param specification The specification
     * @return The protocol reply
     */
    XSPReply addInputSpecification(ArtifactSpecification specification);

    /**
     * Adds a new specification for an output
     *
     * @param specification The specification
     * @return The protocol reply
     */
    XSPReply addOutputSpecification(ArtifactSpecification specification);

    /**
     * Removes the specification of an input
     *
     * @param specificationId The identifier of the specification to remove
     * @return The protocol reply
     */
    XSPReply removeInputSpecification(String specificationId);

    /**
     * Removes the specification of an output
     *
     * @param specificationId The identifier of the specification to remove
     * @return The protocol reply
     */
    XSPReply removeOutputSpecification(String specificationId);

    /**
     * Gets the available artifacts (in this collaboration) for a specific input
     *
     * @param specificationId The identifier of the input specification
     * @return The associated artifacts
     */
    Collection<Artifact> getInputsFor(String specificationId);

    /**
     * Gets the available artifacts (in this collaboration) for a specific output
     *
     * @param specificationId The identifier of the output specification
     * @return The associated artifacts
     */
    Collection<Artifact> getOutputsFor(String specificationId);

    /**
     * Registers an artifact as an input for this collaboration
     *
     * @param specificationId The identifier of the input specification that the artifact fulfills
     * @param artifactId      The identifier of the input artifact
     * @return The protocol reply
     */
    XSPReply registerInput(String specificationId, String artifactId);

    /**
     * Un-registers an artifact as an input for this collaboration
     *
     * @param specificationId The identifier of the input specification that the artifact fulfills
     * @param artifactId      The identifier of the input artifact
     * @return The protocol reply
     */
    XSPReply unregisterInput(String specificationId, String artifactId);

    /**
     * Registers an artifact as an output for this collaboration
     *
     * @param specificationId The identifier of the output specification that the artifact fulfills
     * @param artifactId      The identifier of the output artifact
     * @return The protocol reply
     */
    XSPReply registerOutput(String specificationId, String artifactId);

    /**
     * Un-registers an artifact as an output for this collaboration
     *
     * @param specificationId The identifier of the output specification that the artifact fulfills
     * @param artifactId      The identifier of the output artifact
     * @return The protocol reply
     */
    XSPReply unregisterOutput(String specificationId, String artifactId);

    /**
     * Gets the roles for this collaboration
     *
     * @return The roles for this collaboration
     */
    Collection<PlatformRole> getRoles();

    /**
     * Creates a role and registers it for this collaboration
     *
     * @param identifier The identifier for this role
     * @param name       The name for this role
     * @return The protocol reply
     */
    XSPReply createRole(String identifier, String name);

    /**
     * Registers a role for this collaboration
     *
     * @param roleId The identifier of the role
     * @return The protocol reply
     */
    XSPReply addRole(String roleId);

    /**
     * Unregisters a role from this collaboration
     *
     * @param roleId The identifier of the role
     * @return The protocol reply
     */
    XSPReply removeRole(String roleId);

    /**
     * Gets the descriptor of the collaboration pattern for the orchestration of this collaboration
     *
     * @return The descriptor of the current collaboration pattern
     */
    CollaborationPatternDescriptor getCollaborationPattern();
}
