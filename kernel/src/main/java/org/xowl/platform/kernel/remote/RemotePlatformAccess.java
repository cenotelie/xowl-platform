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

import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.Identifiable;
import fr.cenotelie.commons.utils.Serializable;
import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.commons.utils.api.ReplyException;
import fr.cenotelie.commons.utils.api.ReplyResult;
import fr.cenotelie.commons.utils.api.ReplyUtils;
import fr.cenotelie.commons.utils.http.HttpConnection;
import fr.cenotelie.commons.utils.http.HttpConstants;
import fr.cenotelie.commons.utils.http.HttpResponse;
import fr.cenotelie.commons.utils.http.URIUtils;
import fr.cenotelie.commons.utils.logging.Logging;
import org.xowl.infra.store.Repository;
import org.xowl.infra.store.sparql.Command;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobStatus;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.security.SecuredActionPolicy;
import org.xowl.platform.kernel.security.SecuredResourceSharing;

/**
 * The base API for accessing a remote platform
 *
 * @author Laurent Wouters
 */
public class RemotePlatformAccess extends HttpConnection {
    /**
     * The factory to use
     */
    protected final PlatformApiDeserializer deserializer;

    /**
     * Initializes this platform connection
     *
     * @param endpoint     The API endpoint (https://something:port/api)
     * @param deserializer The deserializer to use
     */
    public RemotePlatformAccess(String endpoint, PlatformApiDeserializer deserializer) {
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
    public Reply login(String login, String password) {
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
    public Reply logout() {
        return doRequest("/kernel/security/logout",
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the current configuration of the security policy
     *
     * @return The protocol reply
     */
    public Reply getSecurityPolicy() {
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
    public Reply setSecuredActionPolicy(String actionId, SecuredActionPolicy policy) {
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
    public Reply getPlatformUsers() {
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
    public Reply getPlatformUser(String userId) {
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
    public Reply createPlatformUser(String userId, String name, String password) {
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
    public Reply deletePlatformUser(String userId) {
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
    public Reply renamePlatformUser(String userId, String name) {
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
    public Reply changePlatformUserPassword(String userId, String oldPassword, String newPassword) {
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
    public Reply resetPlatformUserPassword(String userId, String newPassword) {
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
    public Reply getPlatformGroups() {
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
    public Reply getPlatformGroup(String groupId) {
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
    public Reply createPlatformGroup(String groupId, String name, String adminId) {
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
    public Reply deletePlatformGroup(String groupId) {
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
    public Reply renamePlatformGroup(String groupId, String name) {
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
    public Reply addMemberToPlatformGroup(String groupId, String userId) {
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
    public Reply removeMemberFromPlatformGroup(String groupId, String userId) {
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
    public Reply addAdminToPlatformGroup(String groupId, String userId) {
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
    public Reply removeAdminFromPlatformGroup(String groupId, String userId) {
        return doRequest(
                "/kernel/security/groups/" + URIUtils.encodeComponent(groupId) + "/removeAdmin?user=" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the roles on the platform
     *
     * @return the protocol reply
     */
    public Reply getPlatformRoles() {
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
    public Reply getPlatformRole(String roleId) {
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
    public Reply createPlatformRole(String roleId, String name) {
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
    public Reply deletePlatformRole(String roleId) {
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
    public Reply renamePlatformRole(String roleId, String name) {
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
    public Reply assignRoleToPlatformUser(String roleId, String userId) {
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
    public Reply unassignRoleFromPlatformUser(String roleId, String userId) {
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
    public Reply assignRoleToPlatformGroup(String roleId, String groupId) {
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
    public Reply unassignRoleFromPlatformGroup(String roleId, String groupId) {
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
    public Reply addPlatformRoleImplication(String roleId, String impliedRoleId) {
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
    public Reply removePlatformRoleImplication(String roleId, String impliedRoleId) {
        return doRequest(
                "/kernel/security/roles/" + URIUtils.encodeComponent(roleId) + "/unimply?target=" + URIUtils.encodeComponent(impliedRoleId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the security descriptor of a secured resource
     *
     * @param resourceId The identifier of a secured resource
     * @return The protocol reply
     */
    public Reply getSecuredResourceDescriptor(String resourceId) {
        return doRequest(
                "/kernel/security/resources/" + URIUtils.encodeComponent(resourceId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Adds a new owner to a secured resource
     *
     * @param resourceId The identifier of a secured resource
     * @param userId     The identifier of the user
     * @return The protocol reply
     */
    public Reply addSecuredResourceOwner(String resourceId, String userId) {
        return doRequest(
                "/kernel/security/resources/" + URIUtils.encodeComponent(resourceId) + "/addOwner?user=" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Removes an owner from a secured resource
     *
     * @param resourceId The identifier of a secured resource
     * @param userId     The identifier of the user
     * @return The protocol reply
     */
    public Reply removeSecuredResourceOwner(String resourceId, String userId) {
        return doRequest(
                "/kernel/security/resources/" + URIUtils.encodeComponent(resourceId) + "/removeOwner?user=" + URIUtils.encodeComponent(userId),
                HttpConstants.METHOD_POST);
    }

    /**
     * Adds a new sharing to a secured resource
     *
     * @param resourceId The identifier of a secured resource
     * @param sharing    The sharing to add
     * @return The protocol reply
     */
    public Reply addSecuredResourceSharing(String resourceId, SecuredResourceSharing sharing) {
        return doRequest(
                "/kernel/security/resources/" + URIUtils.encodeComponent(resourceId) + "/addSharing",
                HttpConstants.METHOD_POST,
                sharing);
    }

    /**
     * Removes a sharing from a secured resource
     *
     * @param resourceId The identifier of a secured resource
     * @param sharing    The sharing to remove
     * @return The protocol reply
     */
    public Reply removeSecuredResourceSharing(String resourceId, SecuredResourceSharing sharing) {
        return doRequest(
                "/kernel/security/resources/" + URIUtils.encodeComponent(resourceId) + "/removeSharing",
                HttpConstants.METHOD_POST,
                sharing);
    }

    /**
     * Gets the list of the existing resources for the document of known APIs
     *
     * @return The protocol reply
     */
    public Reply getApiResources() {
        return doRequest("/kernel/discovery/resources", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the list of the known APIs on the platform
     *
     * @return The protocol reply
     */
    public Reply getApiServices() {
        return doRequest("/kernel/discovery/services", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of the platform's product
     *
     * @return The protocol reply
     */
    public Reply getPlatformProduct() {
        return doRequest("/kernel/platform/product", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a list of the OSGi bundles deployed on the platform
     *
     * @return The protocol reply
     */
    public Reply getPlatformBundles() {
        return doRequest("/kernel/platform/bundles", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a list of the addons currently installed on the platform
     *
     * @return The protocol reply
     */
    public Reply getPlatformAddons() {
        return doRequest("/kernel/platform/addons", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of an addon installed on the platform
     *
     * @param addonId The identifier of an addon
     * @return The protocol reply
     */
    public Reply getPlatformAddon(String addonId) {
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
    public Reply installPlatformAddon(String addonId, byte[] stream) {
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
    public Reply uninstallPlatformAddon(String addonId) {
        return doRequest(
                "/kernel/platform/addons/" + URIUtils.encodeComponent(addonId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Initiates a complete shutdown of the platform
     *
     * @return The protocol reply
     */
    public Reply platformShutdown() {
        return doRequest("/kernel/platform/shutdown", HttpConstants.METHOD_POST);
    }

    /**
     * Initiates a restart sequence of the platform
     *
     * @return The protocol reply
     */
    public Reply platformRestart() {
        return doRequest("/kernel/platform/restart", HttpConstants.METHOD_POST);
    }

    /**
     * Gets the log messages on the platform
     *
     * @return The log messages
     */
    public Reply getLogMessages() {
        return doRequest("/kernel/log", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the current jobs on the platform
     *
     * @return The protocol reply
     */
    public Reply getJobs() {
        return doRequest("/kernel/jobs", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific job on the platform
     *
     * @param jobId The identifier of the job
     * @return The protocol reply
     */
    public Reply getJob(String jobId) {
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
    public Reply cancelJob(String jobId) {
        return doRequest(
                "/kernel/jobs/" + URIUtils.encodeComponent(jobId) + "/cancel",
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets all the metrics for the platform
     *
     * @return The protocol reply
     */
    public Reply getAllMetrics() {
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
    public Reply getMetric(String metricId) {
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
    public Reply getMetricSnapshot(String metricId) {
        return doRequest(
                "/kernel/statistics/metrics/" + URIUtils.encodeComponent(metricId) + "/snapshot",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the available artifact archetypes
     *
     * @return The protocol reply
     */
    public Reply getArtifactArchetypes() {
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
    public Reply getArtifactArchetype(String archetypeId) {
        return doRequest(
                "/kernel/business/archetypes/" + URIUtils.encodeComponent(archetypeId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the available artifact schemas
     *
     * @return The protocol reply
     */
    public Reply getArtifactSchemas() {
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
    public Reply getArtifactSchema(String schemaId) {
        return doRequest(
                "/kernel/business/schemas/" + URIUtils.encodeComponent(schemaId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets a list of the modules for the web application UI
     *
     * @return The protocol reply
     */
    public Reply getWebModules() {
        return doRequest("/services/webapp/modules", HttpConstants.METHOD_GET);
    }

    /**
     * Archives the current collaboration (stops the current platform's instance)
     *
     * @return The protocol reply
     */
    public Reply archiveCollaboration() {
        return doRequest("/services/collaboration/archive", HttpConstants.METHOD_POST);
    }

    /**
     * Deletes the current collaboration (and all the data for the platform's instance)
     *
     * @return The protocol reply
     */
    public Reply deleteCollaboration() {
        return doRequest("/services/collaboration/delete", HttpConstants.METHOD_POST);
    }

    /**
     * Gets the manifest for the collaboration implemented by the platform's instance
     *
     * @return The protocol reply
     */
    public Reply getCollaborationManifest() {
        return doRequest("/services/collaboration/manifest", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the specifications of inputs
     *
     * @return The protocol reply
     */
    public Reply getCollaborationInputSpecifications() {
        return doRequest("/services/collaboration/manifest/inputs", HttpConstants.METHOD_GET);
    }

    /**
     * Adds a specification for a new input
     *
     * @param specification The input specification
     * @return The protocol reply
     */
    public Reply addCollaborationInputSpecification(Serializable specification) {
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
    public Reply removeCollaborationInputSpecification(String specificationId) {
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
    public Reply getArtifactsForCollaborationInput(String specificationId) {
        return doRequest("/services/collaboration/manifest/inputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts", HttpConstants.METHOD_GET);
    }

    /**
     * Registers an artifact as fulfilling an input specification
     *
     * @param specificationId The identifier of the specification
     * @param artifactId      The identifier of the artifact
     * @return The protocol reply
     */
    public Reply registerArtifactForCollaborationInput(String specificationId, String artifactId) {
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
    public Reply unregisterArtifactForCollaborationInput(String specificationId, String artifactId) {
        return doRequest(
                "/services/collaboration/manifest/inputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts/" + URIUtils.encodeComponent(artifactId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the specification of outputs
     *
     * @return The protocol reply
     */
    public Reply getCollaborationOutputSpecifications() {
        return doRequest("/services/collaboration/manifest/outputs", HttpConstants.METHOD_GET);
    }

    /**
     * Adds a specification for a new output
     *
     * @param specification The output specification
     * @return The protocol reply
     */
    public Reply addCollaborationOutputSpecification(Serializable specification) {
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
    public Reply removeCollaborationOutputSpecification(String specificationId) {
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
    public Reply getArtifactsForCollaborationOutput(String specificationId) {
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
    public Reply registerArtifactForCollaborationOutput(String specificationId, String artifactId) {
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
    public Reply unregisterArtifactForCollaborationOutput(String specificationId, String artifactId) {
        return doRequest(
                "/services/collaboration/manifest/outputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts/" + URIUtils.encodeComponent(artifactId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the collaboration's role on the platform
     *
     * @return The protocol reply
     */
    public Reply getCollaborationRoles() {
        return doRequest("/services/collaboration/manifest/roles", HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new collaboration role
     *
     * @param role The role to create
     * @return The protocol reply
     */
    public Reply createCollaborationRole(PlatformRole role) {
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
    public Reply addCollaborationRole(String roleId) {
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
    public Reply removeCollaborationRole(String roleId) {
        return doRequest(
                "/services/collaboration/manifest/roles/" + URIUtils.encodeComponent(roleId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the used collaboration pattern
     *
     * @return The protocol reply
     */
    public Reply getCollaborationPattern() {
        return doRequest("/services/collaboration/manifest/pattern", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the known input and output specifications
     *
     * @return The protocol reply
     */
    public Reply getKnownIOSpecifications() {
        return doRequest("/services/collaboration/specifications", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the known collaboration patterns
     *
     * @return The protocol reply
     */
    public Reply getKnownPatterns() {
        return doRequest("/services/collaboration/patterns", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the neighbour collaborations
     *
     * @return The protocol reply
     */
    public Reply getCollaborationNeighbours() {
        return doRequest("/services/collaboration/neighbours", HttpConstants.METHOD_GET);
    }

    /**
     * Spawns a new collaboration in the neighbourhood
     *
     * @param specification The specification for the collaboration
     * @return The protocol reply
     */
    public Reply spawnCollaboration(Serializable specification) {
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
    public Reply getCollaborationNeighbour(String neighbourId) {
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
    public Reply getCollaborationNeighbourManifest(String neighbourId) {
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
    public Reply getCollaborationNeighbourStatus(String neighbourId) {
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
    public Reply deleteCollaborationNeighbour(String neighbourId) {
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
    public Reply archiveCollaborationNeighbour(String neighbourId) {
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
    public Reply restartCollaborationNeighbour(String neighbourId) {
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
    public Reply getCollaborationNeighbourInputs(String neighbourId, String specificationId) {
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
    public Reply getCollaborationNeighbourOutputs(String neighbourId, String specificationId) {
        return doRequest(
                "/services/collaboration/neighbours/" + URIUtils.encodeComponent(neighbourId) + "/manifest/outputs/" + URIUtils.encodeComponent(specificationId) + "/artifacts",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of the bots on the platform
     *
     * @return The protocol reply
     */
    public Reply getBots() {
        return doRequest("/services/community/bots", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific bot on the platform
     *
     * @param botId The identifier of the bot
     * @return The protocol reply
     */
    public Reply getBot(String botId) {
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
    public Reply getBotMessages(String botId) {
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
    public Reply wakeupBot(String botId) {
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
    public Reply putBotToSleep(String botId) {
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
    public Reply getPublicProfile(String profileId) {
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
    public Reply updatePublicProfile(Identifiable profile) {
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
    public Reply getBadges() {
        return doRequest("/services/community/badges", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific badge
     *
     * @param badgeId The identifier of a badge
     * @return The protocol reply
     */
    public Reply getBadge(String badgeId) {
        return doRequest("/services/community/badges/" + URIUtils.encodeComponent(badgeId), HttpConstants.METHOD_GET);
    }

    /**
     * Awards a badge to a user
     *
     * @param userId  The identifier of the user
     * @param badgeId The identifier of the badge
     * @return The protocol reply
     */
    public Reply awardBadge(String userId, String badgeId) {
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
    public Reply rescindBadge(String userId, String badgeId) {
        return doRequest(
                "/services/community/profiles/" + URIUtils.encodeComponent(userId) + "/public/badges/" + URIUtils.encodeComponent(badgeId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the descriptors for available connectors
     *
     * @return The protocol reply
     */
    public Reply getDescriptors() {
        return doRequest("/services/connection/descriptors", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the descriptions of the spawned connectors
     *
     * @return The protocol reply
     */
    public Reply getConnectors() {
        return doRequest("/services/connection/connectors", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific connector
     *
     * @param connectorId The identifier of a connector
     * @return The protocol reply
     */
    public Reply getConnector(String connectorId) {
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
    public Reply createConnector(String descriptorId, Identifiable specification) {
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
    public Reply deleteConnector(String connectorId) {
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
    public Reply pullFromConnector(String connectorId) {
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
    public Reply pushToConnector(String connectorId, String artifactId) {
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
    public Reply sparql(String query) {
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
    public Reply sparql(String query, String store) {
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
    public Reply getAllArtifacts() {
        return doRequest("/services/storage/artifacts", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of the artifacts currently active for reasoning
     *
     * @return The protocol reply
     */
    public Reply getLiveArtifacts() {
        return doRequest("/services/storage/artifacts/live", HttpConstants.METHOD_GET);
    }

    /**
     * Gets a description of the artifacts with the specified base
     *
     * @param base The base URI to look for
     * @return The protocol reply
     */
    public Reply getArtifactsForBase(String base) {
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
    public Reply getArtifactsForArchetype(String archetype) {
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
    public Reply getArtifact(String artifactId) {
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
    public Reply getArtifactMetadata(String artifactId) {
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
    public Reply getArtifactContent(String artifactId) {
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
    public Reply deleteArtifact(String artifactId) {
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

    public Reply diffArtifacts(String artifactLeft, String artifactRight) {
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
    public Reply pullArtifactFromLive(String artifactId) {
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
    public Reply pushArtifactToLive(String artifactId) {
        return doRequest(
                "/services/storage/artifacts/" + URIUtils.encodeComponent(artifactId) + "/activate",
                HttpConstants.METHOD_POST);
    }

    /**
     * Gets the list of the available importers for uploaded documents
     *
     * @return The protocol reply
     */
    public Reply getDocumentImporters() {
        return doRequest("/services/importation/importers", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific importer for uploaded documents
     *
     * @param importerId The identifier of an importer
     * @return The protocol reply
     */
    public Reply getDocumentImporter(String importerId) {
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
    public Reply getImporterConfigurationsFor(String importerId) {
        return doRequest(
                "/services/importation/importers/" + URIUtils.encodeComponent(importerId) + "/configurations",
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets all the stored importer configurations
     *
     * @return The protocol reply
     */
    public Reply getImporterConfigurations() {
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
    public Reply getImporterConfiguration(String configurationId) {
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
    public Reply deleteImporterConfiguration(String configurationId) {
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
    public Reply storeImporterConfiguration(Serializable configuration) {
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
    public Reply getUploadedDocuments() {
        return doRequest("/services/importation/documents", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific uploaded document
     *
     * @param documentId The identifier of the document
     * @return The protocol reply
     */
    public Reply getUploadedDocument(String documentId) {
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
    public Reply dropUploadedDocument(String documentId) {
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
    public Reply uploadDocument(String name, String fileName, byte[] content) {
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
    public Reply getUploadedDocumentPreview(String documentId, String configurationId) {
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
    public Reply getUploadedDocumentPreview(String documentId, Serializable configuration) {
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
    public Reply importUploadedDocument(String documentId, String configurationId, Artifact metadata) {
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
    public Reply importUploadedDocument(String documentId, Serializable configuration, Artifact metadata) {
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
    public Reply getInconsistencies() {
        return doRequest("/services/consistency/inconsistencies", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the current reasoning rules deployed on the live database
     *
     * @return The protocol reply
     */
    public Reply getReasoningRules() {
        return doRequest("/services/consistency/rules", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific reasoning rule
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public Reply getReasoningRule(String ruleId) {
        return doRequest(
                "/services/consistency/rules/" + URIUtils.encodeComponent(ruleId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new reasoning rule
     *
     * @param name       The name for the new rule
     * @param definition The rule's xRDF definition
     * @return The protocol reply
     */
    public Reply newReasoningRule(String name, String definition) {
        return doRequest("/services/consistency/rules" +
                        "?name=" + URIUtils.encodeComponent(name),
                HttpConstants.METHOD_PUT,
                definition.getBytes(IOUtils.CHARSET),
                Repository.SYNTAX_XRDF,
                false,
                HttpConstants.MIME_JSON);
    }

    /**
     * Activates a reasoning rule
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public Reply activateReasoningRule(String ruleId) {
        return doRequest(
                "/services/consistency/rules/" + URIUtils.encodeComponent(ruleId) + "/activate",
                HttpConstants.METHOD_POST);
    }

    /**
     * Deactivates a reasoning rule
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public Reply deactivateReasoningRule(String ruleId) {
        return doRequest(
                "/services/consistency/rules/" + URIUtils.encodeComponent(ruleId) + "/deactivate",
                HttpConstants.METHOD_POST);
    }

    /**
     * Deletes a reasoning rule
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public Reply deleteReasoningRule(String ruleId) {
        return doRequest(
                "/services/consistency/rules/" + URIUtils.encodeComponent(ruleId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Gets the current consistency constraints used to detect inconsistencies in the live data
     *
     * @return The protocol reply
     */
    public Reply getConsistencyConstraints() {
        return doRequest("/services/consistency/constraints", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description of a specific consistency constraint
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public Reply getConsistencyConstraint(String ruleId) {
        return doRequest(
                "/services/consistency/constraints/" + URIUtils.encodeComponent(ruleId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Creates a new consistency constraint
     *
     * @param name        The rule's name
     * @param message     The message produced by the rule
     * @param prefixes    The prefixes for short URIs
     * @param antecedents The constraint's antecedents for matching
     * @param guard       The constraint's guard (if any)
     * @return The protocol reply
     */
    public Reply newConsistencyConstraint(String name, String message, String prefixes, String antecedents, String guard) {
        return doRequest("/services/consistency/constraints" +
                        "?name=" + URIUtils.encodeComponent(name) +
                        "&message=" + URIUtils.encodeComponent(message) +
                        "&prefixes=" + URIUtils.encodeComponent(prefixes) +
                        "&antecedents=" + URIUtils.encodeComponent(antecedents) +
                        (guard != null && !guard.isEmpty() ? "&guard=" + URIUtils.encodeComponent(guard) : ""),
                HttpConstants.METHOD_PUT);
    }

    /**
     * Activates a consistency constraint
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public Reply activateConsistencyConstraint(String ruleId) {
        return doRequest(
                "/services/consistency/constraints/" + URIUtils.encodeComponent(ruleId) + "/activate",
                HttpConstants.METHOD_POST);
    }

    /**
     * Deactivates a consistency constraint
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public Reply deactivateConsistencyConstraint(String ruleId) {
        return doRequest(
                "/services/consistency/constraints/" + URIUtils.encodeComponent(ruleId) + "/deactivate",
                HttpConstants.METHOD_POST);
    }

    /**
     * Deletes a consistency constraint
     *
     * @param ruleId The identifier of the rule
     * @return The protocol reply
     */
    public Reply deleteConsistencyConstraint(String ruleId) {
        return doRequest(
                "/services/consistency/constraints/" + URIUtils.encodeComponent(ruleId),
                HttpConstants.METHOD_DELETE);
    }

    /**
     * Launches a new impact analysis
     *
     * @param definition The specification for the analysis
     * @return The protocol reply
     */
    public Reply newImpactAnalysis(Serializable definition) {
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
    public Reply getEvaluations() {
        return doRequest("/services/evaluation/evaluations", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the description a specification evaluation
     *
     * @param evaluationId The identifier of an evaluation
     * @return The protocol reply
     */
    public Reply getEvaluation(String evaluationId) {
        return doRequest(
                "/services/evaluation/evaluations/" + URIUtils.encodeComponent(evaluationId),
                HttpConstants.METHOD_GET);
    }

    /**
     * Gets the list of the known types of elements that can be the subject of an evaluation
     *
     * @return The protocol reply
     */
    public Reply getEvaluableTypes() {
        return doRequest("/services/evaluation/evaluableTypes", HttpConstants.METHOD_GET);
    }

    /**
     * Gets the list of evaluable elements for a certain type
     *
     * @param typeId The identifier of the evaluable type
     * @return The protocol reply
     */
    public Reply getEvaluables(String typeId) {
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
    public Reply getEvaluationCriteria(String typeId) {
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
    public Reply newEvaluation(Serializable definition) {
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
    public Reply marketplaceLookupAddons(String input) {
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
    public Reply marketplaceGetAddon(String addonId) {
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
    public Reply marketplaceInstallAddon(String addonId) {
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
    public Reply doRequest(String uriComplement, String method) {
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
    public Reply doRequest(String uriComplement, String method, Object body) {
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
    public Reply doRequest(String uriComplement, String method, byte[] body, String contentType, boolean compressed, String accept) {
        HttpResponse response = request(uriComplement,
                method,
                body,
                contentType,
                compressed,
                accept
        );
        return ReplyUtils.fromHttpResponse(response, deserializer);
    }

    /**
     * Waits for a job to finish
     *
     * @param jobId The identifier of the job
     * @return The job's result, or the error
     */
    public Reply waitForJob(String jobId) {
        while (true) {
            Reply reply = getJob(jobId);
            if (!reply.isSuccess())
                return reply;
            Job job = ((ReplyResult<Job>) reply).getData();
            if (job.getStatus() == JobStatus.Completed)
                return job.getResult();
            if (job.getStatus() == JobStatus.Cancelled)
                return job.getResult();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException exception) {
                Logging.get().error(exception);
                return new ReplyException(exception);
            }
        }
    }
}
