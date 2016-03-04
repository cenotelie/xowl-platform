/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.store.IOUtils;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.jobs.JobBase;

/**
 * A job for deleting an artifact
 *
 * @author Laurent Wouters
 */
public class DeleteArtifactJob extends JobBase {
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
    public DeleteArtifactJob(String artifactId) {
        this(PullArtifactFromLiveJob.class.getCanonicalName(), artifactId);
    }

    /**
     * Initializes this job
     *
     * @param type       The custom type of this job
     * @param artifactId The target connector
     */
    public DeleteArtifactJob(String type, String artifactId) {
        super("Delete artifact " + artifactId, type);
        this.artifactId = artifactId;
    }

    /**
     * Initializes this job
     *
     * @param definition The job's definition
     */
    public DeleteArtifactJob(ASTNode definition) {
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
            result = XSPReplyServiceUnavailable.instance();
            return;
        }
        result = storage.delete(artifactId);
    }
}
