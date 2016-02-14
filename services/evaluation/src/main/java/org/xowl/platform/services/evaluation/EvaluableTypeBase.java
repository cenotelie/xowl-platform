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

package org.xowl.platform.services.evaluation;

import org.xowl.infra.store.IOUtils;
import org.xowl.platform.kernel.artifacts.ArtifactArchetype;

/**
 * Base implementation for representation of evaluable element types
 *
 * @author Laurent Wouters
 */
public abstract class EvaluableTypeBase implements EvaluableType {
    /**
     * The type's unique identifier
     */
    protected final String identifier;
    /**
     * The type's name
     */
    protected final String name;

    /**
     * Initializes this element
     *
     * @param identifier The type's unique identifier
     * @param name       The type's name
     */
    public EvaluableTypeBase(String identifier, String name) {
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
                IOUtils.escapeStringJSON(getClass().getCanonicalName()) +
                "\", \"id\": \"" +
                IOUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(name) +
                "\"}";
    }
}
