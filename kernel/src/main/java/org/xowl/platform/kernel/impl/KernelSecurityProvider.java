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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.config.Section;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.jobs.SecuredActionPolicyJobOwner;
import org.xowl.platform.kernel.security.*;

/**
 * Implements a provider for security components for the kernel
 *
 * @author Laurent Wouters
 */
public class KernelSecurityProvider implements SecuredActionPolicyProvider, SecurityPolicyProvider, SecurityRealmProvider {
    @Override
    public String getIdentifier() {
        return KernelSecurityProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Kernel Security Provider";
    }

    @Override
    public SecuredActionPolicy newPolicy(String policyId, ASTNode definition) {
        if (SecuredActionPolicyNone.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyNone.INSTANCE;
        else if (SecuredActionPolicyRoleAdmin.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyRoleAdmin.INSTANCE;
        else if (SecuredActionPolicyRole.class.getCanonicalName().equals(policyId))
            return new SecuredActionPolicyRole(definition);
        else if (SecuredActionPolicyGroupAdmin.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyGroupAdmin.INSTANCE;
        else if (SecuredActionPolicyJobOwner.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyJobOwner.INSTANCE;
        return null;
    }

    @Override
    public SecurityRealm newRealm(String identifier, Section configuration) {
        if (KernelSecurityNosecRealm.class.getCanonicalName().equals(identifier))
            return new KernelSecurityNosecRealm();
        return null;
    }

    @Override
    public SecurityPolicy newPolicy(String identifier, Section configuration) {
        if (KernelSecurityPolicyAuthenticated.class.getCanonicalName().equals(identifier))
            return new KernelSecurityPolicyAuthenticated();
        if (KernelSecurityPolicyCustom.class.getCanonicalName().equals(identifier))
            return new KernelSecurityPolicyCustom(configuration);
        return null;
    }
}
