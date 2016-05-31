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

package org.xowl.platform.kernel.jobs;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.utils.concurrent.SafeRunnable;
import org.xowl.infra.utils.logging.Logging;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Base implementation of a job on the platform
 *
 * @author Laurent Wouters
 */
public abstract class JobBase extends SafeRunnable implements Job {
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
    protected JobStatus status;

    /**
     * Initializes this job
     *
     * @param name The job's name
     * @param type The job's type
     */
    public JobBase(String name, String type) {
        super(Logging.getDefault());
        this.identifier = Job.class.getCanonicalName() + "." + UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.status = JobStatus.Unscheduled;
        this.timeScheduled = "";
        this.timeRun = "";
        this.timeCompleted = "";
    }

    /**
     * Initializes this job
     *
     * @param definition The JSON definition
     */
    public JobBase(ASTNode definition) {
        super(Logging.getDefault());
        String id = null;
        String name = null;
        String type = null;
        this.status = JobStatus.Scheduled;
        this.timeScheduled = "";
        this.timeRun = "";
        this.timeCompleted = "";
        for (ASTNode member : definition.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                id = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("type".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                type = value.substring(1, value.length() - 1);
            } else if ("status".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                this.status = JobStatus.valueOf(value.substring(1, value.length() - 1));
            } else if ("timeScheduled".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                this.timeScheduled = value.substring(1, value.length() - 1);
            } else if ("timeRun".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                this.timeRun = value.substring(1, value.length() - 1);
            } else if ("timeCompleted".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                this.timeCompleted = value.substring(1, value.length() - 1);
            }
        }
        this.identifier = id;
        this.name = name;
        this.type = type;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"identifier\": \""
                + IOUtils.escapeStringJSON(identifier)
                + "\", \"name\":\""
                + IOUtils.escapeStringJSON(name)
                + "\", \"type\": \""
                + IOUtils.escapeStringJSON(type)
                + "\", \"status\": \""
                + IOUtils.escapeStringJSON(status.toString())
                + "\", \"timeScheduled\": \""
                + IOUtils.escapeStringJSON(timeScheduled)
                + "\", \"timeRun\": \""
                + IOUtils.escapeStringJSON(timeRun)
                + "\", \"timeCompleted\": \""
                + IOUtils.escapeStringJSON(timeCompleted)
                + "\", \"payload\": "
                + getJSONSerializedPayload()
                + ", \"result\": "
                + (getResult() == null ? "{}" : getResult().serializedJSON())
                + "}";
    }

    @Override
    public JobStatus getStatus() {
        return status;
    }

    @Override
    public void onScheduled() {
        status = JobStatus.Scheduled;
        timeScheduled = DateFormat.getDateTimeInstance().format(new Date());
    }

    @Override
    public void onRun() {
        status = JobStatus.Running;
        timeRun = DateFormat.getDateTimeInstance().format(new Date());
    }

    @Override
    public void onCompleted() {
        status = JobStatus.Completed;
        timeCompleted = DateFormat.getDateTimeInstance().format(new Date());
    }

    /**
     * Gets the JSON serialization of the job's payload
     *
     * @return The serialization
     */
    protected abstract String getJSONSerializedPayload();

    /**
     * Gets the AST node for the payload in the specified definition
     *
     * @param definition The definition of a job
     * @return The AST node for the payload
     */
    public static ASTNode getPayloadNode(ASTNode definition) {
        for (ASTNode member : definition.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("payload".equals(head)) {
                return member.getChildren().get(1);
            }
        }
        return null;
    }
}
