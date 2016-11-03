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

package org.xowl.platform.satellites.base;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.api.XOWLFactory;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.utils.TextUtils;

/**
 * Represents a job on a remote xOWL platform
 *
 * @author Laurent Wouters
 */
public class RemoteJob {
    /**
     * The job is scheduled (in the queue of an executor)
     */
    public static final String STATUS_SCHEDULED = "Scheduled";
    /**
     * The job is running
     */
    public static final String STATUS_RUNNING = "Running";
    /**
     * The job is completed
     */
    public static final String STATUS_COMPLETED = "Completed";

    /**
     * The job's identifier
     */
    protected final String identifier;
    /**
     * The job's name
     */
    protected final String name;
    /**
     * The job's type
     */
    protected final String type;
    /**
     * The time this job has been scheduled
     */
    protected String timeScheduled;
    /**
     * The time this job has started running
     */
    protected String timeRun;
    /**
     * The time this job has been completed
     */
    protected String timeCompleted;
    /**
     * The job's status
     */
    protected String status;
    /**
     * The job's result, if any
     */
    protected XSPReply result;

    /**
     * Gets the job's identifier
     *
     * @return The job's identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the job's name
     *
     * @return The job's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the job's current status
     *
     * @return The job's current status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the result for this job, or null if it not yet complete
     *
     * @return The result for this job
     */
    public XSPReply getResult() {
        return result;
    }

    /**
     * Initializes this job
     *
     * @param definition The JSON definition
     * @param factory    The factory for the result
     */
    public RemoteJob(ASTNode definition, XOWLFactory factory) {
        String id = null;
        String name = null;
        String type = null;
        this.status = "";
        this.timeScheduled = "";
        this.timeRun = "";
        this.timeCompleted = "";
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                id = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("type".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                type = value.substring(1, value.length() - 1);
            } else if ("status".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                this.status = value.substring(1, value.length() - 1);
            } else if ("timeScheduled".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                this.timeScheduled = value.substring(1, value.length() - 1);
            } else if ("timeRun".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                this.timeRun = value.substring(1, value.length() - 1);
            } else if ("timeCompleted".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                this.timeCompleted = value.substring(1, value.length() - 1);
            } else if ("result".equals(head)) {
                ASTNode value = member.getChildren().get(1);
                if (!value.getChildren().isEmpty())
                    result = XSPReplyUtils.parseJSONResult(value, factory);
            }
        }
        this.identifier = id;
        this.name = name;
        this.type = type;
    }

    /**
     * Updates the content of this job
     *
     * @param definition The JSON definition
     * @param factory    The factory for the result
     */
    void update(ASTNode definition, XOWLFactory factory) {
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("status".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                this.status = value.substring(1, value.length() - 1);
            } else if ("timeScheduled".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                this.timeScheduled = value.substring(1, value.length() - 1);
            } else if ("timeRun".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                this.timeRun = value.substring(1, value.length() - 1);
            } else if ("timeCompleted".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                this.timeCompleted = value.substring(1, value.length() - 1);
            } else if ("result".equals(head)) {
                ASTNode value = member.getChildren().get(1);
                if (!value.getChildren().isEmpty())
                    result = XSPReplyUtils.parseJSONResult(value, factory);
            }
        }
    }
}
