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

package org.xowl.platform.kernel;

import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.security.ServiceActionSecurityPolicyNone;
import org.xowl.platform.kernel.security.ServiceActionSecurityPolicyRole;
import org.xowl.platform.kernel.security.ServiceActionSecurityPolicyRoleAdmin;

/**
 * Represents a user action for a service
 *
 * @author Laurent Wouters
 */
public class ServiceAction implements Identifiable, Serializable {
    /**
     * The default authorization policies
     */
    private static final String[] DEFAULT_POLICIES = new String[]{
            ServiceActionSecurityPolicyNone.class.getCanonicalName(),
            ServiceActionSecurityPolicyRole.class.getCanonicalName(),
            ServiceActionSecurityPolicyRoleAdmin.class.getCanonicalName()
    };

    /**
     * The identifier for this action
     */
    protected final String identifier;
    /**
     * The name of this action
     */
    protected final String name;
    /**
     * The identifiers of the possible authorization policies for this action
     */
    protected final String[] policies;

    /**
     * Initializes this action
     *
     * @param identifier The identifier for this action
     * @param name       The name of this action
     */
    public ServiceAction(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
        this.policies = DEFAULT_POLICIES;
    }

    /**
     * Initializes this action
     *
     * @param identifier The identifier for this action
     * @param name       The name of this action
     * @param policies   The identifiers of the possible authorization policies for this action
     */
    public ServiceAction(String identifier, String name, String... policies) {
        this.identifier = identifier;
        this.name = name;
        this.policies = policies;
    }

    /**
     * Gets the identifiers of the possible authorization policies for this action
     *
     * @return The identifiers of the possible authorization policies for this action
     */
    public String[] getPolicies() {
        return policies;
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
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(ServiceAction.class.getCanonicalName()));
        builder.append("\", \"identifier\":\"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"policies\": [");
        for (int i = 0; i != policies.length; i++) {
            if (i != 0)
                builder.append(", ");
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(policies[i]));
            builder.append("\"");
        }
        builder.append("]}");
        return builder.toString();
    }
}
