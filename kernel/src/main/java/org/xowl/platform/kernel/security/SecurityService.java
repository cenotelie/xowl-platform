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

import fr.cenotelie.commons.utils.api.Reply;
import org.xowl.platform.kernel.platform.PlatformUser;

/**
 * Manages the security on the platform
 *
 * @author Laurent Wouters
 */
public interface SecurityService extends SecuredService {
    /**
     * Service action to get the configuration of the security policy
     */
    SecuredAction ACTION_GET_POLICY = new SecuredAction(SecurityService.class.getCanonicalName() + ".GetPolicy", "Security Service - Get Security Policy", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to set the policy for a secured action
     */
    SecuredAction ACTION_SET_POLICY = new SecuredAction(SecurityService.class.getCanonicalName() + ".SetPolicy", "Security Service - Set Security Policy", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to assume the identity of another user
     */
    SecuredAction ACTION_CHANGE_ID = new SecuredAction(SecurityService.class.getCanonicalName() + ".ChangeId", "Security Service - Change User Identity", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to create a user
     */
    SecuredAction ACTION_CREATE_USER = new SecuredAction(SecurityService.class.getCanonicalName() + ".CreateUser", "Security Service - Create User", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to create a group
     */
    SecuredAction ACTION_CREATE_GROUP = new SecuredAction(SecurityService.class.getCanonicalName() + ".CreateGroup", "Security Service - Create Group", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to create a role
     */
    SecuredAction ACTION_CREATE_ROLE = new SecuredAction(SecurityService.class.getCanonicalName() + ".CreateRole", "Security Service - Create Role", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to rename a user
     */
    SecuredAction ACTION_RENAME_USER = new SecuredAction(SecurityService.class.getCanonicalName() + ".RenameUser", "Security Service - Rename User", SecuredActionPolicyIsSelf.DESCRIPTOR);
    /**
     * Service action to rename a group
     */
    SecuredAction ACTION_RENAME_GROUP = new SecuredAction(SecurityService.class.getCanonicalName() + ".RenameGroup", "Security Service - Rename Group", SecuredActionPolicyIsGroupAdmin.DESCRIPTOR);
    /**
     * Service action to rename a role
     */
    SecuredAction ACTION_RENAME_ROLE = new SecuredAction(SecurityService.class.getCanonicalName() + ".RenameRole", "Security Service - Rename Role", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to delete a user
     */
    SecuredAction ACTION_DELETE_USER = new SecuredAction(SecurityService.class.getCanonicalName() + ".DeleteUser", "Security Service - Delete User", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to delete a group
     */
    SecuredAction ACTION_DELETE_GROUP = new SecuredAction(SecurityService.class.getCanonicalName() + ".DeleteGroup", "Security Service - Delete Group", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to delete a role
     */
    SecuredAction ACTION_DELETE_ROLE = new SecuredAction(SecurityService.class.getCanonicalName() + ".DeleteRole", "Security Service - Delete Role", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to update the key for a user
     */
    SecuredAction ACTION_UPDATE_USER_KEY = new SecuredAction(SecurityService.class.getCanonicalName() + ".UpdateUserKey", "Security Service - Update User Key", SecuredActionPolicyIsSelf.DESCRIPTOR);
    /**
     * Service action to reset the key for a user
     */
    SecuredAction ACTION_RESET_USER_KEY = new SecuredAction(SecurityService.class.getCanonicalName() + ".ResetUserKey", "Security Service - Reset User Key", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to add a user to a group
     */
    SecuredAction ACTION_ADD_USER_TO_GROUP = new SecuredAction(SecurityService.class.getCanonicalName() + ".AddUserToGroup", "Security Service - Add User to Group", SecuredActionPolicyIsGroupAdmin.DESCRIPTOR);
    /**
     * Service action to remove a user from a group
     */
    SecuredAction ACTION_REMOVE_USER_FROM_GROUP = new SecuredAction(SecurityService.class.getCanonicalName() + ".RemoveUserFromGroup", "Security Service - Remove User from Group", SecuredActionPolicyIsGroupAdmin.DESCRIPTOR);
    /**
     * Service action to add an admin to a group
     */
    SecuredAction ACTION_ADD_ADMIN_TO_GROUP = new SecuredAction(SecurityService.class.getCanonicalName() + ".AddAdminToGroup", "Security Service - Add Admin to Group", SecuredActionPolicyIsGroupAdmin.DESCRIPTOR);
    /**
     * Service action to remove an admin from a group
     */
    SecuredAction ACTION_REMOVE_ADMIN_FROM_GROUP = new SecuredAction(SecurityService.class.getCanonicalName() + ".RemoveAdminFromGroup", "Security Service - Remove Admin from Group", SecuredActionPolicyIsGroupAdmin.DESCRIPTOR);
    /**
     * Service action to assign a role to a user
     */
    SecuredAction ACTION_ASSIGN_ROLE_TO_USER = new SecuredAction(SecurityService.class.getCanonicalName() + ".AssignRoleToUser", "Security Service - Assign Role to User", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to un-assign a role to a user
     */
    SecuredAction ACTION_UNASSIGN_ROLE_TO_USER = new SecuredAction(SecurityService.class.getCanonicalName() + ".UnassignRoleToUser", "Security Service - Un-assign Role to User", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to assign a role to a group
     */
    SecuredAction ACTION_ASSIGN_ROLE_TO_GROUP = new SecuredAction(SecurityService.class.getCanonicalName() + ".AssignRoleToGroup", "Security Service - Assign Role to Group", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to un-assign a role to a group
     */
    SecuredAction ACTION_UNASSIGN_ROLE_TO_GROUP = new SecuredAction(SecurityService.class.getCanonicalName() + ".UnassignRoleToGroup", "Security Service - Un-assign Role to Group", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to add a role implication
     */
    SecuredAction ACTION_ADD_ROLE_IMPLICATION = new SecuredAction(SecurityService.class.getCanonicalName() + ".AddRoleImplication", "Security Service - Add Role Implication", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Service action to remove a role implication
     */
    SecuredAction ACTION_REMOVE_ROLE_IMPLICATION = new SecuredAction(SecurityService.class.getCanonicalName() + ".RemoveRoleImplication", "Security Service - Remove Role Implication", SecuredActionPolicyIsPlatformAdmin.DESCRIPTOR);
    /**
     * Action to access to a secured resource
     */
    SecuredAction ACTION_RESOURCE_ACCESS = new SecuredAction(SecurityService.class.getCanonicalName() + ".ResourceAccess", "Security Service - Secured Resource Access", SecuredActionPolicyIsInSharing.DESCRIPTOR);
    /**
     * Action to get the security descriptor of a secured resource
     */
    SecuredAction ACTION_RESOURCE_GET_DESCRIPTOR = new SecuredAction(SecurityService.class.getCanonicalName() + ".GetResourceDescriptor", "Security Service - Secured Resource Get Descriptor", SecuredActionPolicyIsInSharing.DESCRIPTOR);
    /**
     * Action to manage the ownership and sharing of a secured resource
     */
    SecuredAction ACTION_RESOURCE_MANAGE = new SecuredAction(SecurityService.class.getCanonicalName() + ".ManageResource", "Security Service - Secured Resource Management", SecuredActionPolicyIsResourceOwner.DESCRIPTOR);

    /**
     * The actions for the security service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_GET_POLICY,
            ACTION_SET_POLICY,
            ACTION_CHANGE_ID,
            ACTION_CREATE_USER,
            ACTION_CREATE_GROUP,
            ACTION_CREATE_ROLE,
            ACTION_RENAME_USER,
            ACTION_RENAME_GROUP,
            ACTION_RENAME_ROLE,
            ACTION_DELETE_USER,
            ACTION_DELETE_GROUP,
            ACTION_DELETE_ROLE,
            ACTION_UPDATE_USER_KEY,
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
            ACTION_REMOVE_ROLE_IMPLICATION,
            ACTION_RESOURCE_ACCESS,
            ACTION_RESOURCE_GET_DESCRIPTOR,
            ACTION_RESOURCE_MANAGE
    };

    /**
     * Gets the realm of this platform
     *
     * @return The realm of this platform
     */
    SecurityRealm getRealm();

    /**
     * Gets the current authorization policy for this platform
     *
     * @return The current authorization policy for this platform
     */
    SecurityPolicy getPolicy();

    /**
     * Gets the security tokens management service for this platform
     *
     * @return The security tokens management service for this platform
     */
    SecurityTokenService getTokens();

    /**
     * Gets the manager for the secured resources
     *
     * @return The manager for the secured resources
     */
    SecuredResourceManager getSecuredResources();

    /**
     * Performs the initial authentication of a client
     *
     * @param client   The requesting client
     * @param userId   The identifier of a user
     * @param password The key used to identified the user (e.g. a password)
     * @return If the operation succeed, the authentication token
     */
    Reply login(String client, String userId, String password);

    /**
     * Performs the logout of a client
     *
     * @return Whether the operation succeed
     */
    Reply logout();

    /**
     * Performs the authentication of a user on the current thread
     *
     * @param client The requesting client
     * @param token  The authentication token
     * @return Whether the operation succeed
     */
    Reply authenticate(String client, String token);

    /**
     * Forces the authentication of the specified user on the current thread
     *
     * @param user The user to authenticate
     */
    Reply authenticate(PlatformUser user);

    /**
     * Gets the currently authenticated user on the current thread, if any
     *
     * @return The currently authenticated user, or null if there is none
     */
    PlatformUser getCurrentUser();

    /**
     * Checks the authorization policy for the specified action
     *
     * @param action The requested action
     * @return The protocol reply
     */
    Reply checkAction(SecuredAction action);

    /**
     * Checks the authorization policy for the specified action
     *
     * @param action The requested action
     * @param data   Custom data that may be required to make a decision
     * @return The protocol reply
     */
    Reply checkAction(SecuredAction action, Object data);
}
