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

package org.xowl.platform.satellites.remoteapi;

import org.xowl.infra.server.xsp.*;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.http.HttpConnection;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.platform.kernel.Deserializer;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecuredActionPolicy;

/**
 * The base API for accessing a remote platform
 *
 * @author Laurent Wouters
 */
public class RemotePlatform {
    /**
     * The connection to the platform
     */
    private final HttpConnection connection;
    /**
     * The deserializer to use
     */
    private final Deserializer deserializer;
    /**
     * The currently logged-in user
     */
    private PlatformUser currentUser;
    /**
     * The login for the current user
     */
    private String currentLogin;
    /**
     * The password for the current user
     */
    private String currentPassword;

    /**
     * Initializes this platform connection
     *
     * @param endpoint     The API endpoint (https://something:port/api)
     * @param deserializer The deserializer to use
     */
    public RemotePlatform(String endpoint, Deserializer deserializer) {
        this.connection = new HttpConnection(endpoint);
        this.deserializer = deserializer;
    }

    /**
     * Gets whether a user is logged-in
     *
     * @return Whether a user is logged-in
     */
    public boolean isLoggedIn() {
        return (currentUser != null);
    }

    /**
     * Gets the currently logged-in user, if any
     *
     * @return The currently logged-in user, if any
     */
    public PlatformUser getLoggedInUser() {
        return currentUser;
    }

    /**
     * Login a user
     *
     * @param login    The user to log in
     * @param password The user password
     * @return The protocol reply, or null if the client is banned
     */
    public XSPReply login(String login, String password) {
        HttpResponse response = connection.request("/kernel/security/login" +
                        "?login=" + URIUtils.encodeComponent(login),
                HttpConstants.METHOD_POST,
                password,
                HttpConstants.MIME_TEXT_PLAIN,
                HttpConstants.MIME_JSON
        );
        XSPReply reply = XSPReplyUtils.fromHttpResponse(response, deserializer);
        if (reply.isSuccess()) {
            currentUser = ((XSPReplyResult<PlatformUser>) reply).getData();
            currentLogin = login;
            currentPassword = password;
        } else {
            currentUser = null;
            currentLogin = null;
            currentPassword = null;
        }
        return reply;
    }

    /**
     * Logout the current user
     *
     * @return The protocol reply
     */
    public XSPReply logout() {
        if (currentUser == null)
            return XSPReplyNetworkError.instance();
        HttpResponse response = connection.request("/kernel/security/logout",
                HttpConstants.METHOD_POST,
                HttpConstants.MIME_JSON
        );
        XSPReply reply = XSPReplyUtils.fromHttpResponse(response, deserializer);
        currentUser = null;
        currentLogin = null;
        currentPassword = null;
        return reply;
    }

    /**
     * Gets the current configuration of the security policy
     *
     * @return The protocol reply
     */
    public XSPReply getSecurityPolicy() {
        return doRequest(
                "kernel/security/policy",
                HttpConstants.METHOD_GET);
    }

    /**
     * Sets the policy for a secured action
     *
     * @param actionId The identifier of the secured action
     * @param policy   The policy to set for the action
     * @return The protocol reply
     */
    public XSPReply setSecuredActionPolicy(String actionId, SecuredActionPolicy policy) {
        return doRequest(
                "kernel/security/policy/actions/" + URIUtils.encodeComponent(actionId),
                HttpConstants.METHOD_POST,
                policy);
    }

