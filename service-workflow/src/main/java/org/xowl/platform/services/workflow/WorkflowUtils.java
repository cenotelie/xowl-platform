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
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.domain.DomainConnectorService;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;

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
    public static WorkflowActionReply pullArtifact(String connector) {
        DomainConnectorService connectorService = ServiceUtils.getService(DomainConnectorService.class, "id", connector);
        if (connectorService == null)
            return new WorkflowActionReplyFailure("Failed to resolve connector " + connector);
        Artifact artifact = connectorService.getNextInput(false);
        if (artifact == null)
            return new WorkflowActionReplyFailure("No queued artifact in connector " + connector);
        TripleStoreService ltsService = ServiceUtils.getService(TripleStoreService.class);
        if (ltsService == null)
            return new WorkflowActionReplyFailure("Failed to resolve a LTS service");
        TripleStore longTermStore = ltsService.getLongTermStore();
        if (longTermStore == null)
            return new WorkflowActionReplyFailure("Failed to retrieve the long term store on LTS service " + ltsService.getIdentifier());
        boolean success = longTermStore.store(artifact);
        return success ? WorkflowActionReplySuccess.INSTANCE : new WorkflowActionReplyFailure("Failed to push the artifact to the long term store");
    }

    /**
     * Pushes an artifact from the long-term store to the live store
     *
     * @param artifactID The identifier of the artifact to push
     * @return The result of the operation
     */
    public static WorkflowActionReply pushToLive(String artifactID) {
        TripleStoreService ltsService = ServiceUtils.getService(TripleStoreService.class);
        if (ltsService == null)
            return new WorkflowActionReplyFailure("Failed to resolve a LTS service");
        TripleStore longTermStore = ltsService.getLongTermStore();
        if (longTermStore == null)
            return new WorkflowActionReplyFailure("Failed to retrieve the long term store on LTS service " + ltsService.getIdentifier());
        TripleStore liveStore = ltsService.getLiveStore();
        if (liveStore == null)
            return new WorkflowActionReplyFailure("Failed to retrieve the live store on LTS service " + ltsService.getIdentifier());
        Artifact artifact = longTermStore.retrieve(artifactID);
        if (artifact == null)
            return new WorkflowActionReplyFailure("Artifact " + artifactID + " does not exist in the long term store");
        boolean success = liveStore.store(artifact);
        return success ? WorkflowActionReplySuccess.INSTANCE : new WorkflowActionReplyFailure("Failed to push the artifact to the live store");
    }
}
