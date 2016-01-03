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

package org.xowl.platform.services.lts.jobs;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.Artifact;
import org.xowl.platform.kernel.ArtifactStorageService;
import org.xowl.platform.kernel.JobBase;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.store.IOUtils;
import org.xowl.store.xsp.XSPReply;
import org.xowl.store.xsp.XSPReplyFailure;
import org.xowl.store.xsp.XSPReplyResult;

/**
 * A job for pushing an artifact from the long-term store to the live store
 *
 * @author Laurent Wouters
 */
public class PushArtifactToLiveJob extends JobBase {
    /**
     * The identifier of the target artifact
     */
    private final String artifactId;
    /**
     * The job's result
     */
    private XSPReply result;

    /**
     * Initializes this job
     *
     * @param artifactId The target connector
     */
    public PushArtifactToLiveJob(String artifactId) {
        this(PushArtifactToLiveJob.class.getCanonicalName(), artifactId);
    }

    /**
     * Initializes this job
     *
     * @param type       The custom type of this job
     * @param artifactId The target connector
     */
    public PushArtifactToLiveJob(String type, String artifactId) {
        super("Push live artifact " + artifactId, type);
        this.artifactId = artifactId;
    }

    /**
     * Initializes this job
     *
     * @param definition The job's definition
     */
    public PushArtifactToLiveJob(ASTNode definition) {
        super(definition);
        String connector = IOUtils.unescape(getPayloadNode(definition).getValue());
        this.artifactId = connector.substring(1, connector.length() - 1);
    }

    @Override
    protected String getJSONSerializedPayload() {
        return "\"" + artifactId + "\"";
    }

    @Override
    public XSPReply getResult() {
        return result;
    }

    @Override
    public void doRun() {
        ArtifactStorageService storage = ServiceUtils.getService(ArtifactStorageService.class);
        if (storage == null) {
            result = new XSPReplyFailure("Failed to resolve an artifact storage service");
            return;
        }
        result = storage.retrieve(artifactId);
        if (!result.isSuccess())
            return;
        result = storage.pushToLive(((XSPReplyResult<Artifact>) result).getData());
    }
}
