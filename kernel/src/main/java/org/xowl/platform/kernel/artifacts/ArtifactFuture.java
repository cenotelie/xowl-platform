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

package org.xowl.platform.kernel.artifacts;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.store.rdf.Quad;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents the skeleton metadata of a future artifact
 *
 * @author Laurent Wouters
 */
public class ArtifactFuture extends ArtifactBase {
    /**
     * Initializes this artifact
     *
     * @param name       The name of this artifact
     * @param base       The identifier of the base artifact
     * @param version    The version of this artifact
     * @param archetype  The archetype of this artifact
     * @param superseded The artifacts superseded by this one
     */
    public ArtifactFuture(String name, String base, String version, String archetype, String[] superseded) {
        super(newArtifactID(), name, base, version, archetype, "", "", superseded);
    }

    /**
     * Initializes this artifact
     *
     * @param definition The AST root for the serialized definition
     */
    public ArtifactFuture(ASTNode definition) {
        super(definition);
    }

    @Override
    public Collection<Quad> getContent() {
        return Collections.emptyList();
    }
}
