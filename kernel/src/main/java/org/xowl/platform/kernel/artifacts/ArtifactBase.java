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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.*;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.KernelSchema;

import java.text.DateFormat;
import java.util.*;

/**
 * Base implementation for artifacts
 *
 * @author Laurent Wouters
 */
public abstract class ArtifactBase implements Artifact {
    /**
     * Creates a new artifact identifier
     *
     * @return The new artifact identifier
     */
    public static String newArtifactID() {
        return KernelSchema.ARTIFACT + "#" + UUID.randomUUID().toString();
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
    protected final String base;
    /**
     * The artifact superseded by this one
     */
    protected final String superseded;
    /**
     * The version of this artifact
     */
    protected final String version;
    /**
     * The archetype of this artifact
     */
    protected final String archetype;
    /**
     * The identifier of the originating connector
     */
    protected final String from;
    /**
     * The artifact's creation time
     */
    protected final String creation;
    /**
     * The metadata quads
     */
    private Collection<Quad> metadata;

    /**
     * Initializes this artifact
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
    public ArtifactBase(String identifier, String name, String base, String version, String archetype, String from, String creation, String superseded) {
        this.identifier = identifier;
        this.name = name;
        this.base = base;
        this.version = version;
        this.archetype = archetype;
        this.from = from;
        this.creation = creation;
        this.superseded = superseded;
    }

    /**
     * Initializes this artifact from the specified skeleton
     *
     * @param skeleton The skeleton
     * @param from     The identifier of the originating connector
     */
    public ArtifactBase(Artifact skeleton, String from) {
        this.identifier = newArtifactID();
        this.name = skeleton.getName();
        this.base = skeleton.getBase();
        this.version = skeleton.getVersion();
        this.archetype = skeleton.getArchetype();
        this.from = from;
        this.creation = DateFormat.getDateTimeInstance().format(new Date());
        this.superseded = skeleton.getSuperseded();
    }

    /**
     * Initializes this artifact
     *
     * @param metadata The metadata quads
     */
    public ArtifactBase(Collection<Quad> metadata) {
        String identifier = "";
        String name = "";
        String base = "";
        String version = "";
        String from = "";
        String creation = "";
        String archetype = "";
        String superseded = null;

        for (Quad quad : metadata) {
            if (identifier.isEmpty() && quad.getSubject().getNodeType() == Node.TYPE_IRI)
                identifier = ((IRINode) quad.getSubject()).getIRIValue();
            if (quad.getProperty().getNodeType() == Node.TYPE_IRI
                    && quad.getSubject().getNodeType() == Node.TYPE_IRI
                    && identifier.equals(((IRINode) quad.getSubject()).getIRIValue())) {
                if (KernelSchema.NAME.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    name = ((LiteralNode) quad.getObject()).getLexicalValue();
                else if (KernelSchema.BASE.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    base = ((IRINode) quad.getObject()).getIRIValue();
                else if (KernelSchema.SUPERSEDE.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    superseded = ((IRINode) quad.getObject()).getIRIValue();
                else if (KernelSchema.VERSION.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    version = ((LiteralNode) quad.getObject()).getLexicalValue();
                else if (KernelSchema.ARCHETYPE.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    archetype = ((LiteralNode) quad.getObject()).getLexicalValue();
                else if (KernelSchema.FROM.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    from = ((LiteralNode) quad.getObject()).getLexicalValue();
                else if (KernelSchema.CREATED.equals(((IRINode) quad.getProperty()).getIRIValue()))
                    creation = ((LiteralNode) quad.getObject()).getLexicalValue();
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.base = base;
        this.version = version;
        this.from = from;
        this.creation = creation;
        this.archetype = archetype;
        this.superseded = superseded;
        this.metadata = Collections.unmodifiableCollection(new ArrayList<>(metadata));
    }

    /**
     * Initializes this artifact
     *
     * @param definition The AST root for the serialized definition
     */
    public ArtifactBase(ASTNode definition) {
        String identifier = "";
        String name = "";
        String base = "";
        String version = "";
        String from = "";
        String creation = "";
        String archetype = "";
        String superseded = null;

        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("base".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                base = value.substring(1, value.length() - 1);
            } else if ("version".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                version = value.substring(1, value.length() - 1);
            } else if ("from".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                from = value.substring(1, value.length() - 1);
            } else if ("creation".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                creation = value.substring(1, value.length() - 1);
            } else if ("archetype".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                archetype = value.substring(1, value.length() - 1);
            } else if ("superseded".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                value = value.substring(1, value.length() - 1);
                if (!value.isEmpty())
                    superseded = value;
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.base = base;
        this.version = version;
        this.from = from;
        this.creation = creation;
        this.archetype = archetype;
        this.superseded = superseded;
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
        return base;
    }

    @Override
    public String getSuperseded() {
        return superseded;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getArchetype() {
        return archetype;
    }

    @Override
    public String getOrigin() {
        return from;
    }

    @Override
    public String getCreationDate() {
        return creation;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" + TextUtils.escapeStringJSON(Artifact.class.getCanonicalName()) +
                "\", \"identifier\":\"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"base\": \"" +
                TextUtils.escapeStringJSON(base) +
                "\", \"version\": \"" +
                TextUtils.escapeStringJSON(version) +
                "\", \"from\": \"" +
                TextUtils.escapeStringJSON(from) +
                "\", \"creation\": \"" +
                TextUtils.escapeStringJSON(creation) +
                "\", \"archetype\": \"" +
                TextUtils.escapeStringJSON(archetype) +
                "\", \"superseded\": \"" +
                (superseded != null ? TextUtils.escapeStringJSON(superseded) : "") +
                "\"}";
    }

    @Override
    public synchronized Collection<Quad> getMetadata() {
        if (metadata == null) {
            metadata = new ArrayList<>();
            NodeManager nodes = new CachedNodes();
            GraphNode graph = nodes.getIRINode(KernelSchema.GRAPH_ARTIFACTS);
            SubjectNode subject = nodes.getIRINode(identifier);
            metadata.add(new Quad(graph, subject, nodes.getIRINode(KernelSchema.NAME), nodes.getLiteralNode(name, Vocabulary.xsdString, null)));
            metadata.add(new Quad(graph, subject, nodes.getIRINode(KernelSchema.BASE), nodes.getIRINode(base)));
            metadata.add(new Quad(graph, subject, nodes.getIRINode(KernelSchema.VERSION), nodes.getLiteralNode(version, Vocabulary.xsdString, null)));
            metadata.add(new Quad(graph, subject, nodes.getIRINode(KernelSchema.FROM), nodes.getLiteralNode(from, Vocabulary.xsdString, null)));
            metadata.add(new Quad(graph, subject, nodes.getIRINode(KernelSchema.CREATED), nodes.getLiteralNode(creation, Vocabulary.xsdDateTime, null)));
            metadata.add(new Quad(graph, subject, nodes.getIRINode(KernelSchema.ARCHETYPE), nodes.getLiteralNode(archetype, Vocabulary.xsdString, null)));
            if (superseded != null)
                metadata.add(new Quad(graph, subject, nodes.getIRINode(KernelSchema.SUPERSEDE), nodes.getIRINode(superseded)));
        }
        return metadata;
    }
}
