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
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.ServiceAction;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Manages the security on the platform
 *
 * @author Laurent Wouters
 */
public interface SecurityService extends Service {
    /**
     * The name of the cookie for the authentication token
     */
    String AUTH_TOKEN = "__Secure-xOWL-Platform";
    /**
     * The URI of the API for the login service
     */
    String URI_LOGIN = "/api/kernel/security/login";

    /**
     * Service action to create a user
     */
    ServiceAction ACTION_CREATE_USER = new ServiceAction(SecurityService.class.getCanonicalName() + ".CreateUser", "Security Service - Create User");
    /**
     * Service action to create a group
     */
    ServiceAction ACTION_CREATE_GROUP = new ServiceAction(SecurityService.class.getCanonicalName() + ".CreateGroup", "Security Service - Create Group");
    /**
     * Service action to create a role
     */
    ServiceAction ACTION_CREATE_ROLE = new ServiceAction(SecurityService.class.getCanonicalName() + ".CreateRole", "Security Service - Create Role");
    /**
     * Service action to rename a user
     */
    ServiceAction ACTION_RENAME_USER = new ServiceAction(SecurityService.class.getCanonicalName() + ".RenameUser", "Security Service - Rename User");
    /**
     * Service action to rename a group
     */
    ServiceAction ACTION_RENAME_GROUP = new ServiceAction(SecurityService.class.getCanonicalName() + ".RenameGroup", "Security Service - Rename Group");
    /**
     * Service action to rename a role
     */
    ServiceAction ACTION_RENAME_ROLE = new ServiceAction(SecurityService.class.getCanonicalName() + ".RenameRole", "Security Service - Rename Role");
    /**
     * Service action to delete a user
     */
    ServiceAction ACTION_DELETE_USER = new ServiceAction(SecurityService.class.getCanonicalName() + ".DeleteUser", "Security Service - Delete User");
    /**
     * Service action to delete a group
     */
    ServiceAction ACTION_DELETE_GROUP = new ServiceAction(SecurityService.class.getCanonicalName() + ".DeleteGroup", "Security Service - Delete Group");
    /**
     * Service action to delete a role
     */
    ServiceAction ACTION_DELETE_ROLE = new ServiceAction(SecurityService.class.getCanonicalName() + ".DeleteRole", "Security Service - Delete Role");
    /**
     * Service action to reset the key for a user
     */
    ServiceAction ACTION_RESET_USER_KEY = new ServiceAction(SecurityService.class.getCanonicalName() + ".ResetUserKey", "Security Service - Reset Use Key");
    /**
     * Service action to add a user to a group
     */
    ServiceAction ACTION_ADD_USER_TO_GROUP = new ServiceAction(SecurityService.class.getCanonicalName() + ".AddUserToGroup", "Security Service - Add User to Group");
    /**
     * Service action to remove a user from a group
     */
    ServiceAction ACTION_REMOVE_USER_FROM_GROUP = new ServiceAction(SecurityService.class.getCanonicalName() + ".RemoveUserFromGroup", "Security Service - Remove User from Group");
    /**
     * Service action to add an admin to a group
     */
    ServiceAction ACTION_ADD_ADMIN_TO_GROUP = new ServiceAction(SecurityService.class.getCanonicalName() + ".AddAdminToGroup", "Security Service - Add Admin to Group");
    /**
     * Service action to remove an admin from a group
     */
    ServiceAction ACTION_REMOVE_ADMIN_FROM_GROUP = new ServiceAction(SecurityService.class.getCanonicalName() + ".RemoveAdminFromGroup", "Security Service - Remove Admin from Group");
    /**
     * Service action to assign a role to a user
     */
    ServiceAction ACTION_ASSIGN_ROLE_TO_USER = new ServiceAction(SecurityService.class.getCanonicalName() + ".AssignRoleToUser", "Security Service - Assign Role to User");
    /**
     * Service action to un-assign a role to a user
     */
    ServiceAction ACTION_UNASSIGN_ROLE_TO_USER = new ServiceAction(SecurityService.class.getCanonicalName() + ".UnassignRoleToUser", "Security Service - Un-assign Role to User");
    /**
     * Service action to assign a role to a group
     */
    ServiceAction ACTION_ASSIGN_ROLE_TO_GROUP = new ServiceAction(SecurityService.class.getCanonicalName() + ".AssignRoleToGroup", "Security Service - Assign Role to Group");
    /**
     * Service action to un-assign a role to a group
     */
    ServiceAction ACTION_UNASSIGN_ROLE_TO_GROUP = new ServiceAction(SecurityService.class.getCanonicalName() + ".UnassignRoleToGroup", "Security Service - Un-assign Role to Group");
    /**
     * Service action to add a role implication
     */
    ServiceAction ACTION_ADD_ROLE_IMPLICATION = new ServiceAction(SecurityService.class.getCanonicalName() + ".AddRoleImplication", "Security Service - Add Role Implication");
    /**
     * Service action to remove a role implication
     */
    ServiceAction ACTION_REMOVE_ROLE_IMPLICATION = new ServiceAction(SecurityService.class.getCanonicalName() + ".RemoveRoleImplication", "Security Service - Remove Role Implication");

