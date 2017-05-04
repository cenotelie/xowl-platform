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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.store.IRIs;
import org.xowl.infra.store.rdf.GraphNode;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.store.writers.JsonSerializer;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.logging.Logging;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Provides a base implementation of an artifact's schema
 *
 * @author Laurent Wouters
 */
public abstract class ArtifactSchemaBase implements ArtifactSchema {
    /**
     * The schema's identifier
     */
    protected final String identifier;
    /**
     * The schema's name
     */
    protected final String name;
    /**
     * Whether this schema can be deployed in a triple store
     */
    protected final boolean deployable;

    /**
     * Initializes this schema
     *
     * @param identifier The schema's identifier
     * @param name       The schema's name
     * @param deployable Whether this schema can be deployed in a triple store
     */
    public ArtifactSchemaBase(String identifier, String name, boolean deployable) {
        this.identifier = identifier;
        this.name = name;
        this.deployable = deployable;
    }

    /**
     * Initializes this schema
     *
     * @param definition The JSON definition
     */
    public ArtifactSchemaBase(ASTNode definition) {
        String identifier = null;
        String name = null;
        String deployable = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("deployable".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                deployable = value.substring(1, value.length() - 1);
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.deployable = deployable != null && "true".equalsIgnoreCase(deployable);
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
    public boolean isDeployable() {
        return deployable;
    }

    @Override
    public Collection<Quad> getDefinition() {
        return getDefinition(false);
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringWriter writer = new StringWriter();
        writer.append("{\"type\": \"");
        writer.append(TextUtils.escapeStringJSON(ArtifactSchema.class.getCanonicalName()));
        writer.append("\", \"identifier\": \"");
        writer.append(TextUtils.escapeStringJSON(identifier));
        writer.append("\", \"name\": \"");
        writer.append(TextUtils.escapeStringJSON(name));
        writer.append("\", \"deployable\": \"");
        writer.append(Boolean.toString(deployable));
        writer.append("\", \"definition\": ");
        JsonSerializer serializer = new JsonSerializer(writer);
        serializer.serialize(Logging.get(), getDefinition().iterator());
        return writer.toString();
    }

    /**
     * Translates original quads to deployable quads
     *
     * @param quads The original quads
     * @return The deployable quads
     */
    protected static Collection<Quad> toDeployable(Collection<Quad> quads) {
        if (quads == null)
            return null;
        if (quads.isEmpty())
            return Collections.emptyList();
        CachedNodes nodes = new CachedNodes();
        GraphNode graph = nodes.getIRINode(IRIs.GRAPH_INFERENCE);
        Collection<Quad> result = new ArrayList<>(quads.size());
        for (Quad quad : quads)
            result.add(new Quad(graph, quad.getSubject(), quad.getProperty(), quad.getObject()));
        return Collections.unmodifiableCollection(result);
    }
}
