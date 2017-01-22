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

package org.xowl.platform.kernel.security;

import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.ServiceAction;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Basic implementation of an authorization policy
 *
 * @author Laurent Wouters
 */
public abstract class ServiceActionSecurityPolicyBase implements ServiceActionSecurityPolicy {
    /**
     * The identifier for this policy
     */
    protected final String identifier;
    /**
     * The name of this policy
     */
    protected final String name;

    /**
     * Initializes this policy
     *
     * @param identifier The identifier for this policy
     * @param name       The name of this policy
     */
    protected ServiceActionSecurityPolicyBase(String identifier, String name) {
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
                TextUtils.escapeStringJSON(ServiceActionSecurityPolicy.class.getCanonicalName()) +
                "\", \"identifier\":\"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\"}";
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, ServiceAction action, Object data) {
        return isAuthorized(securityService, user, action);
    }
}