    /**
     * The actions for the security service
     */
    ServiceAction[] ACTIONS = new ServiceAction[]{
            ACTION_CREATE_USER,
            ACTION_CREATE_GROUP,
            ACTION_CREATE_ROLE,
            ACTION_RENAME_USER,
            ACTION_RENAME_GROUP,
            ACTION_RENAME_ROLE,
            ACTION_DELETE_USER,
            ACTION_DELETE_GROUP,
            ACTION_DELETE_ROLE,
            ACTION_RESET_USER_KEY,
            ACTION_ADD_USER_TO_GROUP,
            ACTION_REMOVE_USER_FROM_GROUP,
            ACTION_ADD_ADMIN_TO_GROUP,
            ACTION_REMOVE_ADMIN_FROM_GROUP,
            ACTION_ASSIGN_ROLE_TO_USER,
            ACTION_UNASSIGN_ROLE_TO_USER,
            ACTION_ASSIGN_ROLE_TO_GROUP,
            ACTION_UNASSIGN_ROLE_TO_GROUP,
            ACTION_ADD_ROLE_IMPLICATION,
            ACTION_REMOVE_ROLE_IMPLICATION
    };

    /**
     * Gets the realm of this platform
     *
     * @return The realm of this platform
     */
    Realm getRealm();

    /**
     * Gets the current authorization policy for this platform
     *
     * @return The current authorization policy for this platform
     */
    SecurityPolicy getPolicy();

    /**
     * Performs the initial authentication of a client
     *
     * @param client   The requesting client
     * @param userId   The identifier of a user
     * @param password The key used to identified the user (e.g. a password)
     * @return If the operation succeed, the authentication token
     */
    XSPReply login(String client, String userId, String password);

    /**
     * Performs the logout of a client
     *
     * @param client The requesting client
     * @return Whether the operation succeed
     */
    XSPReply logout(String client);

    /**
     * Performs the authentication of a user on the current thread
     *
     * @param client The requesting client
     * @param token  The authentication token
     * @return Whether the operation succeed
     */
    XSPReply authenticate(String client, String token);

    /**
     * Forces the authentication of the specified user on the current thread
     *
     * @param user The user to authenticate
     */
    void authenticate(PlatformUser user);

    /**
     * Gets the currently authenticated user on the current thread, if any
     *
     * @return The currently authenticated user, or null if there is none
     */
    PlatformUser getCurrentUser();

    /**
     * Event when the request terminated
     *
     * @param client The requesting client
     */
    void onRequestEnd(String client);

    /**
     * Checks the authorization policy for the specified action
     *
     * @param action The requested action
     * @return The protocol reply
     */
    XSPReply checkAction(ServiceAction action);

    /**
     * Checks the authorization policy for the specified action
     *
     * @param action The requested action
     * @param data   Custom data that may be required to make a decision
     * @return The protocol reply
     */
    XSPReply checkAction(ServiceAction action, Object data);
}
