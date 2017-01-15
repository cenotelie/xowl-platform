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
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformRoleBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Specifies a collaboration so that it can be spawned
 *
 * @author Laurent Wouters
 */
public class CollaborationSpecification implements Serializable {
    /**
     * The human-readable name for the collaboration
     */
    private final String name;
    /**
     * The expected inputs
     */
    private final Collection<ArtifactSpecification> inputs;
    /**
     * The expected outputs
     */
    private final Collection<ArtifactSpecification> outputs;
    /**
     * The roles for this collaboration
     */
    private final Collection<PlatformRole> roles;
    /**
     * The identifier of the collaboration pattern
     */
    private final String pattern;

    /**
     * Initializes this specification
     *
     * @param name    The human-readable name for the collaboration
     * @param pattern The identifier of the collaboration pattern
     */
    public CollaborationSpecification(String name, String pattern) {
        this.name = name;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.roles = new ArrayList<>();
        this.pattern = pattern;
    }

    /**
     * Initializes this specification
     *
     * @param definition The AST node for the serialized definition
     */
    public CollaborationSpecification(ASTNode definition) {
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.roles = new ArrayList<>();
        String name = "";
        String pattern = "";
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("inputs".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    inputs.add(new ArtifactSpecification(child));
                }
            } else if ("outputs".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    outputs.add(new ArtifactSpecification(child));
                }
            } else if ("roles".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    roles.add(new PlatformRoleBase(child));
                }
            } else if ("pattern".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                pattern = value.substring(1, value.length() - 1);
            }
        }
        this.name = name;
        this.pattern = pattern;
    }

    /**
     * Gets the name for the collaboration
     *
     * @return The name for the collaboration
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the expected inputs for this collaboration
     *
     * @return The expected inputs
     */
    public Collection<ArtifactSpecification> getInputSpecifications() {
        return Collections.unmodifiableCollection(inputs);
    }

    /**
     * Gets the expected outputs for this collaboration
     *
     * @return The expected outputs
     */
    public Collection<ArtifactSpecification> getOutputSpecifications() {
        return Collections.unmodifiableCollection(outputs);
    }

    /**
     * Gets the roles for this collaboration
     *
     * @return The roles for this collaboration
     */
    public Collection<PlatformRole> getRoles() {
        return Collections.unmodifiableCollection(roles);
    }

    /**
     * Gets the identifier of the collaboration pattern for the orchestration of this collaboration
     *
     * @return The identifier of the collaboration pattern
     */
    public String getCollaborationPattern() {
        return pattern;
    }

    /**
     * Adds a new input specification
     *
     * @param specification The input specification
     */
    public void addInput(ArtifactSpecification specification) {
        inputs.add(specification);
    }

    /**
     * Adds a new output specification
     *
     * @param specification The output specification
     */
    public void addOutput(ArtifactSpecification specification) {
        outputs.add(specification);
    }

    /**
     * Adds a new role
     *
     * @param role The role
     */
    public void addRole(PlatformRole role) {
        roles.add(role);
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(CollaborationSpecification.class.getCanonicalName()));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"inputs\": [");
        boolean first = true;
        for (ArtifactSpecification specification : inputs) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(specification.serializedJSON());
        }
        builder.append("], \"outputs\": [");
        first = true;
        for (ArtifactSpecification specification : outputs) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(specification.serializedJSON());
        }
        builder.append("], \"roles\": [");
        first = true;
        for (PlatformRole role : roles) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(role.serializedJSON());
        }
        builder.append("], \"pattern\": \"");
        builder.append(TextUtils.escapeStringJSON(pattern));
        builder.append("\"}");
        return builder.toString();
    }
}
