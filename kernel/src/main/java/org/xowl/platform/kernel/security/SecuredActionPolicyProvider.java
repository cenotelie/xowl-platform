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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.Registrable;

/**
 * A providers for instances of secured action policies
 *
 * @author Laurent Wouters
 */
public interface SecuredActionPolicyProvider extends Registrable {
    /**
     * Tries to instantiate a policy
     *
     * @param policyId   The identifier of the policy
     * @param definition The serialized definition
     * @return The policy, or null if it cannot be instantiated
     */
    SecuredActionPolicy newPolicy(String policyId, ASTNode definition);
}
