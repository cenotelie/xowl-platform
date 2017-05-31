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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.config.Section;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.jobs.SecuredActionPolicyIsJobOwner;
import org.xowl.platform.kernel.security.*;

/**
 * Implements a provider for security components for the kernel
 *
 * @author Laurent Wouters
 */
class KernelSecurityProvider implements SecuredActionPolicyProvider, SecurityPolicyProvider, SecurityRealmProvider, SecurityTokenServiceProvider {
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
        if (SecuredActionPolicyAllowAll.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyAllowAll.INSTANCE;
        else if (SecuredActionPolicyDenyAll.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyDenyAll.INSTANCE;
        else if (SecuredActionPolicyIsPlatformAdmin.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyIsPlatformAdmin.INSTANCE;
        else if (SecuredActionPolicyHasRole.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyHasRole.newInstance(definition);
        else if (SecuredActionPolicyIsGroupAdmin.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyIsGroupAdmin.INSTANCE;
        else if (SecuredActionPolicyIsSelf.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyIsSelf.INSTANCE;
        else if (SecuredActionPolicyIsResourceOwner.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyIsResourceOwner.INSTANCE;
        else if (SecuredActionPolicyIsAllowedAccessToResource.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyIsAllowedAccessToResource.INSTANCE;
        else if (SecuredActionPolicyIsJobOwner.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyIsJobOwner.INSTANCE;
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

    @Override
    public SecurityTokenService newService(String identifier, Section configuration) {
        if (KernelSecurityTokenService.class.getCanonicalName().equals(identifier))
            return new KernelSecurityTokenService(configuration);
        return null;
    }
}
