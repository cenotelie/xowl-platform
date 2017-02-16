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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements a simple artifacts that contains all its data
 *
 * @author Laurent Wouters
 */
public class ArtifactSimple extends ArtifactBase {
    /**
     * The payload quads
     */
    protected final Collection<Quad> content;

    /**
     * Initializes this simple artifact
     *
     * @param identifier The identifier for this artifact
     * @param name       The name of this artifact
     * @param base       The identifier of the base artifact
     * @param version    The version of this artifact
     * @param archetype  The archetype of this artifact
     * @param from       The identifier of the originating connector
     * @param creation   The artifact's creation time
     * @param superseded The artifact superseded by this one
     */
    public ArtifactSimple(String identifier, String name, String base, String version, String archetype, String from, String creation, String superseded, Collection<Quad> content) {
        super(identifier, name, base, version, archetype, from, creation, superseded);
        this.content = Collections.unmodifiableCollection(new ArrayList<>(content));
    }

    /**
     * Initializes this simple artifact
     *
     * @param skeleton The skeleton
     * @param from     The identifier of the originating connector
     * @param content  The payload quads
     */
    public ArtifactSimple(Artifact skeleton, String from, Collection<Quad> content) {
        super(skeleton, from);
        this.content = Collections.unmodifiableCollection(new ArrayList<>(content));
    }

    /**
     * Initializes this simple artifact
     *
     * @param metadata The metadata quads
     * @param content  The payload quads
     */
    public ArtifactSimple(Collection<Quad> metadata, Collection<Quad> content) {
        super(metadata);
        this.content = Collections.unmodifiableCollection(new ArrayList<>(content));
    }

    @Override
    public Collection<Quad> getContent() {
        return content;
    }
}
