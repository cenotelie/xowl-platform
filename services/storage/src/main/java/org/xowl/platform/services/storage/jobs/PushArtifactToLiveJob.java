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

package org.xowl.platform.services.storage.jobs;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.api.ReplyResult;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.jobs.JobBase;

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
    private Reply result;

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
        String connector = TextUtils.unescape(getPayloadNode(definition).getValue());
        this.artifactId = connector.substring(1, connector.length() - 1);
    }

    @Override
    protected String getJSONSerializedPayload() {
        return "\"" + artifactId + "\"";
    }

    @Override
    public Reply getResult() {
        return result;
    }

    @Override
    public void doRun() {
        ArtifactStorageService storage = Register.getComponent(ArtifactStorageService.class);
        if (storage == null) {
            result = ReplyServiceUnavailable.instance();
            return;
        }
        Reply reply = storage.retrieve(artifactId);
        if (!reply.isSuccess()) {
            result = reply;
            return;
        }
        result = storage.pushToLive(((ReplyResult<Artifact>) reply).getData());
    }
}
