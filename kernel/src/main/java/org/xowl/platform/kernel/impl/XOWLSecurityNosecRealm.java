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

package org.xowl.platform.kernel.impl;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.platform.kernel.security.Realm;

/**
 * A realm with no security
 *
 * @author Laurent Wouters
 */
public class XOWLSecurityNosecRealm implements Realm {
    /**
     * Initializes this realm provider
     */
    public XOWLSecurityNosecRealm() {
    }

    @Override
    public String getIdentifier() {
        return XOWLSecurityNosecRealm.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Nosec Realm";
    }

    @Override
    public XSPReply authenticate(String userId, char[] key) {
        return new XSPReplyResult<>(userId);
    }

    @Override
    public void onRequestEnd(String userId) {
        // do nothing
    }

    @Override
    public boolean checkHasRole(String userId, String roleId) {
        return true;
    }
}
