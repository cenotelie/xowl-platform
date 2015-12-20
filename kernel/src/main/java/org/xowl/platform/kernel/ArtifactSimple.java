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
import org.xowl.store.rdf.IRINode;
import org.xowl.store.rdf.LiteralNode;
import org.xowl.store.rdf.Node;
import org.xowl.store.rdf.Quad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements a simple artifacts that contains all its data
 *
 * @author Laurent Wouters
 */
public class ArtifactSimple implements Artifact {
    /**
     * The identifier for this baseline
     */
    protected final String identifier;
    /**
     * The name of this baseline
     */
    protected final String name;
    /**
     * The metadata quads
     */
    protected final Collection<Quad> metadata;
    /**
     * The payload quads
     */
    protected final Collection<Quad> content;

    /**
     * Initializes this data package
     *
     * @param metadata The metadata quads
     * @param content  The payload quads
     */
    public ArtifactSimple(Collection<Quad> metadata, Collection<Quad> content) {
        String identifier = "";
        String name = "";
        for (Quad quad : metadata) {
            if (quad.getProperty().getNodeType() == Node.TYPE_IRI
                    && KernelSchema.HAS_NAME.equals(((IRINode) quad.getProperty()).getIRIValue())
                    && quad.getSubject().getNodeType() == Node.TYPE_IRI
                    && identifier.equals(((IRINode) quad.getSubject()).getIRIValue())
                    && quad.getObject().getNodeType() == Node.TYPE_LITERAL) {
                name = ((LiteralNode) quad.getObject()).getLexicalValue();
            }
            if (identifier.isEmpty())
                identifier = ((IRINode) quad.getSubject()).getIRIValue();
        }
        this.identifier = identifier;
        this.name = name;
        this.metadata = Collections.unmodifiableCollection(new ArrayList<>(metadata));
        this.content = Collections.unmodifiableCollection(new ArrayList<>(content));
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
        return "{\"identifier\": \""
                + IOUtils.escapeStringJSON(identifier)
                + "\", \"name\":"
                + IOUtils.escapeStringJSON(name)
                + "\", \"type\": \""
                + IOUtils.escapeStringJSON(Artifact.class.getCanonicalName())
                + "\"}";
    }

    @Override
    public Collection<Quad> getMetadata() {
        return metadata;
    }

    @Override
    public Collection<Quad> getContent() {
        return content;
    }
}
