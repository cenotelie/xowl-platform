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

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.JobBase;
import org.xowl.store.xsp.XSPReply;

/**
 * Implements a job for the workflow that encapsulate a workflow action
 *
 * @author Laurent Wouters
 */
public class WorkflowJob extends JobBase {
    /**
     * The underlying action
     */
    protected final WorkflowAction action;
    /**
     * The action's result
     */
    protected XSPReply result;

    /**
     * Initializes this job
     *
     * @param name   The action's name
     * @param action The workflow action
     */
    public WorkflowJob(String name, WorkflowAction action) {
        super(name, action.getType());
        this.action = action;
    }

    /**
     * Initializes this job
     *
     * @param definition The actions's definition
     * @param action     The workflow action
     */
    public WorkflowJob(ASTNode definition, WorkflowAction action) {
        super(definition);
        this.action = action;
    }

    @Override
    protected String getJSONSerializedPayload() {
        return action.serializedJSON();
    }

    @Override
    public void onComplete() {
        // do nothing
    }

    @Override
    public void doRun() {
        result = action.execute(null);
    }
}
