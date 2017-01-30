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

import org.xowl.infra.server.api.XOWLFactory;
import org.xowl.infra.utils.TextUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * Implements the domain of schemas
 *
 * @author Laurent Wouters
 */
public class SchemaDomain implements BusinessDomain {
    /**
     * The singleton instance
     */
    public static final BusinessDomain INSTANCE = new SchemaDomain();

    /**
     * Initializes this domain
     */
    private SchemaDomain() {
    }

    @Override
    public String getIdentifier() {
        return SchemaDomain.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "Schema Domain";
    }

    @Override
    public String getDescription() {
        return "The domain of schemas for other artifacts and datasets.";
    }

    @Override
    public XOWLFactory getFactory() {
        // no factory
        return null;
    }

    @Override
    public ArtifactSchema getSchema() {
        return ArtifactSchemaKernel.INSTANCE;
    }

    @Override
    public Collection<ArtifactArchetype> getArchetypes() {
        return Collections.singletonList(ArtifactArchetypeSchema.INSTANCE);
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(BusinessDomain.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"description\": \"" +
                TextUtils.escapeStringJSON(getDescription()) +
                "\"}";
    }
}
