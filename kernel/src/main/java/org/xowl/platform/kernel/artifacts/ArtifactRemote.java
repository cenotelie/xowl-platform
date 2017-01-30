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
 * Represents an artifact stored on a remote platform
 *
 * @author Laurent Wouters
 */
public class ArtifactRemote extends ArtifactBase {
    /**
     * Initializes this artifact
     *
     * @param definition The AST root for the serialized definition
     */
    public ArtifactRemote(ASTNode definition) {
        super(definition);
    }

    @Override
    public Collection<Quad> getContent() {
        return Collections.emptyList();
    }
}
