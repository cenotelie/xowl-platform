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

import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.jobs.SecuredActionPolicyJobOwner;
import org.xowl.platform.kernel.security.*;

/**
 * Implements a provider of secured action policies for this bundle
 *
 * @author Laurent Wouters
 */
public class KernelSecuredActionPolicyProvider implements SecuredActionPolicyProvider {
    @Override
    public String getIdentifier() {
        return KernelSecuredActionPolicyProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Kernel Secured Action Policy Service";
    }

    @Override
    public SecuredActionPolicy instantiate(String policyId, Object... parameters) {
        if (SecuredActionPolicyNone.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyNone.INSTANCE;
        else if (SecuredActionPolicyRoleAdmin.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyRoleAdmin.INSTANCE;
        else if (SecuredActionPolicyRole.class.getCanonicalName().equals(policyId))
            return new SecuredActionPolicyRole((String) parameters[0]);
        else if (SecuredActionPolicyGroupAdmin.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyGroupAdmin.INSTANCE;
        else if (SecuredActionPolicyJobOwner.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyJobOwner.INSTANCE;
        return null;
    }
}
