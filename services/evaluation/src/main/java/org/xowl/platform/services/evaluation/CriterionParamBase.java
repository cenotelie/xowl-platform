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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.store.IOUtils;

/**
 * Base implementation of a criterion parameter
 *
 * @author Laurent Wouters
 */
public class CriterionParamBase implements CriterionParam {
    /**
     * The parameter's identifier
     */
    protected final String identifier;
    /**
     * The parameter's name
     */
    protected final String name;
    /**
     * Whether the parameter is required
     */
    protected final boolean isRequired;

    /**
     * Initializes this parameter
     *
     * @param identifier The parameter's identifier
     * @param name       The parameter's name
     * @param isRequired Whether the parameter is required
     */
    public CriterionParamBase(String identifier, String name, boolean isRequired) {
        this.identifier = identifier;
        this.name = name;
        this.isRequired = isRequired;
    }

    /**
     * Initializes this parameter
     *
     * @param definition The definition of this parameter
     */
    public CriterionParamBase(ASTNode definition) {
        String identifier = null;
        String name = null;
        String isRequired = null;
        for (ASTNode member : definition.getChildren()) {
            String memberName = IOUtils.unescape(member.getChildren().get(0).getValue());
            memberName = memberName.substring(1, memberName.length() - 1);
            switch (memberName) {
                case "id":
                    identifier = IOUtils.unescape(member.getChildren().get(1).getValue());
                    identifier = identifier.substring(1, identifier.length() - 1);
                    break;
                case "name":
                    name = IOUtils.unescape(member.getChildren().get(1).getValue());
                    name = name.substring(1, name.length() - 1);
                    break;
                case "isRequired":
                    isRequired = IOUtils.unescape(member.getChildren().get(1).getValue());
                    break;
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.isRequired = "true".equalsIgnoreCase(isRequired);
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
    public boolean isRequired() {
        return isRequired;
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
                "\", \"isRequired\": " +
                Boolean.toString(isRequired) +
                "}";
    }
}
