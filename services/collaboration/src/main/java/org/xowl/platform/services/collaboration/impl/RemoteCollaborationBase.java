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

package org.xowl.platform.services.collaboration.impl;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.services.collaboration.CollaborationManifest;
import org.xowl.platform.services.collaboration.CollaborationNetworkService;
import org.xowl.platform.services.collaboration.CollaborationStatus;
import org.xowl.platform.services.collaboration.RemoteCollaboration;

/**
 * Implements a remote collaboration access through the network
 *
 * @author Laurent Wouters
 */
public class RemoteCollaborationBase implements RemoteCollaboration {
    /**
     * The time-to-live of a the manifest cache in nano-seconds
     */
    private static final long MANIFEST_TTL = 60000000000L;

    /**
     * The identifier of the remote collaboration
     */
    private final String identifier;
    /**
     * The name of the remote collaboration
     */
    private final String name;
    /**
     * The API endpoint for the remote collaboration
     */
    private final String endpoint;
    /**
     * The parent network service
     */
    private final CollaborationNetworkService networkService;
    /**
     * The cache for the manifest of the remote collaboration
     */
    private CollaborationManifest manifest;
    /**
     * The timestamp when the manifest was last retrieve
     */
    private long manifestTimestamp;

    /**
     * Initializes this remote collaboration
     *
     * @param identifier     The identifier of the remote collaboration
     * @param name           The name of the remote collaboration
     * @param endpoint       The API endpoint for the remove collaboration
     * @param networkService The parent network service
     */
    public RemoteCollaborationBase(String identifier, String name, String endpoint, CollaborationNetworkService networkService) {
        this.identifier = identifier;
        this.name = name;
        this.endpoint = endpoint;
        this.networkService = networkService;
    }

    /**
     * Gets or refresh the cached manifest
     *
     * @return The protocol reply
     */
    private XSPReply refreshManifestCache() {
        if (manifest != null && System.nanoTime() < manifestTimestamp + MANIFEST_TTL)
            return XSPReplySuccess.instance();
        XSPReply reply = networkService.getNeighbourManifest(identifier);
        if (!reply.isSuccess())
            return reply;
        manifest = ((XSPReplyResult<CollaborationManifest>) reply).getData();
        manifestTimestamp = System.nanoTime();
        return XSPReplySuccess.instance();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getApiEndpoint() {
        return endpoint;
    }

    @Override
    public CollaborationStatus getStatus() {
        return networkService.getNeighbourStatus(identifier);
    }

    @Override
    public XSPReply getManifest() {
        XSPReply reply = refreshManifestCache();
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(manifest);
    }

    @Override
    public XSPReply getArtifactsForInput(String specificationId) {
        return networkService.getNeighbourInputsFor(identifier, specificationId);
    }

    @Override
    public XSPReply getArtifactsForOutput(String specificationId) {
        return networkService.getNeighbourOutputsFor(identifier, specificationId);
    }

    @Override
    public XSPReply archive() {
        return networkService.archive(identifier);
    }

    @Override
    public XSPReply restart() {
        return networkService.restart(identifier);
    }

    @Override
    public XSPReply delete() {
        return networkService.delete(identifier);
    }

    @Override
    public XSPReply retrieveOutput(String specificationId, String artifactId) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" + TextUtils.escapeStringJSON(RemoteCollaboration.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"status\": \"" +
                TextUtils.escapeStringJSON(getStatus().toString()) +
                "\"}";
    }
}
