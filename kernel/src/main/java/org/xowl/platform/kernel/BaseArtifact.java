/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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

package org.xowl.platform.kernel;

import org.xowl.store.IOUtils;

/**
 * The base implementation of an artifact
 *
 * @author Laurent Wouters
 */
public abstract class BaseArtifact implements Artifact {
    /**
     * The identifier for this baseline
     */
    protected final String identifier;
    /**
     * The name of this baseline
     */
    protected final String name;

    /**
     * Initializes this data package
     *
     * @param identifier The identifier for this artifact
     * @param name       The name for this artifact
     */
    public BaseArtifact(String identifier, String name) {
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
        return "{\"identifier\": \""
                + IOUtils.escapeStringJSON(identifier)
                + "\", \"name\":"
                + IOUtils.escapeStringJSON(name)
                + "\", \"type\": \""
                + IOUtils.escapeStringJSON(getMIMEType())
                + "\"}";
    }
}
