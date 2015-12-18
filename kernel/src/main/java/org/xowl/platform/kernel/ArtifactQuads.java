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

import org.xowl.store.AbstractRepository;
import org.xowl.store.rdf.Quad;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents an artifact that is a set of quads
 *
 * @author Laurent Wouters
 */
public class ArtifactQuads extends BaseArtifact {
    /**
     * The contained quads
     */
    protected final Collection<Quad> content;

    /**
     * Initializes this artifact
     *
     * @param identifier The identifier for this artifact
     * @param name       The name for this artifact
     * @param quads      The quads contained in this artifact
     */
    public ArtifactQuads(String identifier, String name, Collection<Quad> quads) {
        super(identifier, name);
        this.content = Collections.unmodifiableCollection(quads);
    }

    @Override
    public String getMIMEType() {
        return AbstractRepository.SYNTAX_NQUADS;
    }
}
