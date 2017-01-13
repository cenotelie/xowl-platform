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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformRoleBase;

import java.util.*;

/**
 * Represents the data about a collaboration
 *
 * @author Laurent Wouters
 */
public class CollaborationManifest implements Identifiable, Serializable {
    /**
     * The identifier for the collaboration
     */
    private final String identifier;
    /**
     * The human-readable name for the collaboration
     */
    private final String name;
    /**
     * The expected inputs
     */
    private final Map<String, ArtifactSpecification> inputSpecifications;
    /**
     * The expected outputs
     */
    private final Map<String, ArtifactSpecification> outputSpecifications;
    /**
     * The artifacts for the inputs
     */
    private final Map<String, Collection<String>> inputArtifacts;
    /**
     * The artifacts for the outputs
     */
    private final Map<String, Collection<String>> outputArtifacts;
    /**
     * The roles for this collaboration
     */
    private final Map<String, PlatformRole> roles;
    /**
     * The collaboration pattern
     */
    private final CollaborationPattern pattern;

    /**
     * Initializes this manifest
     *
     * @param identifier The identifier for the collaboration
     * @param name       The human-readable name for the collaboration
     * @param pattern    The collaboration pattern
     */
    public CollaborationManifest(String identifier, String name, CollaborationPattern pattern) {
        this.identifier = identifier;
        this.name = name;
        this.pattern = pattern;
        this.inputSpecifications = new HashMap<>();
        this.outputSpecifications = new HashMap<>();
        this.inputArtifacts = new HashMap<>();
        this.outputArtifacts = new HashMap<>();
        this.roles = new HashMap<>();
    }

