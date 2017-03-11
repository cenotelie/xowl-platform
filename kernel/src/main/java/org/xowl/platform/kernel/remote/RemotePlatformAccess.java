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

package org.xowl.platform.kernel.remote;

import org.xowl.infra.server.api.XOWLRule;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyException;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.Repository;
import org.xowl.infra.store.sparql.Command;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.http.HttpConnection;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobStatus;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.security.SecuredActionPolicy;

/**
 * The base API for accessing a remote platform
 *
 * @author Laurent Wouters
 */
public class RemotePlatformAccess extends HttpConnection {
    /**
     * The deserializer to use
     */
    protected final Deserializer deserializer;

    /**
     * Initializes this platform connection
     *
     * @param endpoint     The API endpoint (https://something:port/api)
     * @param deserializer The deserializer to use
     */
    public RemotePlatformAccess(String endpoint, Deserializer deserializer) {
        super(endpoint);
        this.deserializer = deserializer;
    }

    /**
     * Login a user
     *
     * @param login    The user to log in
     * @param password The user password
     * @return The protocol reply, or null if the client is banned
     */
    public XSPReply login(String login, String password) {
        return doRequest(
                "/kernel/security/login?login=" + URIUtils.encodeComponent(login),
                HttpConstants.METHOD_POST,
                password);
    }

