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

package org.xowl.platform.kernel.security;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.Registrable;
import org.xowl.platform.kernel.platform.PlatformGroup;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformUser;

import java.util.Collection;

/**
 * Represents a security realm for users, groups and roles on this platform
 *
 * @author Laurent Wouters
 */
public interface Realm extends Registrable {
    /**
     * The identifier of the property for the realm identifier
     */
    String PROPERTY_ID = "realmId";

    /**
     * On a new request, performs the authentication of a user
     *
     * @param login    The identifier of a user
     * @param password The key used to identified the user (e.g. a password)
     * @return The authenticated user, if the authentication is successful, null otherwise
     */
    PlatformUser authenticate(String login, String password);

    /**
     * Event when the request terminated
     *
     * @param userId The identifier of a user
     */
    void onRequestEnd(String userId);

    /**
     * Checks whether a specified user has a role
     *
     * @param userId The identifier of a user
     * @param roleId The identifier of a role
     * @return Whether the user has the role
     */
    boolean checkHasRole(String userId, String roleId);

    /**
     * Gets all the users in this realm
     *
     * @return The users in this realm
     */
    Collection<PlatformUser> getUsers();

    /**
     * Gets all the groups in this realm
     *
     * @return The groups in this realm
     */
    Collection<PlatformGroup> getGroups();

    /**
     * Gets all the roles in this realm
     *
     * @return The roles in this realm
     */
    Collection<PlatformRole> getRoles();

    /**
     * Gets a user for the specified identifier
     *
     * @param identifier The identifier of a user
     * @return The associated user
     */
    PlatformUser getUser(String identifier);

    /**
     * Gets a group for the specified identifier
     *
     * @param identifier The identifier of a group
     * @return The associated group
     */
    PlatformGroup getGroup(String identifier);

    /**
     * Gets a role for the specified identifier
     *
     * @param identifier The identifier of a role
     * @return The associated role
     */
    PlatformRole getRole(String identifier);

    /**
     * Creates a new user
     *
     * @param identifier The identifier for the new user
     * @param name       The human-readable name for the user
     * @param key        The initial key associated to the user
     * @return The protocol reply
     */
    XSPReply createUser(String identifier, String name, String key);

    /**
     * Creates a new group
     *
     * @param identifier The identifier for the new group
     * @param name       The human-readable name for the group
     * @param adminId    The identifier of the initial administrator for the group
     * @return The protocol reply
     */
    XSPReply createGroup(String identifier, String name, String adminId);

    /**
     * Creates a new role
     *
     * @param identifier The identifier for the role
     * @param name       The human-readable name for the role
     * @return The protocol reply
     */
    XSPReply createRole(String identifier, String name);

    /**
     * Renames a user
     *
     * @param identifier The identifier of the user to rename
     * @param name       The new name for the user
     * @return The protocol reply
     */
    XSPReply renameUser(String identifier, String name);

    /**
     * Renames a group
     *
     * @param identifier The identifier of the group to rename
     * @param name       The new name for the group
     * @return The protocol reply
     */
    XSPReply renameGroup(String identifier, String name);

    /**
     * Renames a role
     *
     * @param identifier The identifier of the role to rename
     * @param name       The new name for the role
     * @return The protocol reply
     */
    XSPReply renameRole(String identifier, String name);

    /**
     * Deletes a user
     *
     * @param identifier The identifier of the user to delete
     * @return The protocol reply
     */
    XSPReply deleteUser(String identifier);

    /**
     * Deletes a group
     *
     * @param identifier The identifier of the group to delete
     * @return The protocol reply
     */
    XSPReply deleteGroup(String identifier);

    /**
     * Deletes a role
     *
     * @param identifier The identifier of the role to delete
     * @return The protocol reply
     */
    XSPReply deleteRole(String identifier);

    /**
     * Changes the key for a user
     *
     * @param identifier The identifier of the user
     * @param oldKey     The old key
     * @param newKey     The new key
     * @return The protocol reply
     */
    XSPReply changeUserKey(String identifier, String oldKey, String newKey);

    /**
     * Resets the key for a user
     *
     * @param identifier The identifier of a user
     * @param newKey     The new key
     * @return The protocol reply
     */
    XSPReply resetUserKey(String identifier, String newKey);

    /**
     * Adds a user as a member of a group
     *
     * @param user  The user to add
     * @param group The group
     * @return The protocol reply
     */
    XSPReply addUserToGroup(String user, String group);

    /**
     * Adds a user as an admin for a group
     *
     * @param user  The user to add
     * @param group The group
     * @return The protocol reply
     */
    XSPReply addAdminToGroup(String user, String group);

    /**
     * Removes a user from a group
     *
     * @param user  The user to remove
     * @param group The group
     * @return The protocol reply
     */
    XSPReply removeUserFromGroup(String user, String group);

    /**
     * Removes a user as an admin from a group
     *
     * @param user  The admin user to remove
     * @param group The group
     * @return The protocol reply
     */
    XSPReply removeAdminFromGroup(String user, String group);

    /**
     * Assigns a role to a user
     *
     * @param user The identifier of a user
     * @param role The role to assign
     * @return The protocol reply
     */
    XSPReply assignRoleToUser(String user, String role);

    /**
     * Assigns a role to a group
     *
     * @param group The identifier of a group
     * @param role  The role to assign
     * @return The protocol reply
     */
    XSPReply assignRoleToGroup(String group, String role);

    /**
     * Un-assigns a role to a user
     *
     * @param user The identifier of a user
     * @param role The role to un-assign
     * @return The protocol reply
     */
    XSPReply unassignRoleToUser(String user, String role);

    /**
     * Un-assigns a role to a group
     *
     * @param group The identifier of a group
     * @param role  The role to un-assign
     * @return The protocol reply
     */
    XSPReply unassignRoleToGroup(String group, String role);

    /**
     * Adds the implication of a role from another
     *
     * @param sourceRole The source (implying) role
     * @param targetRole The target (implied) role
     * @return The protocol reply
     */
    XSPReply addRoleImplication(String sourceRole, String targetRole);

    /**
     * Removes the implication of a role from another
     *
     * @param sourceRole The source (implying) role
     * @param targetRole The target (implied) role
     * @return The protocol reply
     */
    XSPReply removeRoleImplication(String sourceRole, String targetRole);
}
