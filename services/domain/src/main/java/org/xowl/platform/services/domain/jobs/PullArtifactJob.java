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

package org.xowl.platform.services.domain.jobs;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.store.IOUtils;
import org.xowl.platform.kernel.JobBase;
import org.xowl.platform.services.domain.DomainUtils;

/**
 * A job for pulling artifact from a connector and storing it with the storage service
 *
 * @author Laurent Wouters
 */
public class PullArtifactJob extends JobBase {
    /**
     * The identifier of the target connector
     */
    private final String connectorId;
    /**
     * The job's result
     */
    private XSPReply result;

    /**
     * Initializes this job
     *
     * @param connectorId The target connector
     */
    public PullArtifactJob(String connectorId) {
        this(PullArtifactJob.class.getCanonicalName(), connectorId);
    }

    /**
     * Initializes this job
     *
     * @param type        The custom type of this job
     * @param connectorId The target connector
     */
    public PullArtifactJob(String type, String connectorId) {
        super("Pull artifact from " + connectorId, type);
        this.connectorId = connectorId;
    }

    /**
     * Initializes this job
     *
     * @param definition The job's definition
     */
    public PullArtifactJob(ASTNode definition) {
        super(definition);
        String connector = IOUtils.unescape(getPayloadNode(definition).getValue());
        this.connectorId = connector.substring(1, connector.length() - 1);
    }

    @Override
    protected String getJSONSerializedPayload() {
        return "\"" + connectorId + "\"";
    }

    @Override
    public XSPReply getResult() {
        return result;
    }

    @Override
    public void doRun() {
        result = DomainUtils.pullArtifactFrom(connectorId);
    }
}