    /**
     * Logout the current user
     *
     * @return The protocol reply
     */
    public XSPReply logout() {
        return doRequest("/kernel/security/logout",
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the current configuration of the security policy
     *
     * @return The protocol reply
     */
    public XSPReply getSecurityPolicy() {
        return doRequest(
                "/kernel/security/policy",
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
                "/kernel/security/policy/actions/" + URIUtils.encodeComponent(actionId),
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
                "/kernel/security/users",
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
                "/kernel/security/users/" + URIUtils.encodeComponent(userId),
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
                "/kernel/security/users/" + URIUtils.encodeComponent(userId) + "?name=" + URIUtils.encodeComponent(name),
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
                "/kernel/security/users/" + URIUtils.encodeComponent(userId),
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
                "/kernel/security/users/" + URIUtils.encodeComponent(userId) + "/rename" + "?name=" + URIUtils.encodeComponent(name),
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
                "/kernel/security/users/" + URIUtils.encodeComponent(userId) + "/updateKey" + "?oldKey=" + URIUtils.encodeComponent(oldPassword),
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
                "/kernel/security/users/" + URIUtils.encodeComponent(userId) + "/resetKey",
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
                "/kernel/security/groups",
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
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId),
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
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "?name=" + URIUtils.encodeComponent(name) + "&admin=" + URIUtils.encodeComponent(adminId),
                HttpConstants.METHOD_PUT);
    }

    /**
     * Deletes a group from the platform
     *
     * @param groupId The identifier of the group to delete
     * @return The protocol reply
     */
    public XSPReply deletePlatformGroup(String groupId) {
        return doRequest("/kernel/security/groups/" + URIUtils.encodeComponent(groupId),
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
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/rename?name=" + URIUtils.encodeComponent(name),
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
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/addMember?user=" + URIUtils.encodeComponent(userId),
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
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/removeMember?user=" + URIUtils.encodeComponent(userId),
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
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/addAdmin?user=" + URIUtils.encodeComponent(userId),
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
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/removeAdmin?user=" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the roles on the platform
     *
     * @return the protocol reply
     */
    public XSPReply getPlatformRoles() {
        return doRequest(
                "/kernel/security/roles",
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
                "/kernel/security/roles/" + URIUtils.encodeComponent(roleId),
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
                "/kernel/security/roles/" + URIUtils.encodeComponent(roleId) + "?name=" + URIUtils.encodeComponent(name),
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
                "/kernel/security/roles/" + URIUtils.encodeComponent(roleId),
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
                "/kernel/security/roles/" + URIUtils.encodeComponent(roleId) + "/rename?name=" + URIUtils.encodeComponent(name),
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
                "/kernel/security/users/" + URIUtils.encodeComponent(userId) + "/assign?role=" + URIUtils.encodeComponent(roleId),
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
                "/kernel/security/users/" + URIUtils.encodeComponent(userId) + "/unassign?role=" + URIUtils.encodeComponent(roleId),
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
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/assign?role=" + URIUtils.encodeComponent(roleId),
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
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/unassign?role=" + URIUtils.encodeComponent(roleId),
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
                "/kernel/security/roles/" + URIUtils.encodeComponent(roleId) + "/imply?target=" + URIUtils.encodeComponent(impliedRoleId),
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
                "/kernel/security/roles/" + URIUtils.encodeComponent(roleId) + "/unimply?target=" + URIUtils.encodeComponent(impliedRoleId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the list of the existing resources for the document of known APIs
     *
     * @return The protocol reply
     */
    public XSPReply getApiResources() {
        return doRequest("/kernel/discovery/resources", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the list of the known APIs on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getApiServices() {
        return doRequest("/kernel/discovery/services", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of the platform's product
     *
     * @return The protocol reply
     */
    public XSPReply getPlatformProduct() {
        return doRequest("/kernel/platform/product", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a list of the OSGi bundles deployed on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getPlatformBundles() {
        return doRequest("/kernel/platform/bundles", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a list of the addons currently installed on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getPlatformAddons() {
        return doRequest("/kernel/platform/addons", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of an addon installed on the platform
     *
     * @param addonId The identifier of an addon
     * @return The protocol reply
     */
    public XSPReply getPlatformAddon(String addonId) {
        return doRequest(
                "/kernel/platform/addons/" + URIUtils.encodeComponent(addonId),
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
        return doRequest("/kernel/platform/addons/" + URIUtils.encodeComponent(addonId),
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
                "/kernel/platform/addons/" + URIUtils.encodeComponent(addonId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Initiates a complete shutdown of the platform
     *
     * @return The protocol reply
     */
    public XSPReply platformShutdown() {
        return doRequest("/kernel/platform/shutdown", HttpConstants.METHOD_POST);
    }

    /**
     * Initiates a restart sequence of the platform
     *
     * @return The protocol reply
     */
    public XSPReply platformRestart() {
        return doRequest("/kernel/platform/restart", HttpConstants.METHOD_POST);
    }

    /**
     * Gets the log messages on the platform
     *
     * @return The log messages
     */
    public XSPReply getLogMessages() {
        return doRequest("/kernel/log", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the current jobs on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getJobs() {
        return doRequest("/kernel/jobs", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific job on the platform
     *
     * @param jobId The identifier of the job
     * @return The protocol reply
     */
    public XSPReply getJob(String jobId) {
        return doRequest(
                "/kernel/jobs/" + URIUtils.encodeComponent(jobId),
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
                "/kernel/jobs/" + URIUtils.encodeComponent(jobId) + "/cancel",
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets all the metrics for the platform
     *
     * @return The protocol reply
     */
    public XSPReply getAllMetrics() {
        return doRequest(
                "/kernel/statistics/metrics",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific metric for the platform
     *
     * @param metricId The identifier of the metric
     * @return The protocol reply
     */
    public XSPReply getMetric(String metricId) {
        return doRequest(
                "/kernel/statistics/metrics/" + URIUtils.encodeComponent(metricId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets a snapshot of a metric for the platform
     *
     * @param metricId The identifier of the metric
     * @return The protocol reply
     */
    public XSPReply getMetricSnapshot(String metricId) {
        return doRequest(
                "/kernel/statistics/metrics/" + URIUtils.encodeComponent(metricId) + "/snapshot",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the available artifact archetypes
     *
     * @return The protocol reply
     */
    public XSPReply getArtifactArchetypes() {
        return doRequest(
                "/kernel/business/archetypes",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific artifact archetype
     *
     * @param archetypeId The identifier of the archetype
     * @return The protocol reply
     */
    public XSPReply getArtifactArchetype(String archetypeId) {
        return doRequest(
                "/kernel/business/archetypes/" + URIUtils.encodeComponent(archetypeId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the available artifact schemas
     *
     * @return The protocol reply
     */
    public XSPReply getArtifactSchemas() {
        return doRequest(
                "/kernel/business/schemas",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific artifact schema
     *
     * @param schemaId The identifier of the schema
     * @return The protocol reply
     */
    public XSPReply getArtifactSchema(String schemaId) {
        return doRequest(
                "/kernel/business/schemas/" + URIUtils.encodeComponent(schemaId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets a list of the modules for the web application UI
     *
     * @return The protocol reply
     */
    public XSPReply getWebModules() {
        return doRequest("/services/webapp/modules", HttpConstants.METHOD_GET);
    }

    /**
     * Archives the current collaboration (stops the current platform's instance)
     *
     * @return The protocol reply
     */
    public XSPReply archiveCollaboration() {
        return doRequest("/services/collaboration/archive", HttpConstants.METHOD_POST);
    }

    /**
     * Deletes the current collaboration (and all the data for the platform's instance)
     *
     * @return The protocol reply
     */
    public XSPReply deleteCollaboration() {
        return doRequest("/services/collaboration/delete", HttpConstants.METHOD_POST);
    }

    /**
     * Gets the manifest for the collaboration implemented by the platform's instance
     *
     * @return The protocol reply
     */
    public XSPReply getCollaborationManifest() {
        return doRequest("/services/collaboration/manifest", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the specifications of inputs
     *
     * @return The protocol reply
     */
    public XSPReply getCollaborationInputSpecifications() {
        return doRequest("/services/collaboration/manifest/inputs", HttpConstants.METHOD_GET);
    }

    /**
     * Adds a specification for a new input
     *
     * @param specification The input specification
     * @return The protocol reply
     */
    public XSPReply addCollaborationInputSpecification(Serializable specification) {
        return doRequest(
                "/services/collaboration/manifest/inputs",
                HttpConstants.METHOD_PUT,
                specification);
    }

    /**
     * Removes a specification for an input
     *
     * @param specificationId The identifier of the specification
     * @return The protocol reply
     */
    public XSPReply removeCollaborationInputSpecification(String specificationId) {
        return doRequest(
                "/services/collaboration/manifest/inputs/" + URIUtils.encodeComponent(specificationId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the artifacts registered as fulfilling an input specification
     *
     * @param specificationId The identifier of the specification
     * @return The protocol reply
     */
    public XSPReply getArtifactsForCollaborationInput(String specificationId) {
        return doRequest("/services/collaboration/manifest/inputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts", HttpConstants.METHOD_GET);
    }

    /**
     * Registers an artifact as fulfilling an input specification
     *
     * @param specificationId The identifier of the specification
     * @param artifactId      The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply registerArtifactForCollaborationInput(String specificationId, String artifactId) {
        return doRequest(
                "/services/collaboration/manifest/inputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts/" + URIUtils.encodeComponent(artifactId),
                HttpConstants.METHOD_PUT);
    }

    /**
     * Un-registers an artifact as fulfilling an input specification
     *
     * @param specificationId The identifier of the specification
     * @param artifactId      The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply unregisterArtifactForCollaborationInput(String specificationId, String artifactId) {
        return doRequest(
                "/services/collaboration/manifest/inputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts/" + URIUtils.encodeComponent(artifactId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the specification of outputs
     *
     * @return The protocol reply
     */
    public XSPReply getCollaborationOutputSpecifications() {
        return doRequest("/services/collaboration/manifest/outputs", HttpConstants.METHOD_GET);
    }

    /**
     * Adds a specification for a new output
     *
     * @param specification The output specification
     * @return The protocol reply
     */
    public XSPReply addCollaborationOutputSpecification(Serializable specification) {
        return doRequest(
                "/services/collaboration/manifest/outputs",
                HttpConstants.METHOD_PUT,
                specification);
    }

    /**
     * Removes a specification for an output
     *
     * @param specificationId The identifier of the specification
     * @return The protocol reply
     */
    public XSPReply removeCollaborationOutputSpecification(String specificationId) {
        return doRequest(
                "/services/collaboration/manifest/outputs/" + URIUtils.encodeComponent(specificationId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the artifacts registered as fulfilling an output specification
     *
     * @param specificationId The identifier of the specification
     * @return The protocol reply
     */
    public XSPReply getArtifactsForCollaborationOutput(String specificationId) {
        return doRequest(
                "/services/collaboration/manifest/outputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts",
                HttpConstants.METHOD_GET);
    }

    /**
     * Registers an artifact as fulfilling an output specification
     *
     * @param specificationId The identifier of the specification
     * @param artifactId      The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply registerArtifactForCollaborationOutput(String specificationId, String artifactId) {
        return doRequest(
                "/services/collaboration/manifest/outputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts/" + URIUtils.encodeComponent(artifactId),
                HttpConstants.METHOD_PUT);
    }

    /**
     * Un-registers an artifact as fulfilling an output specification
     *
     * @param specificationId The identifier of the specification
     * @param artifactId      The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply unregisterArtifactForCollaborationOutput(String specificationId, String artifactId) {
        return doRequest(
                "/services/collaboration/manifest/outputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts/" + URIUtils.encodeComponent(artifactId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the collaboration's role on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getCollaborationRoles() {
        return doRequest("/services/collaboration/manifest/roles", HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new collaboration role
     *
     * @param role The role to create
     * @return The protocol reply
     */
    public XSPReply createCollaborationRole(PlatformRole role) {
        return doRequest(
                "/services/collaboration/manifest/roles",
                HttpConstants.METHOD_PUT,
                role);
    }

    /**
     * Adds an existing role as a collaboration role
     *
     * @param roleId The identifier of the role
     * @return The protocol reply
     */
    public XSPReply addCollaborationRole(String roleId) {
        return doRequest(
                "/services/collaboration/manifest/roles/" + URIUtils.encodeComponent(roleId),
                HttpConstants.METHOD_PUT);
    }

    /**
     * Removes a role as a collaboration role
     *
     * @param roleId The identifier of the role
     * @return The protocol reply
     */
    public XSPReply removeCollaborationRole(String roleId) {
        return doRequest(
                "/services/collaboration/manifest/roles/" + URIUtils.encodeComponent(roleId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the used collaboration pattern
     *
     * @return The protocol reply
     */
    public XSPReply getCollaborationPattern() {
        return doRequest("/services/collaboration/manifest/pattern", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the known input and output specifications
     *
     * @return The protocol reply
     */
    public XSPReply getKnownIOSpecifications() {
        return doRequest("/services/collaboration/specifications", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the known collaboration patterns
     *
     * @return The protocol reply
     */
    public XSPReply getKnownPatterns() {
        return doRequest("/services/collaboration/patterns", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the neighbour collaborations
     *
     * @return The protocol reply
     */
    public XSPReply getCollaborationNeighbours() {
        return doRequest("/services/collaboration/neighbours", HttpConstants.METHOD_GET);
    }

    /**
     * Spawns a new collaboration in the neighbourhood
     *
     * @param specification The specification for the collaboration
     * @return The protocol reply
     */
    public XSPReply spawnCollaboration(Serializable specification) {
        return doRequest(
                "/services/collaboration/neighbours",
                HttpConstants.METHOD_PUT,
                specification);
    }

    /**
     * Gets the description of a collaboration in the neighbourhood
     *
     * @param neighbourId The identifier of the collaboration
     * @return The protocol reply
     */
    public XSPReply getCollaborationNeighbour(String neighbourId) {
        return doRequest(
                "/services/collaboration/neighbours/" + URIUtils.encodeComponent(neighbourId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the manifest of a collaboration in the neighbourhood
     *
     * @param neighbourId The identifier of the collaboration
     * @return The protocol reply
     */
    public XSPReply getCollaborationNeighbourManifest(String neighbourId) {
        return doRequest(
                "/services/collaboration/neighbours/" + URIUtils.encodeComponent(neighbourId) + "/manifest",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the status of a collaboration in the neighbourhood
     *
     * @param neighbourId The identifier of the collaboration
     * @return The protocol reply
     */
    public XSPReply getCollaborationNeighbourStatus(String neighbourId) {
        return doRequest(
                "/services/collaboration/neighbours/" + URIUtils.encodeComponent(neighbourId) + "/status",
                HttpConstants.METHOD_GET);
    }

    /**
     * Stops and deletes all data related to a neighbour collaboration
     *
     * @param neighbourId The identifier of the collaboration
     * @return The protocol reply
     */
    public XSPReply deleteCollaborationNeighbour(String neighbourId) {
        return doRequest(
                "/services/collaboration/neighbours/" + URIUtils.encodeComponent(neighbourId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Archives a neighbour collaboration
     *
     * @param neighbourId The identifier of the collaboration
     * @return The protocol reply
     */
    public XSPReply archiveCollaborationNeighbour(String neighbourId) {
        return doRequest(
                "/services/collaboration/neighbours/" + URIUtils.encodeComponent(neighbourId) + "/archive",
                HttpConstants.METHOD_POST);
    }

    /**
     * Restarts an archived neighbour collaboration
     *
     * @param neighbourId The identifier of the collaboration
     * @return The protocol reply
     */
    public XSPReply restartCollaborationNeighbour(String neighbourId) {
        return doRequest(
                "/services/collaboration/neighbours/" + URIUtils.encodeComponent(neighbourId) + "/restart",
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the input artifacts for a neighbour collaboration
     *
     * @param neighbourId     The identifier of the collaboration
     * @param specificationId The identifier of the requested input specification
     * @return The protocol reply
     */
    public XSPReply getCollaborationNeighbourInputs(String neighbourId, String specificationId) {
        return doRequest(
                "/services/collaboration/neighbours/" + URIUtils.encodeComponent(neighbourId) + "/manifest/inputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the output artifacts for an neighbour collaboration
     *
     * @param neighbourId     The identifier of the collaboration
     * @param specificationId The identifier of the requested output specification
     * @return The protocol reply
     */
    public XSPReply getCollaborationNeighbourOutputs(String neighbourId, String specificationId) {
        return doRequest(
                "/services/collaboration/neighbours/" + URIUtils.encodeComponent(neighbourId) + "/manifest/outputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the bots on the platform
     *
     * @return The protocol reply
     */
    public XSPReply getBots() {
        return doRequest("/services/community/bots", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific bot on the platform
     *
     * @param botId The identifier of the bot
     * @return The protocol reply
     */
    public XSPReply getBot(String botId) {
        return doRequest(
                "/services/community/bots/" + URIUtils.encodeComponent(botId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the messages of a bot
     *
     * @param botId The identifier of the bot
     * @return The protocol reply
     */
    public XSPReply getBotMessages(String botId) {
        return doRequest(
                "/services/community/bots/" + URIUtils.encodeComponent(botId) + "/messages",
                HttpConstants.METHOD_GET);
    }

    /**
     * Attempts to wake a bot up on the platform
     *
     * @param botId The identifier of the bot
     * @return The protocol reply
     */
    public XSPReply wakeupBot(String botId) {
        return doRequest(
                "/services/community/bots/" + URIUtils.encodeComponent(botId) + "/wakeup",
                HttpConstants.METHOD_POST);
    }

    /**
     * Attempts to put a bot to sleep on the platform
     *
     * @param botId The identifier of the bot
     * @return The protocol reply
     */
    public XSPReply putBotToSleep(String botId) {
        return doRequest(
                "/services/community/bots/" + URIUtils.encodeComponent(botId) + "/putToSleep",
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the public profile for user corresponding to the specified identifier
     *
     * @param profileId The identifier of the the profile
     * @return The protocol reply
     */
    public XSPReply getPublicProfile(String profileId) {
        return doRequest(
                "/services/community/profiles/" + URIUtils.encodeComponent(profileId) + "/public",
                HttpConstants.METHOD_GET);
    }

    /**
     * Updates the public profile
     *
     * @param profile The profile to update
     * @return The protocol reply
     */
    public XSPReply updatePublicProfile(Identifiable profile) {
        return doRequest(
                "/services/community/profiles/" + URIUtils.encodeComponent(profile.getIdentifier()) + "/public",
                HttpConstants.METHOD_PUT,
                profile);
    }

    /**
     * Gets the description of all the badges
     *
     * @return The protocol reply
     */
    public XSPReply getBadges() {
        return doRequest("/services/community/badges", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific badge
     *
     * @param badgeId The identifier of a badge
     * @return The protocol reply
     */
    public XSPReply getBadge(String badgeId) {
        return doRequest("/services/community/badges/" + URIUtils.encodeComponent(badgeId), HttpConstants.METHOD_GET);
    }

    /**
     * Awards a badge to a user
     *
     * @param userId  The identifier of the user
     * @param badgeId The identifier of the badge
     * @return The protocol reply
     */
    public XSPReply awardBadge(String userId, String badgeId) {
        return doRequest(
                "/services/community/profiles/" + URIUtils.encodeComponent(userId) + "/public/badges/" + URIUtils.encodeComponent(badgeId),
                HttpConstants.METHOD_PUT);
    }

    /**
     * Rescinds a badge from a user
     *
     * @param userId  The identifier of the user
     * @param badgeId The identifier of the badge
     * @return The protocol reply
     */
    public XSPReply rescindBadge(String userId, String badgeId) {
        return doRequest(
                "/services/community/profiles/" + URIUtils.encodeComponent(userId) + "/public/badges/" + URIUtils.encodeComponent(badgeId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the descriptors for available connectors
     *
     * @return The protocol reply
     */
    public XSPReply getDescriptors() {
        return doRequest("/services/connection/descriptors", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the descriptions of the spawned connectors
     *
     * @return The protocol reply
     */
    public XSPReply getConnectors() {
        return doRequest("/services/connection/connectors", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific connector
     *
     * @param connectorId The identifier of a connector
     * @return The protocol reply
     */
    public XSPReply getConnector(String connectorId) {
        return doRequest(
                "/services/connection/connectors/" + URIUtils.encodeComponent(connectorId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new connector
     *
     * @param descriptorId  The identifier of the descriptor for the requested connector
     * @param specification The specification data for the connector
     * @return The protocol reply
     */
    public XSPReply createConnector(String descriptorId, Identifiable specification) {
        return doRequest(
                "/services/connection/connectors/" + URIUtils.encodeComponent(specification.getIdentifier()) + "?descriptor=" + URIUtils.encodeComponent(descriptorId),
                HttpConstants.METHOD_PUT,
                specification);
    }

    /**
     * Deletes a spawned connector
     *
     * @param connectorId The identifier of the connector
     * @return The protocol reply
     */
    public XSPReply deleteConnector(String connectorId) {
        return doRequest(
                "/services/connection/connectors/" + URIUtils.encodeComponent(connectorId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Pulls the next available artifact from a connector
     *
     * @param connectorId The identifier of the connector
     * @return The protocol reply
     */
    public XSPReply pullFromConnector(String connectorId) {
        return doRequest(
                "/services/connection/connectors/" + URIUtils.encodeComponent(connectorId) + "/pull",
                HttpConstants.METHOD_POST);
    }

    /**
     * Pushes an artifact through a connector to its client
     *
     * @param connectorId The identifier of a connector
     * @param artifactId  The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply pushToConnector(String connectorId, String artifactId) {
        return doRequest(
                "/services/connection/connectors/" + URIUtils.encodeComponent(connectorId) + "/push?artifact=" + URIUtils.encodeComponent(artifactId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Executes a SPARQL query against the RDF live storage
     *
     * @param query The SPARQL query
     * @return The protocol reply
     */
    public XSPReply sparql(String query) {
        return doRequest("/services/storage/sparql",
                HttpConstants.METHOD_POST,
                query.getBytes(IOUtils.CHARSET),
                Command.MIME_SPARQL_QUERY,
                false,
                HttpConstants.MIME_JSON);
    }

    /**
     * Executes a SPARQL query against a RDF store
     *
     * @param query The SPARQL query
     * @param store The identifier of the RDF store
     * @return The protocol reply
     */
    public XSPReply sparql(String query, String store) {
        return doRequest("/services/storage/sparql?store=" + URIUtils.encodeComponent(store),
                HttpConstants.METHOD_POST,
                query.getBytes(IOUtils.CHARSET),
                Command.MIME_SPARQL_QUERY,
                false,
                HttpConstants.MIME_JSON);
    }

    /**
     * Gets a description of all the stored artifacts
     *
     * @return The protocol reply
     */
    public XSPReply getAllArtifacts() {
        return doRequest("/services/storage/artifacts", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of the artifacts currently active for reasoning
     *
     * @return The protocol reply
     */
    public XSPReply getLiveArtifacts() {
        return doRequest("/services/storage/artifacts/live", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of the artifacts with the specified base
     *
     * @param base The base URI to look for
     * @return The protocol reply
     */
    public XSPReply getArtifactsForBase(String base) {
        return doRequest(
                "/services/storage/artifacts?=base" + URIUtils.encodeComponent(base),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of the artifacts of the specified archetype
     *
     * @param archetype The identifier of an archetype
     * @return The protocol reply
     */
    public XSPReply getArtifactsForArchetype(String archetype) {
        return doRequest(
                "/services/storage/artifacts?=archetype" + URIUtils.encodeComponent(archetype),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of a specific artifact
     *
     * @param artifactId The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply getArtifact(String artifactId) {
        return doRequest(
                "/services/storage/artifacts/" + URIUtils.encodeComponent(artifactId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the metadata quads for a specific artifact
     *
     * @param artifactId The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply getArtifactMetadata(String artifactId) {
        return doRequest(
                "/services/storage/artifacts/" + URIUtils.encodeComponent(artifactId) + "/metadata",
                HttpConstants.METHOD_GET,
                null,
                HttpConstants.MIME_TEXT_PLAIN,
                false,
                Repository.SYNTAX_NQUADS);
    }

    /**
     * Gets the quads contained by a specific artifact
     *
     * @param artifactId The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply getArtifactContent(String artifactId) {
        return doRequest(
                "/services/storage/artifacts/" + URIUtils.encodeComponent(artifactId) + "/content",
                HttpConstants.METHOD_GET,
                null,
                HttpConstants.MIME_TEXT_PLAIN,
                false,
                Repository.SYNTAX_NQUADS);
    }

    /**
     * Deletes an artifact
     *
     * @param artifactId The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply deleteArtifact(String artifactId) {
        return doRequest(
                "/services/storage/artifacts/" + URIUtils.encodeComponent(artifactId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Computes the diff between two artifacts
     *
     * @param artifactLeft  The identifier of the artifact on the left
     * @param artifactRight The identifier of the artifact on the right
     * @return The protocol reply
     */

    public XSPReply diffArtifacts(String artifactLeft, String artifactRight) {
        return doRequest(
                "/services/storage/artifacts/diff?left=" + URIUtils.encodeComponent(artifactLeft) + "&right=" + URIUtils.encodeComponent(artifactRight),
                HttpConstants.METHOD_POST);
    }

    /**
     * Activates an artifact for live reasoning
     *
     * @param artifactId The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply pullArtifactFromLive(String artifactId) {
        return doRequest(
                "/services/storage/artifacts/" + URIUtils.encodeComponent(artifactId) + "/deactivate",
                HttpConstants.METHOD_POST);
    }

    /**
     * De-activates an artifact for live reasoning
     *
     * @param artifactId The identifier of the artifact
     * @return The protocol reply
     */
    public XSPReply pushArtifactToLive(String artifactId) {
        return doRequest(
                "/services/storage/artifacts/" + URIUtils.encodeComponent(artifactId) + "/activate",
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the list of the available importers for uploaded documents
     *
     * @return The protocol reply
     */
    public XSPReply getDocumentImporters() {
        return doRequest("/services/importation/importers", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific importer for uploaded documents
     *
     * @param importerId The identifier of an importer
     * @return The protocol reply
     */
    public XSPReply getDocumentImporter(String importerId) {
        return doRequest(
                "/services/importation/importers/" + URIUtils.encodeComponent(importerId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the stored configurations for a specific importer
     *
     * @param importerId The identifier of an importer
     * @return The protocol reply
     */
    public XSPReply getImporterConfigurationsFor(String importerId) {
        return doRequest(
                "/services/importation/importers/" + URIUtils.encodeComponent(importerId) + "/configurations",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets all the stored importer configurations
     *
     * @return The protocol reply
     */
    public XSPReply getImporterConfigurations() {
        return doRequest(
                "/services/importation/configurations",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets a stored importer configuration
     *
     * @param configurationId The identifier of a configuration
     * @return The protocol reply
     */
    public XSPReply getImporterConfiguration(String configurationId) {
        return doRequest(
                "/services/importation/configurations/" + URIUtils.encodeComponent(configurationId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Deletes a stored importer configuration
     *
     * @param configurationId The identifier of a configuration
     * @return The protocol reply
     */
    public XSPReply deleteImporterConfiguration(String configurationId) {
        return doRequest(
                "/services/importation/configurations/" + URIUtils.encodeComponent(configurationId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Stores a configuration for an importer
     *
     * @param configuration The configuration to store
     * @return The protocol reply
     */
    public XSPReply storeImporterConfiguration(Serializable configuration) {
        return doRequest(
                "/services/importation/configurations",
                HttpConstants.METHOD_PUT,
                configuration);
    }

    /**
     * Gets a description of the uploaded documents
     *
     * @return The protocol reply
     */
    public XSPReply getUploadedDocuments() {
        return doRequest("/services/importation/documents", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific uploaded document
     *
     * @param documentId The identifier of the document
     * @return The protocol reply
     */
    public XSPReply getUploadedDocument(String documentId) {
        return doRequest
                ("/services/importation/documents/" + URIUtils.encodeComponent(documentId),
                        HttpConstants.METHOD_GET);
    }

    /**
     * Drops (delete) a previously uploaded document
     *
     * @param documentId The identifier of the document
     * @return The protocol reply
     */
    public XSPReply dropUploadedDocument(String documentId) {
        return doRequest(
                "/services/importation/documents/" + URIUtils.encodeComponent(documentId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Uploads a document
     *
     * @param name     The name for the document
     * @param fileName The original file name
     * @param content  The file's content
     * @return The protocol reply
     */
    public XSPReply uploadDocument(String name, String fileName, byte[] content) {
        return doRequest(
                "/services/importation/documents?name=" + URIUtils.encodeComponent(name) + "&fileName=" + URIUtils.encodeComponent(fileName),
                HttpConstants.METHOD_PUT,
                content,
                HttpConstants.MIME_OCTET_STREAM,
                false,
                HttpConstants.MIME_JSON);
    }

    /**
     * Gets a preview of the result of importing a document
     *
     * @param documentId      The identifier of the document to import
     * @param configurationId The identifier of the stored configuration to use
     * @return The protocol reply
     */
    public XSPReply getUploadedDocumentPreview(String documentId, String configurationId) {
        return doRequest(
                "/services/importation/documents/" + URIUtils.encodeComponent(documentId) + "/preview?configuration=" + URIUtils.encodeComponent(configurationId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets a preview of the result of importing a document
     *
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importer
     * @return The protocol reply
     */
    public XSPReply getUploadedDocumentPreview(String documentId, Serializable configuration) {
        return doRequest(
                "/services/importation/documents/" + URIUtils.encodeComponent(documentId) + "/preview",
                HttpConstants.METHOD_POST,
                configuration);
    }

    /**
     * Performs the import of a document
     *
     * @param documentId      The identifier of the document to import
     * @param configurationId The identifier of the stored configuration to use
     * @param metadata        The metadata for the artifact to produce
     * @return The protocol reply
     */
    public XSPReply importUploadedDocument(String documentId, String configurationId, Artifact metadata) {
        return doRequest(
                "/services/importation/documents/" + URIUtils.encodeComponent(documentId) + "/import" +
                        "?configuration=" + URIUtils.encodeComponent(configurationId) +
                        "&name=" + URIUtils.encodeComponent(metadata.getName()) +
                        "&base=" + URIUtils.encodeComponent(metadata.getBase()) +
                        "&version=" + URIUtils.encodeComponent(metadata.getVersion()) +
                        "&archetype=" + URIUtils.encodeComponent(metadata.getArchetype()) +
                        (metadata.getSuperseded() != null ? "&superseded=" + URIUtils.encodeComponent(metadata.getSuperseded()) : "")
                ,
                HttpConstants.METHOD_POST);
    }

    /**
     * Performs the import of a document
     *
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importer
     * @param metadata      The metadata for the artifact to produce
     * @return The protocol reply
     */
    public XSPReply importUploadedDocument(String documentId, Serializable configuration, Artifact metadata) {
        return doRequest(
                "/services/importation/documents/" + URIUtils.encodeComponent(documentId) + "/import" +
                        "?name=" + URIUtils.encodeComponent(metadata.getName()) +
                        "&base=" + URIUtils.encodeComponent(metadata.getBase()) +
                        "&version=" + URIUtils.encodeComponent(metadata.getVersion()) +
                        "&archetype=" + URIUtils.encodeComponent(metadata.getArchetype()) +
                        (metadata.getSuperseded() != null ? "&superseded=" + URIUtils.encodeComponent(metadata.getSuperseded()) : "")
                ,
                HttpConstants.METHOD_POST, configuration);
    }

    /**
     * Gets the current inconsistencies in the live data
     *
     * @return The protocol reply
     */
    public XSPReply getInconsistencies() {
        return doRequest("/services/consistency/inconsistencies", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the current consistency rules used to detect inconsistencies in the live data
     *
     * @return The protocol reply
     */
    public XSPReply getConsistencyRules() {
        return doRequest("/services/consistency/rules", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific consistency rule
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public XSPReply getConsistencyRule(String ruleId) {
        return doRequest(
                "/services/consistency/rules/" + URIUtils.encodeComponent(ruleId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new consistency rule
     *
     * @param name       The rule's name
     * @param message    The message produced by the rule
     * @param prefixes   The prefixes for the rule definition
     * @param conditions The conditions for matching the rule
     * @return The protocol reply
     */
    public XSPReply newConsistencyRule(String name, String message, String prefixes, String conditions) {
        return doRequest("/services/consistency/rules" +
                        "?name=" + URIUtils.encodeComponent(name) +
                        "&message=" + URIUtils.encodeComponent(message) +
                        "&prefixes=" + URIUtils.encodeComponent(prefixes),
                HttpConstants.METHOD_PUT,
                conditions.getBytes(IOUtils.CHARSET),
                Repository.SYNTAX_RDFT,
                false,
                HttpConstants.MIME_JSON);
    }

    /**
     * Adds a new consistency rule
     *
     * @param rule The consistency rule to add
     * @return The protocol reply
     */
    public XSPReply addConsistencyRule(XOWLRule rule) {
        return doRequest("/services/consistency/rules/" + URIUtils.encodeComponent(rule.getName()),
                HttpConstants.METHOD_PUT,
                rule);
    }

    /**
     * Activates a consistency rule
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public XSPReply activateConsistencyRule(String ruleId) {
        return doRequest(
                "/services/consistency/rules/" + URIUtils.encodeComponent(ruleId) + "/activate",
                HttpConstants.METHOD_POST);
    }

    /**
     * Deactivates a consistency rule
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public XSPReply deactivateConsistencyRule(String ruleId) {
        return doRequest(
                "/services/consistency/rules/" + URIUtils.encodeComponent(ruleId) + "/deactivate",
                HttpConstants.METHOD_POST);
    }

    /**
     * Deletes a consistency rule
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public XSPReply deleteConsistencyRule(String ruleId) {
        return doRequest(
                "/services/consistency/rules/" + URIUtils.encodeComponent(ruleId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Launches a new impact analysis
     *
     * @param definition The specification for the analysis
     * @return The protocol reply
     */
    public XSPReply newImpactAnalysis(Serializable definition) {
        return doRequest(
                "/services/impact",
                HttpConstants.METHOD_POST,
                definition);
    }

    /**
     * Gets the description of the current evaluations
     *
     * @return The protocol reply
     */
    public XSPReply getEvaluations() {
        return doRequest("/services/evaluation/evaluations", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description a specification evaluation
     *
     * @param evaluationId The identifier of an evaluation
     * @return The protocol reply
     */
    public XSPReply getEvaluation(String evaluationId) {
        return doRequest(
                "/services/evaluation/evaluations/" + URIUtils.encodeComponent(evaluationId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the list of the known types of elements that can be the subject of an evaluation
     *
     * @return The protocol reply
     */
    public XSPReply getEvaluableTypes() {
        return doRequest("/services/evaluation/evaluableTypes", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the list of evaluable elements for a certain type
     *
     * @param typeId The identifier of the evaluable type
     * @return The protocol reply
     */
    public XSPReply getEvaluables(String typeId) {
        return doRequest(
                "/services/evaluation/evaluables?type=" + URIUtils.encodeComponent(typeId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the possible criteria for a type of evaluable element
     *
     * @param typeId The identifier of the evaluable type
     * @return The protocol reply
     */
    public XSPReply getEvaluationCriteria(String typeId) {
        return doRequest(
                "/services/evaluation/criterionTypes?for=typeId" + URIUtils.encodeComponent(typeId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new evaluation from the specified definition
     *
     * @param definition the definition of an evaluation
     * @return The protocol reply
     */
    public XSPReply newEvaluation(Serializable definition) {
        return doRequest(
                "/services/evaluation/evaluations",
                HttpConstants.METHOD_PUT,
                definition);
    }

    /**
     * Lookups available addons in the current marketplace that match the specified input
     *
     * @param input The input string to look for
     * @return The protocol reply
     */
    public XSPReply marketplaceLookupAddons(String input) {
        return doRequest(
                "/services/marketplace/addons?input=" + URIUtils.encodeComponent(input),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific addon
     *
     * @param addonId The identifier of the addon in the marketplace
     * @return The protocol reply
     */
    public XSPReply marketplaceGetAddon(String addonId) {
        return doRequest(
                "/services/marketplace/addons/" + URIUtils.encodeComponent(addonId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Requests the installation of an addon from the marketplace
     *
     * @param addonId The identifier of the addon in the marketplace
     * @return The protocol reply
     */
    public XSPReply marketplaceInstallAddon(String addonId) {
        return doRequest(
                "/services/marketplace/addons/" + URIUtils.encodeComponent(addonId) + "/install",
                HttpConstants.METHOD_POST);
    }

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
                byteBody = ((Serializable) body).serializedJSON().getBytes(IOUtils.CHARSET);
                contentType = HttpConstants.MIME_JSON;
            } else {
                byteBody = body.toString().getBytes(IOUtils.CHARSET);
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
        HttpResponse response = request(uriComplement,
                method,
                body,
                contentType,
                compressed,
                accept
        );
        return XSPReplyUtils.fromHttpResponse(response, deserializer);
    }

    /**
     * Waits for a job to finish
     *
     * @param jobId The identifier of the job
     * @return The job's result, or the error
     */
    public XSPReply waitForJob(String jobId) {
        while (true) {
            XSPReply reply = getJob(jobId);
            if (!reply.isSuccess())
                return reply;
            Job job = ((XSPReplyResult<Job>) reply).getData();
            if (job.getStatus() == JobStatus.Completed)
                return job.getResult();
            if (job.getStatus() == JobStatus.Cancelled)
                return job.getResult();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException exception) {
                Logging.get().error(exception);
                return new XSPReplyException(exception);
            }
        }
    }
}
