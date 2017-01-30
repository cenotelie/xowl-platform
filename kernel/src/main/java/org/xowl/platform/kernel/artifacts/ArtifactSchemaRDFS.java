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

import org.xowl.infra.store.IRIs;

/**
 * The RDFS meta-schema
 *
 * @author Laurent Wouters
 */
public class ArtifactSchemaRDFS extends ArtifactSchemaFromResource {
    /**
     * The instance for this schema
     */
    public static final ArtifactSchema INSTANCE = new ArtifactSchemaRDFS();

    /**
     * Initializes this schema
     */
    private ArtifactSchemaRDFS() {
        super(IRIs.class, "/org/w3c/www/2000/01/rdf-schema.ttl", IRIs.RDFS);
    }
}
