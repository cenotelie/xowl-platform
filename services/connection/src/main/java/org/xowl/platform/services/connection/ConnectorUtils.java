/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.connection;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.platform.kernel.Artifact;
import org.xowl.platform.kernel.ArtifactStorageService;
import org.xowl.platform.kernel.ServiceUtils;

/**
 * Utility APIs for the management of domains and related artifacts
 *
 * @author Laurent Wouters
 */
public class ConnectorUtils {
    /**
     * Pulls an artifact from a connector into the long-term store
     *
     * @param connectorId The identifier of a connector
     * @return The result of the operation
     */
    public static XSPReply pullArtifactFrom(String connectorId) {
        ConnectorService connector = ServiceUtils.getService(ConnectorService.class, "id", connectorId);
        if (connector == null)
            return new XSPReplyFailure("Failed to resolve connector " + connectorId);
        return pullArtifactFrom(connector);
    }

    /**
     * Pulls an artifact from the connector into the standard storage (usually the long-term store)
     *
     * @param connector The connector to pull from
     * @return The result of the operation
     */
    public static XSPReply pullArtifactFrom(ConnectorService connector) {
        ArtifactStorageService storageService = ServiceUtils.getService(ArtifactStorageService.class);
        if (storageService == null)
            return new XSPReplyFailure("Failed to resolve an artifact storage service");
        XSPReply replyArtifact = connector.getNextInput(false);
        if (!replyArtifact.isSuccess())
            return replyArtifact;
        Artifact artifact = ((XSPReplyResult<Artifact>) replyArtifact).getData();
        XSPReply reply = storageService.store(artifact);
        if (!reply.isSuccess())
            return reply;
        // reply with the artifact
        return replyArtifact;
    }

    /**
     * Pushes an artifact to a connector's client
     *
     * @param connectorId The identifier of the connector to push to
     * @param artifactId  The identifier of the artifact to push
     * @return The result of the operation
     */
    public static XSPReply pushArtifactTo(String connectorId, String artifactId) {
        ConnectorService connector = ServiceUtils.getService(ConnectorService.class, "id", connectorId);
        if (connector == null)
            return new XSPReplyFailure("Failed to resolve connector " + connectorId);
        ArtifactStorageService storage = ServiceUtils.getService(ArtifactStorageService.class);
        if (storage == null)
            return new XSPReplyFailure("Failed to resolve the artifact storage service");
        XSPReply reply = storage.retrieve(artifactId);
        if (!reply.isSuccess())
            return reply;
        return connector.pushToClient(((XSPReplyResult<Artifact>) reply).getData());
    }
}