    /**
     * Gets the users on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getPlatformUsers() {
        return doRequest(
                "kernel/security/users",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets a specific user on the platform
     *
     * @param userId The identifier of the user to retrieve
     * @return The protocol reply
     */
    public XSPReply getPlatformUser(String userId) {
        return doRequest(
                "kernel/security/users/" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new user on the target platform
     *
     * @param userId   The identifier for the new user
     * @param name     The name for the user
     * @param password The password for the user
     * @return The protocol reply
     */
    public XSPReply createPlatformUser(String userId, String name, String password) {
        return doRequest(
                "kernel/security/users/" + URIUtils.encodeComponent(userId) + "?name=" + URIUtils.encodeComponent(name),
                HttpConstants.METHOD_PUT,
                password);
    }

    /**
     * Deletes a user on the target platform
     *
     * @param userId The identifier of the user to delete
     * @return The protocol reply
     */
    public XSPReply deletePlatformUser(String userId) {
        return doRequest(
                "kernel/security/users/" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Renames a user on the target platform
     *
     * @param userId The identifier of the user to rename
     * @param name   The new name for the user
     * @return The protocol reply
     */
    public XSPReply renamePlatformUser(String userId, String name) {
        return doRequest(
                "kernel/security/users/" + URIUtils.encodeComponent(userId) + "/rename" + "?name=" + URIUtils.encodeComponent(name),
                HttpConstants.METHOD_POST);
    }

    /**
     * Changes the password of a user
     *
     * @param userId      The identifier of the user
     * @param oldPassword The old password for verification
     * @param newPassword The new password
     * @return The protocol reply
     */
    public XSPReply changePlatformUserPassword(String userId, String oldPassword, String newPassword) {
        return doRequest(
                "kernel/security/users/" + URIUtils.encodeComponent(userId) + "/updateKey" + "?oldKey=" + URIUtils.encodeComponent(oldPassword),
                HttpConstants.METHOD_POST,
                newPassword);
    }

    /**
     * Resets the password of a user
     *
     * @param userId      The identifier of a user
     * @param newPassword The new password
     * @return The protocol reply
     */
    public XSPReply resetPlatformUserPassword(String userId, String newPassword) {
        return doRequest(
                "kernel/security/users/" + URIUtils.encodeComponent(userId) + "/resetKey",
                HttpConstants.METHOD_POST,
                newPassword);
    }

    /**
     * Gets the groups on the remote platform
     *
     * @return The protocol reply
     */
    public XSPReply getPlatformGroups() {
        return doRequest(
                "kernel/security/groups",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the a group on the target platform
     *
     * @param groupId The identifier of the group
     * @return The protocol reply
     */
    public XSPReply getPlatformGroup(String groupId) {
        return doRequest(
                "kernel/security/groups/" + URIUtils.encodeComponent(groupId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new group on the platform
     *
     * @param groupId The identifier of the new group
     * @param name    The name for the group
     * @param adminId The identifier of the first administrator for the group
     * @return The protocol reply
     */
    public XSPReply createPlatformGroup(String groupId, String name, String adminId) {
        return doRequest(
                "kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "?name=" + URIUtils.encodeComponent(name) + "&admin=" + URIUtils.encodeComponent(adminId),
                HttpConstants.METHOD_PUT);
    }

    /**
     * Deletes a group from the platform
     *
     * @param groupId The identifier of the group to delete
     * @return The protocol reply
     */
    public XSPReply deletePlatformGroup(String groupId) {
        return doRequest("kernel/security/groups/" + URIUtils.encodeComponent(groupId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Renames a group on the platform
     *
     * @param groupId The identifier of the group
     * @param name    The new name for the group
     * @return The protocol reply
     */
    public XSPReply renamePlatformGroup(String groupId, String name) {
        return doRequest(
                "kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/rename?name=" + URIUtils.encodeComponent(name),
                HttpConstants.METHOD_POST);
    }

    /**
     * Adds a member to a group
     *
     * @param groupId The identifier of the group
     * @param userId  The identifier of the user
     * @return The protocol reply
     */
    public XSPReply addMemberToPlatformGroup(String groupId, String userId) {
        return doRequest(
                "kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/addMember?user=" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Removes a member from a group
     *
     * @param groupId The identifier of the group
     * @param userId  The identifier of the user
     * @return The protocol reply
     */
    public XSPReply removeMemberFromPlatformGroup(String groupId, String userId) {
        return doRequest(
                "kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/removeMember?user=" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Adds an administrator member to a group
     *
     * @param groupId The identifier of the group
     * @param userId  The identifier of the user
     * @return The protocol reply
     */
    public XSPReply addAdminToPlatformGroup(String groupId, String userId) {
        return doRequest(
                "kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/addAdmin?user=" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Removes an administrator member from a group
     *
     * @param groupId The identifier of the group
     * @param userId  The identifier of the user
     * @return The protocol reply
     */
    public XSPReply removeAdminFromPlatformGroup(String groupId, String userId) {
        return doRequest(
                "kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/removeAdmin?user=" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the roles on the platform
     *
     * @return the protocol reply
     */
    public XSPReply getPlatformRoles() {
        return doRequest(
                "kernel/security/roles",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets a role on the platform
     *
     * @param roleId The identifier of the role
     * @return The protocol reply
     */
    public XSPReply getPlatformRole(String roleId) {
        return doRequest(
                "kernel/security/roles/" + URIUtils.encodeComponent(roleId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new role on the platform
     *
     * @param roleId The identifier for the new role
     * @param name   The name for the new role
     * @return The protocol reply
     */
    public XSPReply createPlatformRole(String roleId, String name) {
        return doRequest(
                "kernel/security/roles/" + URIUtils.encodeComponent(roleId) + "?name=" + URIUtils.encodeComponent(name),
                HttpConstants.METHOD_PUT);
    }

    /**
     * Deletes a role from the platform
     *
     * @param roleId The identifier of the role
     * @return The protocol reply
     */
    public XSPReply deletePlatformRole(String roleId) {
        return doRequest(
                "kernel/security/roles/" + URIUtils.encodeComponent(roleId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Renames a role on the platform
     *
     * @param roleId The identifier of the role
     * @param name   The new name for the role
     * @return The protocol reply
     */
    public XSPReply renamePlatformRole(String roleId, String name) {
        return doRequest(
                "kernel/security/roles/" + URIUtils.encodeComponent(roleId) + "/rename?name=" + URIUtils.encodeComponent(name),
                HttpConstants.METHOD_POST);
    }

    /**
     * Assigns a platform role to a user
     *
     * @param roleId The identifier of the role
     * @param userId The identifier of the target user
     * @return The protocol reply
     */
    public XSPReply assignRoleToPlatformUser(String roleId, String userId) {
        return doRequest(
                "kernel/security/users/" + URIUtils.encodeComponent(userId) + "/assign?role=" + URIUtils.encodeComponent(roleId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Un-assigns a platform role from a user
     *
     * @param roleId The identifier of the role
     * @param userId The identifier of the target user
     * @return The protocol reply
     */
    public XSPReply unassignRoleFromPlatformUser(String roleId, String userId) {
        return doRequest(
                "kernel/security/users/" + URIUtils.encodeComponent(userId) + "/unassign?role=" + URIUtils.encodeComponent(roleId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Assigns a platform role to a group
     *
     * @param roleId  The identifier of the role
     * @param groupId The identifier of the target group
     * @return The protocol reply
     */
    public XSPReply assignRoleToPlatformGroup(String roleId, String groupId) {
        return doRequest(
                "kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/assign?role=" + URIUtils.encodeComponent(roleId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Un-assigns a platform role from a group
     *
     * @param roleId  The identifier of the role
     * @param groupId The identifier of the target group
     * @return The protocol reply
     */
    public XSPReply unassignRoleFromPlatformGroup(String roleId, String groupId) {
        return doRequest(
                "kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/unassign?role=" + URIUtils.encodeComponent(roleId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Adds the fact that a role implies another role
     *
     * @param roleId        The identifier of a role
     * @param impliedRoleId The identifier of the role implied by the first one
     * @return The protocol reply
     */
    public XSPReply addPlatformRoleImplication(String roleId, String impliedRoleId) {
        return doRequest(
                "kernel/security/roles/" + URIUtils.encodeComponent(roleId) + "/imply?target=" + URIUtils.encodeComponent(impliedRoleId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Removes the fact that a role implies another role
     *
     * @param roleId        The identifier of a role
     * @param impliedRoleId The identifier of the role implied by the first one
     * @return The protocol reply
     */
    public XSPReply removePlatformRoleImplication(String roleId, String impliedRoleId) {
        return doRequest(
                "kernel/security/roles/" + URIUtils.encodeComponent(roleId) + "/unimply?target=" + URIUtils.encodeComponent(impliedRoleId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the list of the existing resources for the document of known APIs
     *
     * @return The protocol reply
     */
    public XSPReply getApiResources() {
        return doRequest("kernel/discovery/resources", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the list of the known APIs on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getApiServices() {
        return doRequest("kernel/discovery/services", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of the platform's product
     *
     * @return The protocol reply
     */
    public XSPReply getPlatformProduct() {
        return doRequest("kernel/platform/product", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a list of the OSGi bundles deployed on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getPlatformBundles() {
        return doRequest("kernel/platform/bundles", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a list of the addons currently installed on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getPlatformAddons() {
        return doRequest("kernel/platform/addons", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of an addon installed on the platform
     *
     * @param addonId The identifier of an addon
     * @return The protocol reply
     */
    public XSPReply getPlatformAddon(String addonId) {
        return doRequest(
                "kernel/platform/addons/" + URIUtils.encodeComponent(addonId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Installs an addon on the platform from the raw data of the addon
     *
     * @param addonId The identifier of the addon
     * @param stream  The data stream containing the addon
     * @return The protocol reply
     */
    public XSPReply installPlatformAddon(String addonId, byte[] stream) {
        return doRequest("kernel/platform/addons/" + URIUtils.encodeComponent(addonId),
                HttpConstants.METHOD_PUT,
                stream,
                HttpConstants.MIME_OCTET_STREAM,
                false,
                HttpConstants.MIME_JSON);
    }

    /**
     * Un-installs an addon from the platform
     *
     * @param addonId The identifier of the addon
     * @return The protocol reply
     */
    public XSPReply uninstallPlatformAddon(String addonId) {
        return doRequest(
                "kernel/platform/addons/" + URIUtils.encodeComponent(addonId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Initiates a complete shutdown of the platform
     *
     * @return The protocol reply
     */
    public XSPReply platformShutdown() {
        return doRequest("kernel/platform/shutdown", HttpConstants.METHOD_POST);
    }

    /**
     * Initiates a restart sequence of the platform
     *
     * @return The protocol reply
     */
    public XSPReply platformRestart() {
        return doRequest("kernel/platform/restart", HttpConstants.METHOD_POST);
    }

    /**
     * Gets the log messages on the platform
     *
     * @return The log messages
     */
    public XSPReply getLogMessages() {
        return doRequest("kernel/log", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the current jobs on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getJobs() {
        return doRequest("kernel/jobs", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific job on the platform
     *
     * @param jobId The identifier of the job
     * @return The protocol reply
     */
    public XSPReply getJob(String jobId) {
        return doRequest(
                "kernel/jobs/" + URIUtils.encodeComponent(jobId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Attempts to cancel a job running on the platform
     *
     * @param jobId The identifier of the job
     * @return The protocol reply
     */
    public XSPReply cancelJob(String jobId) {
        return doRequest(
                "kernel/jobs/" + URIUtils.encodeComponent(jobId) + "/cancel",
                HttpConstants.METHOD_POST);
    }


/*****************************************************
 * Kernel - Statistics Service
 ****************************************************/
/*
    public XSPReply getAllMetrics() {
        return doRequest("kernel/statistics/metrics", null, "GET", null, null);
    }

    public XSPReply getMetric(metricId) {
        return doRequest("kernel/statistics/metrics/" +  URIUtils.encodeComponent(metricId), null, "GET", null, null);
    }

    public XSPReply getMetricSnapshot(metricId) {
        return doRequest("kernel/statistics/metrics/" +  URIUtils.encodeComponent(metricId) + "/snapshot", null, "GET", null, null);
    }
*/


/*****************************************************
 * Kernel - Business Directory Service
 ****************************************************/

/*
    public XSPReply getArtifactArchetypes() {
        return doRequest("kernel/business/archetypes", null, "GET", null, null);
    }

    public XSPReply getBusinessArchetype(archetypeId) {
        return doRequest("kernel/business/archetypes/" +  URIUtils.encodeComponent(archetypeId), null, "GET", null, null);
    }

    public XSPReply getArtifactSchemas() {
        return doRequest("kernel/business/schemas", null, "GET", null, null);
    }

    public XSPReply getArtifactSchema(schemaId) {
        return doRequest("kernel/business/schemas/" +  URIUtils.encodeComponent(schemaId), null, "GET", null, null);
    }
*/


/*****************************************************
 * Webapp - Web Modules Directory Service
 ****************************************************/
/*
    public XSPReply getWebModules() {
        return doRequest("services/webapp/modules", null, "GET", null, null);
    }
*/


/*****************************************************
 * Collaboration - Collaboration Service
 ****************************************************/
/*
    public XSPReply archiveCollaboration() {
        return doRequest("services/collaboration/archive", null, "POST", null, null);
    }

    public XSPReply deleteCollaboration() {
        return doRequest("services/collaboration/delete", null, "POST", null, null);
    }

    public XSPReply getCollaborationManifest() {
        return doRequest("services/collaboration/manifest", null, "GET", null, null);
    }

    public XSPReply getCollaborationInputSpecifications() {
        return doRequest("services/collaboration/manifest/inputs", null, "GET", null, null);
    }

    public XSPReply addCollaborationInputSpecification(specification) {
        return doRequest("services/collaboration/manifest/inputs", null, "PUT", MIME_JSON, specification);
    }

    public XSPReply removeCollaborationInputSpecification(specificationId) {
        return doRequest("services/collaboration/manifest/inputs/" +  URIUtils.encodeComponent(specificationId), null, "DELETE", null, null);
    }

    public XSPReply getArtifactsForCollaborationInput(specificationId) {
        return doRequest("services/collaboration/manifest/inputs/" +  URIUtils.encodeComponent(specificationId) + "/artifacts", null, "GET", null, null);
    }

    public XSPReply registerArtifactForCollaborationInput(specificationId, artifactId) {
        return doRequest("services/collaboration/manifest/inputs/" +  URIUtils.encodeComponent(specificationId) + "/artifacts/" +  URIUtils.encodeComponent(artifactId), null, "PUT", null, null);
    }

    public XSPReply unregisterArtifactForCollaborationInput(specificationId, artifactId) {
        return doRequest("services/collaboration/manifest/inputs/" +  URIUtils.encodeComponent(specificationId) + "/artifacts/" +  URIUtils.encodeComponent(artifactId), null, "DELETE", null, null);
    }

    public XSPReply getCollaborationOutputSpecifications() {
        return doRequest("services/collaboration/manifest/outputs", null, "GET", null, null);
    }

    public XSPReply addCollaborationOutputSpecification(specification) {
        return doRequest("services/collaboration/manifest/outputs", null, "PUT", MIME_JSON, specification);
    }

    public XSPReply removeCollaborationOutputSpecification(specificationId) {
        return doRequest("services/collaboration/manifest/outputs/" +  URIUtils.encodeComponent(specificationId), null, "DELETE", null, null);
    }

    public XSPReply getArtifactsForCollaborationOutput(specificationId) {
        return doRequest("services/collaboration/manifest/outputs/" +  URIUtils.encodeComponent(specificationId) + "/artifacts", null, "GET", null, null);
    }

    public XSPReply registerArtifactForCollaborationOutput(specificationId, artifactId) {
        return doRequest("services/collaboration/manifest/outputs/" +  URIUtils.encodeComponent(specificationId) + "/artifacts/" +  URIUtils.encodeComponent(artifactId), null, "PUT", null, null);
    }

    public XSPReply unregisterArtifactForCollaborationOutput(specificationId, artifactId) {
        return doRequest("services/collaboration/manifest/outputs/" +  URIUtils.encodeComponent(specificationId) + "/artifacts/" +  URIUtils.encodeComponent(artifactId), null, "DELETE", null, null);
    }

    public XSPReply getCollaborationRoles() {
        return doRequest("services/collaboration/manifest/roles", null, "GET", null, null);
    }

    public XSPReply createCollaborationRole(role) {
        return doRequest("services/collaboration/manifest/roles", null, "PUT", MIME_JSON, role);
    }

    public XSPReply addCollaborationRole(roleId) {
        return doRequest("services/collaboration/manifest/roles/" +  URIUtils.encodeComponent(roleId), null, "PUT", null, null);
    }

    public XSPReply removeCollaborationRole(roleId) {
        return doRequest("services/collaboration/manifest/roles/" +  URIUtils.encodeComponent(roleId), null, "DELETE", null, null);
    }

    public XSPReply getCollaborationPattern() {
        return doRequest("services/collaboration/manifest/pattern", null, "GET", null, null);
    }

    public XSPReply getKnownIOSpecifications() {
        return doRequest("services/collaboration/specifications", null, "GET", null, null);
    }

    public XSPReply getKnownPatterns() {
        return doRequest("services/collaboration/patterns", null, "GET", null, null);
    }

    public XSPReply getCollaborationNeighbours() {
        return doRequest("services/collaboration/neighbours", null, "GET", null, null);
    }

    public XSPReply spawnCollaboration(specification) {
        return doRequest("services/collaboration/neighbours", null, "PUT", MIME_JSON, specification);
    }

    public XSPReply getCollaborationNeighbour(neighbourId) {
        return doRequest("services/collaboration/neighbours/" +  URIUtils.encodeComponent(neighbourId), null, "GET", null, null);
    }

    public XSPReply getCollaborationNeighbourManifest(neighbourId) {
        return doRequest("services/collaboration/neighbours/" +  URIUtils.encodeComponent(neighbourId) + "/manifest", null, "GET", null, null);
    }

    public XSPReply getCollaborationNeighbourStatus(neighbourId) {
        return doRequest("services/collaboration/neighbours/" +  URIUtils.encodeComponent(neighbourId) + "/status", null, "GET", null, null);
    }

    public XSPReply deleteCollaborationNeighbour(neighbourId) {
        return doRequest("services/collaboration/neighbours/" +  URIUtils.encodeComponent(neighbourId), null, "DELETE", null, null);
    }

    public XSPReply archiveCollaborationNeighbour(neighbourId) {
        return doRequest("services/collaboration/neighbours/" +  URIUtils.encodeComponent(neighbourId) + "/archive", null, "POST", null, null);
    }

    public XSPReply restartCollaborationNeighbour(neighbourId) {
        return doRequest("services/collaboration/neighbours/" +  URIUtils.encodeComponent(neighbourId) + "/restart", null, "POST", null, null);
    }

    public XSPReply getCollaborationNeighbourInputs(neighbourId, specificationId) {
        return doRequest("services/collaboration/neighbours/" +  URIUtils.encodeComponent(neighbourId) + "/manifest/inputs/" +  URIUtils.encodeComponent(specificationId) + "/artifacts", null, "GET", null, null);
    }

    public XSPReply getCollaborationNeighbourOutputs(neighbourId, specificationId) {
        return doRequest("services/collaboration/neighbours/" +  URIUtils.encodeComponent(neighbourId) + "/manifest/outputs/" +  URIUtils.encodeComponent(specificationId) + "/artifacts", null, "GET", null, null);
    }
*/


/*****************************************************
 * Connection - Connection Service
 ****************************************************/
/*
    public XSPReply getDescriptors() {
        return doRequest("services/connection/descriptors", null, "GET", null, null);
    }

    public XSPReply getConnectors() {
        return doRequest("services/connection/connectors", null, "GET", null, null);
    }

    public XSPReply getConnector(connectorId) {
        return doRequest("services/connection/connectors/" +  URIUtils.encodeComponent(connectorId), null, "GET", null, null);
    }

    public XSPReply createConnector(descriptor, definition) {
        return doRequest("services/connection/connectors/" +  URIUtils.encodeComponent(connectorId), {descriptor: descriptor.identifier}, "PUT", MIME_JSON, definition);
    }

    public XSPReply deleteConnector(connectorId) {
        return doRequest("services/connection/connectors/" +  URIUtils.encodeComponent(connectorId), null, "DELETE", null, null);
    }

    public XSPReply pullFromConnector(connectorId) {
        return doRequest("services/connection/connectors/" +  URIUtils.encodeComponent(connectorId) + "/pull", null, "POST", null, null);
    }

    public XSPReply pushToConnector(connectorId, artifactId) {
        return doRequest("services/connection/connectors/" +  URIUtils.encodeComponent(connectorId) + "/push", {artifact: artifactId}, "POST", null, null);
    }
*/


/*****************************************************
 * Storage - Storage Service
 ****************************************************/
/*
    public XSPReply sparql(payload) {
        return doRequest("services/storage/sparql", null, "POST", "application/sparql-query", payload);
    }

    public XSPReply getAllArtifacts() {
        return doRequest("services/storage/artifacts", null, "GET", null, null);
    }

    public XSPReply getLiveArtifacts() {
        return doRequest("services/storage/artifacts/live", null, "GET", null, null);
    }

    public XSPReply getArtifactsForBase(base) {
        return doRequest("services/storage/artifacts", {base: base}, "GET", null, null);
    }

    public XSPReply getArtifactsForArchetype(archetype) {
        return doRequest("services/storage/artifacts", {archetype: archetype}, "GET", null, null);
    }

    public XSPReply getArtifact(artifactId) {
        return doRequest("services/storage/artifacts/" +  URIUtils.encodeComponent(artifactId), null, "GET", null, null);
    }

    public XSPReply getArtifactMetadata(artifactId) {
        return doRequest("application/n-quads", content);
            } else {
                (code, type, content);
            }
        }, "services/storage/artifacts/" +  URIUtils.encodeComponent(artifactId) + "/metadata", null, "GET", null, null);
    }

    public XSPReply getArtifactContent(artifactId) {
        return doRequest("application/n-quads", content);
            } else {
                (code, type, content);
            }
        }, "services/storage/artifacts/" +  URIUtils.encodeComponent(artifactId) + "/content", null, "GET", null, null);
    }

    public XSPReply deleteArtifact(artifactId) {
        return doRequest("services/storage/artifacts/" +  URIUtils.encodeComponent(artifactId), null, "DELETE", null, null);
    }

    public XSPReply diffArtifacts(artifactLeft, artifactRight) {
        return doRequest("--xowlQuads");
                var rightIndex = content.lastIndexOf("--xowlQuads");
                var contentLeft = content.substring(leftIndex + "--xowlQuads".length, rightIndex);
                var contentRight = content.substring(rightIndex + "--xowlQuads".length);
                (code, MIME_JSON, {
                        left: contentLeft,
                        right: contentRight
			});
            } else {
                (code, type, content);
            }
        }, "services/storage/artifacts/diff", {left: artifactLeft, right: artifactRight}, "POST", null, null);
    }

    public XSPReply pullArtifactFromLive(artifactId) {
        return doRequest("services/storage/artifacts/" +  URIUtils.encodeComponent(artifactId) + "/deactivate", null, "POST", null, null);
    }

    public XSPReply pushArtifactToLive(artifactId) {
        return doRequest("services/storage/artifacts/" +  URIUtils.encodeComponent(artifactId) + "/activate", null, "POST", null, null);
    }
*/


/*****************************************************
 * Importation - Importation Service
 ****************************************************/
/*
    public XSPReply getUploadedDocuments() {
        return doRequest("services/importation/documents", null, "GET", null, null);
    }

    public XSPReply getUploadedDocument(docId) {
        return doRequest("services/importation/documents/" +  URIUtils.encodeComponent(docId), null, "GET", null, null);
    }

    public XSPReply getDocumentImporters() {
        return doRequest("services/importation/importers", null, "GET", null, null);
    }

    public XSPReply getDocumentImporter(importerId) {
        return doRequest("services/importation/importers/" +  URIUtils.encodeComponent(importerId), null, "GET", null, null);
    }

    public XSPReply getUploadedDocumentPreview(docId, importer, configuration) {
        return doRequest("services/importation/documents/" +  URIUtils.encodeComponent(docId) + "/preview", {importer: importer}, "POST", MIME_JSON, configuration);
    }

    public XSPReply dropUploadedDocument(docId) {
        return doRequest("services/importation/documents/" +  URIUtils.encodeComponent(docId), null, "DELETE", null, null);
    }

    public XSPReply importUploadedDocument(docId, importer, configuration) {
        return doRequest("services/importation/documents/" +  URIUtils.encodeComponent(docId) + "/import", {importer: importer}, "POST", MIME_JSON, configuration);
    }

    public XSPReply uploadDocument(name, content, fileName) {
        return doRequest("services/importation/documents", {name: name, fileName: fileName}, "PUT", MIME_OCTET_STREAM, content);
    }
*/


/*****************************************************
 * Consistency - Consistency Service
 ****************************************************/
/*
    public XSPReply getInconsistencies() {
        return doRequest("services/consistency/inconsistencies", null, "GET", null, null);
    }

    public XSPReply getConsistencyRules() {
        return doRequest("services/consistency/rules", null, "GET", null, null);
    }

    public XSPReply getConsistencyRule(ruleId) {
        return doRequest("services/consistency/rules/" +  URIUtils.encodeComponent(ruleId), null, "GET", null, null);
    }

    public XSPReply newConsistencyRule(name, message, prefixes, conditions) {
        return doRequest("services/consistency/rules", {
                name: name,
                message: message,
                prefixes: prefixes
	}, "PUT", "application/x-xowl-rdft", conditions);
    }

    public XSPReply activateConsistencyRule(ruleId) {
        return doRequest("services/consistency/rules/" +  URIUtils.encodeComponent(ruleId) + "/activate", null, "POST", null, null);
    }

    public XSPReply deactivateConsistencyRule(ruleId) {
        return doRequest("services/consistency/rules/" +  URIUtils.encodeComponent(ruleId) + "/deactivate", null, "POST", null, null);
    }

    public XSPReply deleteConsistencyRule(ruleId) {
        return doRequest("services/consistency/rules/" +  URIUtils.encodeComponent(ruleId), null, "DELETE", null, null);
    }
*/


/*****************************************************
 * Impact - Impact Analysis Service
 ****************************************************/
/*
    public XSPReply newImpactAnalysis(definition) {
        return doRequest("services/impact", null, "POST", MIME_JSON, definition);
    }
*/


/*****************************************************
 * Evaluation - Evaluation Service
 ****************************************************/

/*
    public XSPReply getEvaluations() {
        return doRequest("services/evaluation/evaluations", null, "GET", null, null);
    }

    public XSPReply getEvaluation(evaluationId) {
        return doRequest("services/evaluation/evaluations/" +  URIUtils.encodeComponent(evaluationId), null, "GET", null, null);
    }

    public XSPReply getEvaluableTypes() {
        return doRequest("services/evaluation/evaluableTypes", null, "GET", null, null);
    }

    public XSPReply getEvaluables(typeId) {
        return doRequest("services/evaluation/evaluables", {type: typeId}, "GET", null, null);
    }

    public XSPReply getEvaluationCriteria(typeId) {
        return doRequest("services/evaluation/criterionTypes", {'for': typeId}, "GET", null, null);
    }

    public XSPReply newEvaluation(definition) {
        return doRequest("services/evaluation/evaluations", null, "PUT", MIME_JSON, definition);
    }
    */


/*****************************************************
 * Marketplace - Marketplace service
 ****************************************************/

    /*
    public XSPReply marketplaceLookupAddons(input) {
        return doRequest("services/marketplace/addons", {input: input}, "GET", null, null);
    }

    public XSPReply marketplaceGetAddon(addonId) {
        return doRequest("services/marketplace/addons/" +  URIUtils.encodeComponent(addonId), null, "GET", null, null);
    }

    public XSPReply marketplaceInstallAddon(addonId) {
        return doRequest("services/marketplace/addons/" +  URIUtils.encodeComponent(addonId) + "/install", null, "POST", null, null);
    }
    
    
    
     */


    /*****************************************************
     * Utility API
     ****************************************************/

    /**
     * Sends an HTTP request to the endpoint, completed with an URI complement
     *
     * @param uriComplement The URI complement to append to the original endpoint URI, if any
     * @param method        The HTTP method to use, if any
     * @return The response, or null if the request failed before reaching the server
     */
    public XSPReply doRequest(String uriComplement, String method) {
        return doRequest(uriComplement, method, null, HttpConstants.MIME_TEXT_PLAIN, false, HttpConstants.MIME_JSON);
    }

    /**
     * Sends an HTTP request to the endpoint, completed with an URI complement
     *
     * @param uriComplement The URI complement to append to the original endpoint URI, if any
     * @param method        The HTTP method to use, if any
     * @param body          The request body object, if any
     * @return The response, or null if the request failed before reaching the server
     */
    public XSPReply doRequest(String uriComplement, String method, Object body) {
        byte[] byteBody = null;
        String contentType = HttpConstants.MIME_TEXT_PLAIN;
        if (body != null) {
            if (body instanceof Serializable) {
                byteBody = ((Serializable) body).serializedJSON().getBytes(Files.CHARSET);
                contentType = HttpConstants.MIME_JSON;
            } else {
                byteBody = body.toString().getBytes(Files.CHARSET);
            }
        }
        return doRequest(uriComplement, method, byteBody, contentType, false, HttpConstants.MIME_JSON);
    }

    /**
     * Sends an HTTP request to the endpoint, completed with an URI complement
     *
     * @param uriComplement The URI complement to append to the original endpoint URI, if any
     * @param method        The HTTP method to use, if any
     * @param body          The request body, if any
     * @param contentType   The request body content type, if any
     * @param compressed    Whether the body is compressed with gzip
     * @param accept        The MIME type to accept for the response, if any
     * @return The response, or null if the request failed before reaching the server
     */
    public XSPReply doRequest(String uriComplement, String method, byte[] body, String contentType, boolean compressed, String accept) {
        // not logged in
        if (currentUser == null)
            return XSPReplyNetworkError.instance();
        HttpResponse response = connection.request(uriComplement,
                method,
                body,
                contentType,
                compressed,
                accept
        );
        XSPReply reply = XSPReplyUtils.fromHttpResponse(response, deserializer);
        if (reply != XSPReplyExpiredSession.instance())
            // not an authentication problem => return this reply
            return reply;
        // try to re-login
        reply = login(currentLogin, currentPassword);
        if (!reply.isSuccess())
            // failed => unauthenticated
            return XSPReplyUnauthenticated.instance();
        // now that we are logged-in, retry
        response = connection.request(uriComplement,
                method,
                body,
                contentType,
                compressed,
                accept
        );
        return XSPReplyUtils.fromHttpResponse(response, deserializer);
    }
}
