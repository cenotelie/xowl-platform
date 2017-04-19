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
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents an artifact schema from a remote platform
 *
 * @author Laurent Wouters
 */
public class ArtifactSchemaRemote extends ArtifactSchemaBase {
    /**
     * The schema definition
     */
    private final Collection<Quad> quads;

    /**
     * Initializes this archetype
     *
     * @param definition The JSON definition
     */
    public ArtifactSchemaRemote(ASTNode definition) {
        super(definition);
        this.quads = new ArrayList<>();
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("definition".equals(head)) {
                JsonLoader loader = new JsonLoader();
                loader.loadGraphs(member.getChildren().get(1), quads);
            }
        }
    }

    @Override
    public Collection<Quad> getDefinition(boolean deployable) {
        if (!deployable)
            return Collections.unmodifiableCollection(quads);
        return toDeployable(quads);
    }
}
