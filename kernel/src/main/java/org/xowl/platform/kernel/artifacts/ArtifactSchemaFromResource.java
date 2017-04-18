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

import org.xowl.infra.utils.TextUtils;

/**
 * Implements a business schema that is backed by a resource file
 *
 * @author Laurent Wouters
 */
public class ArtifactSchemaFromResource implements ArtifactSchema {
    /**
     * The rdfs:label property
     */
    private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

    /**
     * The schema's IRI
     */
    private final String iri;
    /**
     * The schema's name
     */
    private final String name;
    /**
     * A class that is in the same bundle as the resource
     */
    private final Class<?> loaderType;
    /**
     * The resource URI, e.g. /com/business/serious/schema.fs
     */
    private final String loaderResource;

    /**
     * Initializes this schema
     *
     * @param type     A class that is in the same bundle as the resource
     * @param resource The resource URI, e.g. /com/business/serious/schema.fs
     * @param iri      The iri for this schema
     * @param name     The name for this schema
     */
    public ArtifactSchemaFromResource(Class<?> type, String resource, String iri, String name) {
        this.iri = iri;
        this.name = name;
        this.loaderType = type;
        this.loaderResource = resource;
    }

    @Override
    public String getIdentifier() {
        return iri;
    }

    @Override
    public String getName() {
        return iri;
    }

    @Override
    public String serializedString() {
        return iri;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(ArtifactSchema.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(iri) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\"}";
    }
}
