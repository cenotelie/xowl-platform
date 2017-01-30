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
import org.xowl.infra.store.ProxyObject;
import org.xowl.infra.utils.TextUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents an artifact schema from a remote platform
 *
 * @author Laurent Wouters
 */
public class ArtifactSchemaRemote implements ArtifactSchema {
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
     * @param iri The schema's iri
     */
    public ArtifactSchemaRemote(String iri) {
        this.identifier = iri;
        this.name = iri;
    }

    /**
     * Initializes this archetype
     *
     * @param definition The JSON definition
     */
    public ArtifactSchemaRemote(ASTNode definition) {
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
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(ArtifactSchema.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"classes\": [" +
                "], \"datatypes\": [" +
                "], \"objectProperties\": [" +
                "], \"dataProperties\": [" +
                "], \"individuals\": [" +
                "]}";
    }

    @Override
    public Collection<ProxyObject> getClasses() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ProxyObject> getDatatypes() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ProxyObject> getObjectProperties() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ProxyObject> getDataProperties() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ProxyObject> getIndividuals() {
        return Collections.emptyList();
    }

    @Override
    public ProxyObject getEntity(String uri) {
        return null;
    }
}
