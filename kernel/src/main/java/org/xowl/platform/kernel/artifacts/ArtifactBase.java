/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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

package org.xowl.platform.kernel.artifacts;

import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.LiteralNode;
import org.xowl.infra.store.rdf.Node;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.platform.kernel.KernelSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Base implementation for artifacts
 *
 * @author Laurent Wouters
 */
public abstract class ArtifactBase implements Artifact {
    /**
     * Creates a new artifact identifier
     *
     * @param baseURI The base URI to build from
     * @return The new artifact identifier
     */
    public static String newArtifactID(String baseURI) {
        return baseURI + "#" + UUID.randomUUID().toString();
    }


    /**
     * The identifier for this artifact
     */
    protected final String identifier;
    /**
     * The name of this artifact
     */
    protected final String name;
    /**
     * The identifier of the base artifact
     */
    protected final String baseID;
    /**
     * The version of this artifact
     */
    protected final String version;
    /**
     * The metadata quads
     */
    protected final Collection<Quad> metadata;

    /**
     * Initializes this data package
     *
     * @param metadata The metadata quads
     */
    public ArtifactBase(Collection<Quad> metadata) {
        String identifier = "";
        String name = "";
        String version = "";
        String baseID = "";
        for (Quad quad : metadata) {
            if (identifier.isEmpty() && quad.getSubject().getNodeType() == Node.TYPE_IRI)
                identifier = ((IRINode) quad.getSubject()).getIRIValue();
            if (quad.getProperty().getNodeType() == Node.TYPE_IRI
                    && quad.getSubject().getNodeType() == Node.TYPE_IRI
                    && identifier.equals(((IRINode) quad.getSubject()).getIRIValue())) {
                if (KernelSchema.NAME.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    name = ((LiteralNode) quad.getObject()).getLexicalValue();
                else if (KernelSchema.BASE.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    baseID = ((IRINode) quad.getObject()).getIRIValue();
                else if (KernelSchema.VERSION.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    version = ((LiteralNode) quad.getObject()).getLexicalValue();
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.baseID = baseID;
        this.version = version;
        this.metadata = Collections.unmodifiableCollection(new ArrayList<>(metadata));
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
    public String getBase() {
        return baseID;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"identifier\": \""
                + IOUtils.escapeStringJSON(identifier)
                + "\", \"name\":\""
                + IOUtils.escapeStringJSON(name)
                + "\", \"type\": \""
                + IOUtils.escapeStringJSON(Artifact.class.getCanonicalName())
                + "\", \"base\": \""
                + IOUtils.escapeStringJSON(baseID)
                + "\", \"version\": \""
                + IOUtils.escapeStringJSON(version)
                + "\"}";
    }

    @Override
    public Collection<Quad> getMetadata() {
        return metadata;
    }
}
