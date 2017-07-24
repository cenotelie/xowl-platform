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
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.Collection;

/**
 * Represents a service that manages the current collaboration that takes place on this instance of the xOWL Collaboration Platform
 *
 * @author Laurent Wouters
 */
public interface CollaborationLocalService extends SecuredService {
    /**
     * Service action to add an input specification
     */
    SecuredAction ACTION_ADD_INPUT_SPEC = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".AddInputSpec", "Collaboration Local Service - Add Input Specification");
    /**
     * Service action to remove an input specification
     */
    SecuredAction ACTION_REMOVE_INPUT_SPEC = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".RemoveInputSpec", "Collaboration Local Service - Remove Input Specification");
    /**
     * Service action to add an output specification
     */
    SecuredAction ACTION_ADD_OUTPUT_SPEC = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".AddOutputSpec", "Collaboration Local Service - Add Output Specification");
    /**
     * Service action to remove an output specification
     */
    SecuredAction ACTION_REMOVE_OUTPUT_SPEC = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".RemoveOutputSpec", "Collaboration Local Service - Remove Output Specification");
    /**
     * Service action to register an artifact as an input
     */
    SecuredAction ACTION_REGISTER_INPUT = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".RegisterInput", "Collaboration Local Service - Register Input Artifact");
    /**
     * Service action to unregister an artifact as an input
     */
    SecuredAction ACTION_UNREGISTER_INPUT = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".UnregisterInput", "Collaboration Local Service - Unregister Input Artifact");
    /**
     * Service action to register an artifact as an output
     */
    SecuredAction ACTION_REGISTER_OUTPUT = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".RegisterOutput", "Collaboration Local Service - Register Output Artifact");
    /**
     * Service action to register an artifact as an output
     */
    SecuredAction ACTION_UNREGISTER_OUTPUT = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".UnregisterOutput", "Collaboration Local Service - Unregister Output Artifact");
    /**
     * Service action to add a collaboration role
     */
    SecuredAction ACTION_ADD_ROLE = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".AddRole", "Collaboration Local Service - Add Collaboration Role");
    /**
     * Service action to remove a collaboration role
     */
    SecuredAction ACTION_REMOVE_ROLE = new SecuredAction(CollaborationLocalService.class.getCanonicalName() + ".RemoveRole", "Collaboration Local Service - Remove Collaboration Role");

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS_LOCAL = new SecuredAction[]{
            ACTION_ADD_INPUT_SPEC,
            ACTION_REMOVE_INPUT_SPEC,
            ACTION_ADD_OUTPUT_SPEC,
            ACTION_REMOVE_OUTPUT_SPEC,
            ACTION_REGISTER_INPUT,
            ACTION_UNREGISTER_INPUT,
            ACTION_REGISTER_OUTPUT,
            ACTION_UNREGISTER_OUTPUT,
            ACTION_ADD_ROLE,
            ACTION_REMOVE_ROLE
    };

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
    Reply addInputSpecification(ArtifactSpecification specification);

    /**
     * Adds a new specification for an output
     *
     * @param specification The specification
     * @return The protocol reply
     */
    Reply addOutputSpecification(ArtifactSpecification specification);

    /**
     * Removes the specification of an input
     *
     * @param specificationId The identifier of the specification to remove
     * @return The protocol reply
     */
    Reply removeInputSpecification(String specificationId);

    /**
     * Removes the specification of an output
     *
     * @param specificationId The identifier of the specification to remove
     * @return The protocol reply
     */
    Reply removeOutputSpecification(String specificationId);

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
    Reply registerInput(String specificationId, String artifactId);

    /**
     * Un-registers an artifact as an input for this collaboration
     *
     * @param specificationId The identifier of the input specification that the artifact fulfills
     * @param artifactId      The identifier of the input artifact
     * @return The protocol reply
     */
    Reply unregisterInput(String specificationId, String artifactId);

    /**
     * Registers an artifact as an output for this collaboration
     *
     * @param specificationId The identifier of the output specification that the artifact fulfills
     * @param artifactId      The identifier of the output artifact
     * @return The protocol reply
     */
    Reply registerOutput(String specificationId, String artifactId);

    /**
     * Un-registers an artifact as an output for this collaboration
     *
     * @param specificationId The identifier of the output specification that the artifact fulfills
     * @param artifactId      The identifier of the output artifact
     * @return The protocol reply
     */
    Reply unregisterOutput(String specificationId, String artifactId);

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
    Reply createRole(String identifier, String name);

    /**
     * Registers a role for this collaboration
     *
     * @param roleId The identifier of the role
     * @return The protocol reply
     */
    Reply addRole(String roleId);

    /**
     * Unregisters a role from this collaboration
     *
     * @param roleId The identifier of the role
     * @return The protocol reply
     */
    Reply removeRole(String roleId);

    /**
     * Gets the descriptor of the collaboration pattern for the orchestration of this collaboration
     *
     * @return The descriptor of the current collaboration pattern
     */
    CollaborationPatternDescriptor getCollaborationPattern();
}
