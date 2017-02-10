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

package org.xowl.platform.kernel.artifacts;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.utils.ApiError;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.webapi.HttpApiService;

/**
 * Represents a service that stores artifacts
 * Artifacts are expected to be primarily stored in a long-term storage facility.
 * Some of them may be pushed (by duplication) to a live store that enable reasoning facilities.
 * An artifact is not removed from the long-term storage facility when it is pushed to a live store, a copy of it is pushed.
 * Similarly, when a live artifact is pulled from the live store, the copy in the long-term storage facility is not affected.
 *
 * @author Laurent Wouters
 */
public interface ArtifactStorageService extends SecuredService {
    /**
     * Service action to store an artifact
     */
    SecuredAction ACTION_STORE = new SecuredAction(ArtifactStorageService.class.getCanonicalName() + ".StoreArtifact", "Artifact Storage Service - Store Artifact");
    /**
     * Service action to retrieve the metadata of an artifact
     */
    SecuredAction ACTION_RETRIEVE_METADATA = new SecuredAction(ArtifactStorageService.class.getCanonicalName() + ".RetrieveMetadata", "Artifact Storage Service - Retrieve Artifact Metadata");
    /**
     * Service action to retrieve the content of an artifact
     */
    SecuredAction ACTION_RETRIEVE_CONTENT = new SecuredAction(ArtifactStorageService.class.getCanonicalName() + ".RetrieveContent", "Artifact Storage Service - Retrieve Artifact Content");
    /**
     * Service action to delete an artifact
     */
    SecuredAction ACTION_DELETE = new SecuredAction(ArtifactStorageService.class.getCanonicalName() + ".DeleteArtifact", "Artifact Storage Service - Delete Artifact");
    /**
     * Service action to push an artifact for live reasoning
     */
    SecuredAction ACTION_PUSH_LIVE = new SecuredAction(ArtifactStorageService.class.getCanonicalName() + ".PushLive", "Artifact Storage Service - Push Artifact Live");
    /**
     * Service action to pull an artifact from live reasoning
     */
    SecuredAction ACTION_PULL_LIVE = new SecuredAction(ArtifactStorageService.class.getCanonicalName() + ".PullLive", "Artifact Storage Service - Pull Artifact from Live");

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_STORE,
            ACTION_RETRIEVE_METADATA,
            ACTION_RETRIEVE_CONTENT,
            ACTION_DELETE,
            ACTION_PUSH_LIVE,
            ACTION_PULL_LIVE
    };

    /**
     * API error - The requested operation failed in storage
     */
    ApiError ERROR_STORAGE_FAILED = new ApiError(0x00020001,
            "The requested operation failed in storage.",
            HttpApiService.ERROR_HELP_PREFIX + "0x00020001.html");
    /**
     * API error - The artifact is invalid
     */
    ApiError ERROR_INVALID_ARTIFACT = new ApiError(0x00020002,
            "The artifact is invalid.",
            HttpApiService.ERROR_HELP_PREFIX + "0x00020002.html");

    /**
     * Stores an artifact in a long-term storage facility
     *
     * @param artifact The artifact to store
     * @return The operation's result
     */
    XSPReply store(Artifact artifact);

    /**
     * Retrieves the artifact identified by the specified identifier
     *
     * @param identifier The identifier of a artifact
     * @return The operation's result which can be casted to XSPReplyResult in case of success
     */
    XSPReply retrieve(String identifier);

    /**
     * Retrieves the specific version of an artifact
     *
     * @param base    The identifier of the base artifact
     * @param version The version to retrieve
     * @return The operation's result which can be casted to XSPReplyResult in case of success
     */
    XSPReply retrieve(String base, String version);

    /**
     * Completely delete an artifact from the long term and live stores
     *
     * @param identifier The identifier of the artifact to delete
     * @return The operation's result
     */
    XSPReply delete(String identifier);

    /**
     * Completely delete an artifact from the long term and live stores
     *
     * @param artifact The artifact to delete
     * @return The operation's result
     */
    XSPReply delete(Artifact artifact);

    /**
     * Lists all the stored artifacts
     *
     * @return The operation's result which can be casted to XSPReplyResultCollection in case of success
     */
    XSPReply getAllArtifacts();

    /**
     * Lists all the stored versions of a base artifact
     *
     * @param base The identifier of the base artifact
     * @return The operation's result which can be casted to XSPReplyResultCollection in case of success
     */
    XSPReply getArtifactsForBase(String base);

    /**
     * Lists all the store artifacts of the specified archetype
     *
     * @param archetype The archetype to look for
     * @return The operation's result which can be casted to XSPReplyResultCollection in case of success
     */
    XSPReply getArtifactsForArchetype(String archetype);

    /**
     * Lists all the live artifacts
     *
     * @return The operation's result which can be casted to XSPReplyResultCollection in case of success
     */
    XSPReply getLiveArtifacts();

    /**
     * Pushes an artifact to a live reasoning store
     *
     * @param artifact The artifact to push
     * @return Whether the operation succeeded
     */
    XSPReply pushToLive(Artifact artifact);

    /**
     * Pulls an artifact from a live reasoning store
     * This effectively removes the artifact from the live reasoning store.
     *
     * @param artifact The artifact to pull
     * @return The operation's result
     */
    XSPReply pullFromLive(Artifact artifact);
}
