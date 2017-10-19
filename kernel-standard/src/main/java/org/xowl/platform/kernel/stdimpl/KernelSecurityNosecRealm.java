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

package org.xowl.platform.kernel.stdimpl;

import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.commons.utils.api.ReplyUnsupported;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.platform.PlatformGroup;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.platform.PlatformUserBase;
import org.xowl.platform.kernel.security.SecurityRealm;

import java.util.Collection;
import java.util.Collections;

/**
 * A realm with no security
 *
 * @author Laurent Wouters
 */
public class KernelSecurityNosecRealm implements SecurityRealm {
    /**
     * The internal representation of a user for this realm
     */
    private static class User extends PlatformUserBase {
        /**
         * Initializes this user
         *
         * @param identifier The identifier of the user
         */
        public User(String identifier) {
            super(identifier, identifier);
        }

        @Override
        public Collection<PlatformRole> getRoles() {
            return Collections.emptyList();
        }
    }

    /**
     * Initializes this realm provider
     */
    public KernelSecurityNosecRealm() {
    }

    @Override
    public String getIdentifier() {
        return KernelSecurityNosecRealm.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - No Security Realm";
    }

    @Override
    public PlatformUser authenticate(String login, String password) {
        return new User(login);
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
    public Collection<PlatformGroup> getGroups() {
        return Collections.emptyList();
    }

    @Override
    public Collection<PlatformRole> getRoles() {
        return Collections.emptyList();
    }

    @Override
    public PlatformUser getUser(String identifier) {
        return null;
    }

    @Override
    public PlatformGroup getGroup(String identifier) {
        return null;
    }

    @Override
    public PlatformRole getRole(String identifier) {
        return null;
    }

    @Override
    public Reply createUser(String identifier, String name, String key) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply createGroup(String identifier, String name, String adminId) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply createRole(String identifier, String name) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply renameUser(String identifier, String name) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply renameGroup(String identifier, String name) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply renameRole(String identifier, String name) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply deleteUser(String identifier) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply deleteGroup(String identifier) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply deleteRole(String identifier) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply changeUserKey(String identifier, String oldKey, String newKey) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply resetUserKey(String identifier, String newKey) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply addUserToGroup(String user, String group) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply addAdminToGroup(String user, String group) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply removeUserFromGroup(String user, String group) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply removeAdminFromGroup(String user, String group) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply assignRoleToUser(String user, String role) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply assignRoleToGroup(String group, String role) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply unassignRoleToUser(String user, String role) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply unassignRoleToGroup(String group, String role) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply addRoleImplication(String sourceRole, String targetRole) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply removeRoleImplication(String sourceRole, String targetRole) {
        return ReplyUnsupported.instance();
    }
}
