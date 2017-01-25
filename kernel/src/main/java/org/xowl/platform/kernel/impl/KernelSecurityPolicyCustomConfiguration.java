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

package org.xowl.platform.kernel.impl;

import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.collections.Couple;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredActionPolicy;
import org.xowl.platform.kernel.security.SecurityPolicyConfiguration;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a snapshot of the configuration of a custom security policy
 *
 * @author Laurent Wouters
 */
public class KernelSecurityPolicyCustomConfiguration implements SecurityPolicyConfiguration {
    /**
     * The parts of this configuration
     */
    private final Collection<Couple<SecuredAction, SecuredActionPolicy>> parts;

    /**
     * Initializes this configuration
     */
    public KernelSecurityPolicyCustomConfiguration() {
        this.parts = new ArrayList<>();
    }

    /**
     * Adds a configuration for a secured action
     *
     * @param action The secured action
     * @param policy The associated policy
     */
    public void add(SecuredAction action, SecuredActionPolicy policy) {
        this.parts.add(new Couple<>(action, policy));
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(KernelSecurityPolicyCustomConfiguration.class.getCanonicalName()));
        builder.append("\", \"parts\": [");
        boolean first = true;
        for (Couple<SecuredAction, SecuredActionPolicy> couple : parts) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("{\"action\": ");
            builder.append(couple.x.serializedJSON());
            builder.append(", \"policy\": ");
            builder.append(couple.y.serializedJSON());
            builder.append("}");
        }
        builder.append("]}");
        return builder.toString();
    }
}
