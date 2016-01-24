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

package org.xowl.platform.services.consistency.impl;

import org.xowl.infra.server.api.XOWLRule;
import org.xowl.infra.store.IOUtils;
import org.xowl.platform.services.consistency.ConsistencyRule;

/**
 * Implements a consistency rule on the xOWL platform
 *
 * @author Laurent Wouters
 */
class XOWLConsistencyRule implements ConsistencyRule {
    /**
     * The original rule
     */
    private final XOWLRule original;
    /**
     * The user-friendly name
     */
    private final String name;

    /**
     * Initializes this rule
     *
     * @param original The original rule
     * @param name     The user-friendly name
     */
    public XOWLConsistencyRule(XOWLRule original, String name) {
        this.original = original;
        this.name = name;
    }

    @Override
    public String getIdentifier() {
        return original.getName();
    }

    @Override
    public String getName() {
        return original.getName();
    }

    @Override
    public boolean isActive() {
        return original.isActive();
    }

    @Override
    public String getDefinition() {
        return original.getDefinition();
    }

    @Override
    public String getUserName() {
        return name;
    }

    @Override
    public String serializedString() {
        return original.getName();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                IOUtils.escapeStringJSON(XOWLConsistencyRule.class.getCanonicalName()) +
                "\", \"id\": \"" +
                IOUtils.escapeStringJSON(original.getName()) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(name) +
                "\", \"definition\": \"" +
                IOUtils.escapeStringJSON(original.getDefinition()) +
                "\", \"isActive\": " +
                Boolean.toString(original.isActive()) +
                "}";
    }
}
