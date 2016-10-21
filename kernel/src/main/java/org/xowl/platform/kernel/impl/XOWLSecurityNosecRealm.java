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
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.store.IOUtils;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.platform.PlatformUserGroup;
import org.xowl.platform.kernel.platform.PlatformUserRole;
import org.xowl.platform.kernel.security.Realm;

import java.util.Collection;
import java.util.Collections;

/**
 * A realm with no security
 *
 * @author Laurent Wouters
 */
public class XOWLSecurityNosecRealm implements Realm {
    /**
     * The internal representation of a user for this realm
     */
    private static class User implements PlatformUser {
        /**
         * The identifier of the user
         */
        private final String identifier;

        /**
         * Initializes this user
         *
         * @param identifier The identifier of the user
         */
        public User(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String getName() {
            return identifier;
        }

        @Override
        public Collection<PlatformUserRole> getRoles() {
            return Collections.emptyList();
        }

        @Override
        public String serializedString() {
            return identifier;
        }

        @Override
        public String serializedJSON() {
            return "{\"type\": \"" +
                    IOUtils.escapeStringJSON(PlatformUser.class.getCanonicalName()) +
                    "\", \"identifier\": \"" +
                    IOUtils.escapeStringJSON(identifier) +
                    "\",  \"name\": \"" +
                    IOUtils.escapeStringJSON(identifier) +
                    "\"}";
        }

        @Override
        public String toString() {
            return identifier;
        }
    }

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
    public PlatformUser authenticate(String userId, char[] key) {
        return new User(userId);
    }

    @Override
    public void onRequestEnd(String userId) {
        // do nothing
    }

    @Override
    public boolean checkHasRole(String userId, String roleId) {
        return true;
    }

    @Override
    public Collection<PlatformUser> getUsers() {
        return Collections.emptyList();
    }

    @Override
    public Collection<PlatformUserGroup> getGroups() {
        return Collections.emptyList();
    }

    @Override
    public Collection<PlatformUserRole> getRoles() {
        return Collections.emptyList();
    }

    @Override
    public PlatformUser getUser(String identifier) {
        return null;
    }

    @Override
    public PlatformUserGroup getGroup(String identifier) {
        return null;
    }

    @Override
    public PlatformUserRole getRole(String identifier) {
        return null;
    }

    @Override
    public XSPReply createUser(String identifier, String name, String key) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply createGroup(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply createRole(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply renameUser(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply renameGroup(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply renameRole(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply deleteUser(String identifier) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply deleteGroup(String identifier) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply deleteRole(String identifier) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply changeUserKey(String identifier, String oldKey, String newKey) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply resetUserKey(String identifier, String newKey) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply addUserToGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply addAdminToGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply removeUserFromGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply removeAdminFromGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply assignRoleToUser(String user, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply assignRoleToGroup(String group, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply unassignRoleToUser(String user, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply unassignRoleToGroup(String group, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply addRoleImplication(String sourceRole, String targetRole) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply removeRoleImplication(String sourceRole, String targetRole) {
        return XSPReplyUnsupported.instance();
    }
}
