/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.kernel;

import org.xowl.store.Serializable;
import org.xowl.store.rdf.Quad;

import java.util.Collection;

/**
 * An artifact represents a package of data in the form of quads that can be managed by the federation platform
 * The identifier of an artifact must be unique.
 * An artifact may have a version tag attached to it.
 * In this case, the base identifier refer to the common identifier for all versions of the artifact.
 * The expected metadata schema is as follow:
 * v1 -- base --> xxx
 * v1 -- version --> v1
 * v2 -- base --> xxx
 * v2 -- version --> v2
 * An artifact contains two sets of quads:
 * - Metadata quads that express knowledge about the artifact itself.
 * Metadata quads must be contained in the platform artifact graph.
 * Metadata quads must have the artifact's identifier as subject.
 * - Content quads, i.e. the payload.
 * Content quads must be contained in the graph that is represented by the artifact's identifier
 *
 * @author Laurent Wouters
 */
public interface Artifact extends Identifiable, Serializable {
    /**
     * Gets the identifier of the base artifact
     *
     * @return The identifier of the base artifact
     */
    String getBase();

    /**
     * Gets the version string of this artifact
     *
     * @return The version string, or null if there is none
     */
    String getVersion();

    /**
     * Gets the metadata quads
     *
     * @return The metadata quads
     */
    Collection<Quad> getMetadata();

    /**
     * Gets the payload content quads
     *
     * @return The payload content quads
     */
    Collection<Quad> getContent();
}
