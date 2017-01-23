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
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Represents an authorization policy that requires the user to be the owner of the associated job
 *
 * @author Laurent Wouters
 */
public class ServiceActionSecurityPolicyJobOwner extends ServiceActionSecurityPolicyBase {
    /**
     * Initializes this policy
     */
    public ServiceActionSecurityPolicyJobOwner() {
        super(ServiceActionSecurityPolicyGroupAdmin.class.getCanonicalName(), "Job owner policy");
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(ServiceActionSecurityPolicyJobOwner.class.getCanonicalName()) +
                "\", \"identifier\":\"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\"}";
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, ServiceAction action) {
        return false;
    }

    @Override
    public boolean isAuthorized(SecurityService securityService, PlatformUser user, ServiceAction action, Object data) {
        return data instanceof Job && ((Job) data).getOwner() == user;
    }
}
