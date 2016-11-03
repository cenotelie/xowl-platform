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

package org.xowl.platform.services.evaluation;

import org.xowl.infra.utils.TextUtils;

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
    protected final Collection<String> parameters;

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

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<String> getParameters() {
        return Collections.unmodifiableCollection(parameters);
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(getClass().getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"parameters\": [");
        boolean first = true;
        for (String param : parameters) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(param));
            builder.append("\"");
        }
        builder.append("]}");
        return builder.toString();
    }
}
