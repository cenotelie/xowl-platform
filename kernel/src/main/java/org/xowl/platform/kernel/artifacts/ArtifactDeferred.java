/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
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

import org.xowl.infra.store.rdf.Quad;

import java.util.Collection;

/**
 * Represents an artifact with content that is not yet loaded
 *
 * @author Laurent Wouters
 */
public abstract class ArtifactDeferred extends ArtifactBase {
    /**
     * The payload quads
     */
    protected Collection<Quad> content;

    /**
     * Initializes this data package
     *
     * @param metadata The metadata quads
     */
    public ArtifactDeferred(Collection<Quad> metadata) {
        super(metadata);
    }

    @Override
    public Collection<Quad> getContent() {
        if (content == null) {
            content = load();
        }
        return content;
    }

    /**
     * Loads the content of this artifact
     *
     * @return The loaded content
     */
    protected abstract Collection<Quad> load();
}
