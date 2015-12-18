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

import org.xowl.store.IOUtils;
import org.xowl.store.rdf.Quad;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * The base implementation of an artifact
 *
 * @author Laurent Wouters
 */
public class BaseArtifact implements Artifact {
    /**
     * The identifier for this baseline
     */
    protected final String identifier;
    /**
     * The name of this baseline
     */
    protected final String name;
    /**
     * The quads containing the serialized requirements
     */
    protected final Collection<Quad> quads;

    /**
     * Initializes this data package
     *
     * @param identifier The identifier for the
     * @param quads      The serialized requirements
     */
    public BaseArtifact(String identifier, String name, Collection<Quad> quads) {
        this.identifier = identifier;
        this.name = name;
        this.quads = Collections.unmodifiableCollection(quads);
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
    public Collection<Quad> getMetadata() {
        return quads;
    }

    @Override
    public String getMIMEType() {
        return null;
    }

    @Override
    public InputStream getStream() {
        return null;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"identifier\": \""
                + IOUtils.escapeStringJSON(identifier)
                + "\", \"name\":"
                + IOUtils.escapeStringJSON(name)
                + "\", \"type\": \""
                + IOUtils.escapeStringJSON(getMIMEType())
                + "\"}";
    }
}
