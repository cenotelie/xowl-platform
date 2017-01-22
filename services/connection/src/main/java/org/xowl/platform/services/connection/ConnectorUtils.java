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

package org.xowl.platform.services.connection;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.services.connection.events.ArtifactPulledFromConnectorEvent;
import org.xowl.platform.services.connection.events.ArtifactPushedToConnectorEvent;

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
        ConnectorService connector = Register.getComponent(ConnectorService.class, "id", connectorId);
        if (connector == null)
            return XSPReplyNotFound.instance();
        return pullArtifactFrom(connector);
    }

    /**
     * Pulls an artifact from the connector into the standard storage (usually the long-term store)
     *
     * @param connector The connector to pull from
     * @return The result of the operation
     */
    public static XSPReply pullArtifactFrom(ConnectorService connector) {
        ArtifactStorageService storageService = Register.getComponent(ArtifactStorageService.class);
        if (storageService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply replyArtifact = connector.getNextInput(false);
        if (!replyArtifact.isSuccess())
            return replyArtifact;
        Artifact artifact = ((XSPReplyResult<Artifact>) replyArtifact).getData();
        XSPReply reply = storageService.store(artifact);
        if (!reply.isSuccess())
            return reply;
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ArtifactPulledFromConnectorEvent(connector, artifact));
        // reply with the artifact
        return new XSPReplyResult<>(artifact.getIdentifier());
    }

    /**
     * Pushes an artifact to a connector's client
     *
     * @param connectorId The identifier of the connector to push to
     * @param artifactId  The identifier of the artifact to push
     * @return The result of the operation
     */
    public static XSPReply pushArtifactTo(String connectorId, String artifactId) {
        ConnectorService connector = Register.getComponent(ConnectorService.class, "id", connectorId);
        if (connector == null)
            return XSPReplyNotFound.instance();
        ArtifactStorageService storage = Register.getComponent(ArtifactStorageService.class);
        if (storage == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = storage.retrieve(artifactId);
        if (!reply.isSuccess())
            return reply;
        Artifact artifact = ((XSPReplyResult<Artifact>) reply).getData();
        reply = connector.pushToClient(artifact);
        if (!reply.isSuccess())
            return reply;
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ArtifactPushedToConnectorEvent(connector, artifact));
        return reply;
    }
}
