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
import org.xowl.infra.store.writers.JsonSerializer;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.logging.Logging;

import java.io.StringWriter;

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
     * Initializes this schema
     *
     * @param identifier The schema's identifier
     * @param name       The schema's name
     */
    public ArtifactSchemaBase(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    /**
     * Initializes this schema
     *
     * @param definition The JSON definition
     */
    public ArtifactSchemaBase(ASTNode definition) {
        String identifier = null;
        String name = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            }
        }
        this.identifier = identifier;
        this.name = name;
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
        StringWriter writer = new StringWriter();
        writer.append("{\"type\": \"");
        writer.append(TextUtils.escapeStringJSON(ArtifactSchema.class.getCanonicalName()));
        writer.append("\", \"identifier\": \"");
        writer.append(TextUtils.escapeStringJSON(identifier));
        writer.append("\", \"name\": \"");
        writer.append(TextUtils.escapeStringJSON(name));
        writer.append("\", \"definition\": ");
        JsonSerializer serializer = new JsonSerializer(writer);
        serializer.serialize(Logging.get(), getDefinition().iterator());
        return writer.toString();
    }
}
