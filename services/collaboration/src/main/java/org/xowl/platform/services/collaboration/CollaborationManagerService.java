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
public interface CollaborationManagerService extends Service {
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
     * @param specification The specification of the input
     * @return The associated artifacts
     */
    Collection<Artifact> getInputFor(ArtifactSpecification specification);

    /**
     * Gets the available artifacts for a specific output
     *
     * @param specification The specification of the output
     * @return The associated artifacts
     */
    Collection<Artifact> getOutputFor(ArtifactSpecification specification);

    /**
     * Registers an artifact as an input for this collaboration
     *
     * @param specification The input specification that the artifact fulfills
     * @param artifact      The input artifact
     * @return The protocol reply
     */
    XSPReply addInput(ArtifactSpecification specification, Artifact artifact);

    /**
     * Registers an artifact as an output for this collaboration
     *
     * @param specification The output specification that the artifact fulfills
     * @param artifact      The output artifact
     * @return The protocol reply
     */
    XSPReply publishOutput(ArtifactSpecification specification, Artifact artifact);

    /**
     * Gets the roles for this collaboration
     *
     * @return The roles for this collaboration
     */
    Collection<PlatformRole> getRoles();

    /**
     * Gets the collaboration pattern for the orchestration of this collaboration
     *
     * @return The current collaboration pattern
     */
    CollaborationPattern getCollaborationPattern();
}