    /**
     * Initializes this manifest
     *
     * @param definition The AST node for the serialized definition
     */
    public CollaborationManifest(ASTNode definition) {
        this.inputSpecifications = new HashMap<>();
        this.outputSpecifications = new HashMap<>();
        this.inputArtifacts = new HashMap<>();
        this.outputArtifacts = new HashMap<>();
        this.roles = new HashMap<>();
        String identifier = "";
        String name = "";
        CollaborationPattern pattern = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            switch (head) {
                case "identifier": {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    identifier = value.substring(1, value.length() - 1);
                    break;
                }
                case "name": {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    name = value.substring(1, value.length() - 1);
                    break;
                }
                case "inputs": {
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        ArtifactSpecification specification = null;
                        Collection<String> artifacts = new ArrayList<>();
                        for (ASTNode member2 : child.getChildren()) {
                            head = TextUtils.unescape(member2.getChildren().get(0).getValue());
                            head = head.substring(1, head.length() - 1);
                            switch (head) {
                                case "specification": {
                                    specification = new ArtifactSpecification(member2.getChildren().get(1));
                                    break;
                                }
                                case "artifacts": {
                                    for (ASTNode child2 : member2.getChildren().get(1).getChildren()) {
                                        String value = TextUtils.unescape(child2.getValue());
                                        value = value.substring(1, value.length() - 1);
                                        artifacts.add(value);
                                    }
                                    break;
                                }
                            }
                        }
                        if (specification != null) {
                            inputSpecifications.put(specification.getIdentifier(), specification);
                            inputArtifacts.put(specification.getIdentifier(), artifacts);
                        }
                    }
                    break;
                }
                case "outputs": {
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        ArtifactSpecification specification = null;
                        Collection<String> artifacts = new ArrayList<>();
                        for (ASTNode member2 : child.getChildren()) {
                            head = TextUtils.unescape(member2.getChildren().get(0).getValue());
                            head = head.substring(1, head.length() - 1);
                            switch (head) {
                                case "specification": {
                                    specification = new ArtifactSpecification(member2.getChildren().get(1));
                                    break;
                                }
                                case "artifacts": {
                                    for (ASTNode child2 : member2.getChildren().get(1).getChildren()) {
                                        String value = TextUtils.unescape(child2.getValue());
                                        value = value.substring(1, value.length() - 1);
                                        artifacts.add(value);
                                    }
                                    break;
                                }
                            }
                        }
                        if (specification != null) {
                            outputSpecifications.put(specification.getIdentifier(), specification);
                            outputArtifacts.put(specification.getIdentifier(), artifacts);
                        }
                    }
                    break;
                }
                case "roles": {
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        PlatformRoleBase role = new PlatformRoleBase(child);
                        roles.put(role.getIdentifier(), role);
                    }
                    break;
                }
                case "pattern": {
                    pattern = new CollaborationPatternBase(member.getChildren().get(1));
                    break;
                }
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.pattern = pattern;
    }

    /**
     * Gets the expected inputs for this collaboration
     *
     * @return The expected inputs
     */
    public Collection<ArtifactSpecification> getInputSpecifications() {
        return Collections.unmodifiableCollection(inputSpecifications.values());
    }

    /**
     * Adds a new input specification
     *
     * @param specification The input specification
     */
    public void addInputSpecification(ArtifactSpecification specification) {
        synchronized (inputSpecifications) {
            inputSpecifications.put(specification.getIdentifier(), specification);
            inputArtifacts.put(specification.getIdentifier(), new ArrayList<String>());
        }
    }

    /**
     * Removes an input specification
     *
     * @param specificationId The identifier of the specification to remove
     * @return Whether the specification was present
     */
    public boolean removeInputSpecification(String specificationId) {
        synchronized (inputSpecifications) {
            ArtifactSpecification specification = inputSpecifications.remove(specificationId);
            if (specification != null)
                inputArtifacts.remove(specificationId);
            return specification != null;
        }
    }

    /**
     * Gets the expected outputs for this collaboration
     *
     * @return The expected outputs
     */
    public Collection<ArtifactSpecification> getOutputSpecifications() {
        return Collections.unmodifiableCollection(outputSpecifications.values());
    }

    /**
     * Adds a new output specification
     *
     * @param specification The output specification
     */
    public void addOutputSpecification(ArtifactSpecification specification) {
        synchronized (outputSpecifications) {
            outputSpecifications.put(specification.getIdentifier(), specification);
            outputArtifacts.put(specification.getIdentifier(), new ArrayList<String>());
        }
    }

    /**
     * Removes an output specification
     *
     * @param specificationId The identifier of the specification to remove
     * @return Whether the specification was present
     */
    public boolean removeOutputSpecification(String specificationId) {
        synchronized (outputSpecifications) {
            ArtifactSpecification specification = outputSpecifications.remove(specificationId);
            if (specification != null)
                outputArtifacts.remove(specificationId);
            return specification != null;
        }
    }

    /**
     * Gets the identifiers of the artifacts that fulfills an input specification
     *
     * @param specificationId The identifier of an input specification
     * @return The identifiers of the artifacts
     */
    public Collection<String> getArtifactsForInput(String specificationId) {
        return Collections.unmodifiableCollection(inputArtifacts.get(specificationId));
    }

    /**
     * Adds an artifact that fulfills an input specification
     *
     * @param specificationId The identifier of an input specification
     * @param artifactId      The identifier of the artifact that fulfills the specification
     */
    public void addInputArtifact(String specificationId, String artifactId) {
        synchronized (inputSpecifications) {
            Collection<String> artifacts = inputArtifacts.get(specificationId);
            if (artifacts != null)
                artifacts.add(artifactId);
        }
    }

    /**
     * Removes an artifact as fulfilling an input specification
     *
     * @param specificationId The identifier of the input specification
     * @param artifactId      The identifier of the artifact that fulfills the specification
     */
    public void removeInputArtifact(String specificationId, String artifactId) {
        synchronized (inputSpecifications) {
            Collection<String> artifacts = inputArtifacts.get(specificationId);
            if (artifacts != null)
                artifacts.remove(artifactId);
        }
    }

    /**
     * Gets the identifiers of the artifacts that fulfuills an output specification
     *
     * @param specificationId The identifier of an output specification
     * @return The identifiers of the artifacts
     */
    public Collection<String> getArtifactsForOutput(String specificationId) {
        return Collections.unmodifiableCollection(outputArtifacts.get(specificationId));
    }

    /**
     * Adds an artifact that fulfills an output specification
     *
     * @param specificationId The identifier of an output specification
     * @param artifactId      The identifier of the artifact that fulfills the specification
     */
    public void addOutputArtifact(String specificationId, String artifactId) {
        synchronized (outputSpecifications) {
            Collection<String> artifacts = outputArtifacts.get(specificationId);
            if (artifacts != null)
                artifacts.add(artifactId);
        }
    }

    /**
     * Removes an artifact as fulfilling an output specification
     *
     * @param specificationId The identifier of the output specification
     * @param artifactId      The identifier of the artifact that fulfills the specification
     */
    public void removeOutputArtifact(String specificationId, String artifactId) {
        synchronized (outputSpecifications) {
            Collection<String> artifacts = outputArtifacts.get(specificationId);
            if (artifacts != null)
                artifacts.remove(artifactId);
        }
    }

    /**
     * Gets the roles for this collaboration
     *
     * @return The roles for this collaboration
     */
    public Collection<PlatformRole> getRoles() {
        return Collections.unmodifiableCollection(roles.values());
    }

    /**
     * Adds a new role
     *
     * @param role The role
     */
    public void addRole(PlatformRole role) {
        synchronized (roles) {
            roles.put(role.getIdentifier(), role);
        }
    }

    /**
     * Removes a role
     *
     * @param roleId The identifier of the role to remove
     * @return true if the role was removed, false if it was not present
     */
    public boolean removeRole(String roleId) {
        synchronized (roles) {
            return roles.remove(roleId) != null;
        }
    }

    /**
     * Gets the collaboration pattern for the orchestration of this collaboration
     *
     * @return The collaboration pattern
     */
    public CollaborationPattern getCollaborationPattern() {
        return pattern;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(CollaborationManifest.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"inputs\": [");
        boolean first = true;
        synchronized (inputSpecifications) {
            for (String inputId : inputSpecifications.keySet()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append("{\"specification\": ");
                builder.append(inputSpecifications.get(inputId).serializedJSON());
                builder.append(", \"artifacts\": [");
                boolean first2 = true;
                for (String artifact : inputArtifacts.get(inputId)) {
                    if (!first2)
                        builder.append(", ");
                    first2 = false;
                    builder.append("\"");
                    builder.append(TextUtils.escapeStringJSON(artifact));
                    builder.append("\"");
                }
                builder.append("]}");
            }
        }
        builder.append("], \"outputs\": [");
        first = true;
        synchronized (outputSpecifications) {
            for (String inputId : outputSpecifications.keySet()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append("{\"specification\": ");
                builder.append(outputSpecifications.get(inputId).serializedJSON());
                builder.append(", \"artifacts\": [");
                boolean first2 = true;
                for (String artifact : outputArtifacts.get(inputId)) {
                    if (!first2)
                        builder.append(", ");
                    first2 = false;
                    builder.append("\"");
                    builder.append(TextUtils.escapeStringJSON(artifact));
                    builder.append("\"");
                }
                builder.append("]}");
            }
        }
        builder.append("], \"roles\": [");
        first = true;
        for (PlatformRole role : roles.values()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(role.serializedJSON());
        }
        builder.append("], \"pattern\": ");
        builder.append(pattern.serializedJSON());
        builder.append("}");
        return builder.toString();
    }
}
