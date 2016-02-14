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
import org.xowl.infra.server.api.XOWLFactory;
import org.xowl.infra.server.api.XOWLUtils;
import org.xowl.infra.store.IOUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Base implementation of a criterion type
 *
 * @author Laurent Wouters
 */
public abstract class CriterionTypeBase implements CriterionType {
    /**
     * The criterion type's identifier
     */
    protected final String identifier;
    /**
     * The criterion type's name
     */
    protected final String name;
    /**
     * The parameters
     */
    protected final Collection<CriterionParam> parameters;

    /**
     * Initializes this criterion type
     *
     * @param identifier The this criterion type's identifier
     * @param name       The this criterion type's name
     */
    public CriterionTypeBase(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
        this.parameters = new ArrayList<>();
    }

    /**
     * Initializes this criterion type
     *
     * @param definition The definition of this criterion type
     * @param factory    The factory to use
     */
    public CriterionTypeBase(ASTNode definition, XOWLFactory factory) {
        String identifier = null;
        String name = null;
        this.parameters = new ArrayList<>();
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
                case "parameters": {
                    for (ASTNode paramNode : member.getChildren().get(1).getChildren()) {
                        Object obj = XOWLUtils.getJSONObject(paramNode, factory);
                        if (obj != null && obj instanceof CriterionParam)
                            parameters.add((CriterionParam) obj);
                    }
                }
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
    public Collection<CriterionParam> getParameters() {
        return Collections.unmodifiableCollection(parameters);
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(IOUtils.escapeStringJSON(getClass().getCanonicalName()));
        builder.append("\", \"id\": \"");
        builder.append(IOUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(name));
        builder.append("\", \"parameters\": [");
        boolean first = true;
        for (CriterionParam param : parameters) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(param.serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}
