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

package org.xowl.platform.kernel.collab;

import org.xowl.infra.utils.Serializable;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.platform.PlatformRole;

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
     * The collaboration pattern
     */
    private final CollaborationPattern pattern;

    /**
     * Initializes this specification
     *
     * @param pattern The collaboration pattern
     */
    public CollaborationSpecification(CollaborationPattern pattern) {
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.roles = new ArrayList<>();
        this.pattern = pattern;
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
     * Gets the collaboration pattern for the orchestration of this collaboration
     *
     * @return The collaboration pattern
     */
    public CollaborationPattern getCollaborationPattern() {
        return pattern;
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        return null;
    }
}
