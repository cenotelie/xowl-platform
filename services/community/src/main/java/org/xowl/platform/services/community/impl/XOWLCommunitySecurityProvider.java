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

package org.xowl.platform.services.community.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.security.SecuredActionPolicy;
import org.xowl.platform.kernel.security.SecuredActionPolicyProvider;
import org.xowl.platform.services.community.bots.SecuredActionPolicyIsRunningBot;
import org.xowl.platform.services.community.profiles.SecuredActionPolicyIsProfileOwner;

/**
 * Implements a provider for security components for the bundle
 *
 * @author Laurent Wouters
 */
public class XOWLCommunitySecurityProvider implements SecuredActionPolicyProvider {
    @Override
    public String getIdentifier() {
        return XOWLCommunitySecurityProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Community Security Provider";
    }

    @Override
    public SecuredActionPolicy newPolicy(String policyId, ASTNode definition) {
        if (SecuredActionPolicyIsProfileOwner.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyIsProfileOwner.INSTANCE;
        if (SecuredActionPolicyIsRunningBot.class.getCanonicalName().equals(policyId))
            return SecuredActionPolicyIsRunningBot.INSTANCE;
        return null;
    }
}