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

package org.xowl.platform.services.connection.jobs;

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.jobs.JobBase;
import org.xowl.platform.services.connection.ConnectorUtils;

/**
 * A job for pushing artifact to a connector's client
 *
 * @author Laurent Wouters
 */
public class PushArtifactJob extends JobBase {
    /**
     * The identifier of the target connector
     */
    private final String connectorId;
    /**
     * The identifier of the artifact to push
     */
    private final String artifactId;
    /**
     * The job's result
     */
    private Reply result;

    /**
     * Initializes this job
     *
     * @param connectorId The target connector
     * @param artifactId  The identifier of the artifact to push
     */
    public PushArtifactJob(String connectorId, String artifactId) {
        this(PullArtifactJob.class.getCanonicalName(), connectorId, artifactId);
    }

    /**
     * Initializes this job
     *
     * @param type        The custom type of this job
     * @param connectorId The target connector
     * @param artifactId  The identifier of the artifact to push
     */
    public PushArtifactJob(String type, String connectorId, String artifactId) {
        super("Push artifact " + artifactId + " to " + connectorId, type);
        this.connectorId = connectorId;
        this.artifactId = artifactId;
    }

    /**
     * Initializes this job
     *
     * @param definition The job's definition
     */
    public PushArtifactJob(ASTNode definition) {
        super(definition);
        ASTNode payloadNode = getPayloadNode(definition);
        String connector = null;
        String artifact = null;
        for (ASTNode member : payloadNode.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            String value = TextUtils.unescape(member.getChildren().get(1).getValue());
            head = head.substring(1, head.length() - 1);
            if ("connectorId".equals(head)) {
                connector = value.substring(1, value.length() - 1);
            } else if ("artifactId".equals(head)) {
                artifact = value.substring(1, value.length() - 1);
            }
        }
        this.connectorId = connector;
        this.artifactId = artifact;
    }

    @Override
    protected String getJSONSerializedPayload() {
        return "{\"connectorId\": \"" +
                TextUtils.escapeStringJSON(connectorId) +
                "\", \"artifactId\": \"" +
                TextUtils.escapeStringJSON(artifactId) +
                "\"}";
    }

    @Override
    public Reply getResult() {
        return result;
    }

    @Override
    public void run() {
        result = ConnectorUtils.pushArtifactTo(connectorId, artifactId);
    }
}