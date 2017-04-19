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
import org.xowl.infra.utils.TextUtils;

/**
 * Represents an artifact's archetype from a remote platform
 */
public class ArtifactArchetypeRemote extends ArtifactArchetypeBase {
    /**
     * The associated schema
     */
    protected final ArtifactSchema schema;

    /**
     * Initializes this archetype
     *
     * @param definition The JSON definition
     */
    public ArtifactArchetypeRemote(ASTNode definition) {
        super(definition);
        String schema = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("schema".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                schema = value.substring(1, value.length() - 1);
            }
        }
        this.schema = schema != null && !schema.isEmpty() ? new ArtifactSchemaStub(schema, schema) : null;
    }

    @Override
    public ArtifactSchema getSchema() {
        return schema;
    }
}
