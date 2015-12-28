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

package org.xowl.platform.services.workflow;

import org.xowl.platform.kernel.Artifact;
import org.xowl.platform.kernel.ArtifactStorageService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.domain.DomainConnectorService;
import org.xowl.store.xsp.XSPReply;
import org.xowl.store.xsp.XSPReplyFailure;
import org.xowl.store.xsp.XSPReplyResult;

/**
 * Utility APIs for the workflows
 *
 * @author Laurent Wouters
 */
public class WorkflowUtils {
    /**
     * Pulls an artifact from a connector into the long-term store
     *
     * @param connector The identifier of a connector
     * @return The result of the operation
     */
    public static XSPReply pullArtifact(String connector) {
        DomainConnectorService connectorService = ServiceUtils.getService(DomainConnectorService.class, "id", connector);
        if (connectorService == null)
            return new XSPReplyFailure("Failed to resolve connector " + connector);
        ArtifactStorageService storageService = ServiceUtils.getService(ArtifactStorageService.class);
        if (storageService == null)
            return new XSPReplyFailure("Failed to resolve an artifact storage service");
        Artifact artifact = connectorService.getNextInput(false);
        if (artifact == null)
            return new XSPReplyFailure("No queued artifact in connector " + connector);
        boolean success = storageService.store(artifact);
        return success ? new XSPReplyResult<>(artifact) : new XSPReplyFailure("Failed to push the artifact to the long term store");
    }

    /**
     * Pushes an artifact from the long-term store to the live store
     *
     * @param artifactID The identifier of the artifact to push
     * @return The result of the operation
     */
    public static XSPReply pushToLive(String artifactID) {
        ArtifactStorageService storageService = ServiceUtils.getService(ArtifactStorageService.class);
        if (storageService == null)
            return new XSPReplyFailure("Failed to resolve an artifact storage service");
        Artifact artifact = storageService.retrieve(artifactID);
        if (artifact == null)
            return new XSPReplyFailure("Artifact " + artifactID + " does not exist in the long term store");
        boolean success = storageService.pushToLive(artifact);
        return success ? new XSPReplyResult<>(artifact) : new XSPReplyFailure("Failed to push the artifact to the live store");
    }
}
