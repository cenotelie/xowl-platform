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

package org.xowl.platform.services.workflow.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.Job;
import org.xowl.platform.kernel.JobFactory;
import org.xowl.platform.services.workflow.WorkflowAction;
import org.xowl.platform.services.workflow.WorkflowFactoryService;
import org.xowl.store.IOUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements the workflow factory service for the xOWL platform
 *
 * @author Laurent Wouters
 */
public class XOWLWorkflowFactoryService implements WorkflowFactoryService, JobFactory {
    /**
     * The supported action types
     */
    public static final String[] ACTIONS = new String[]{
            "XOWLWorkflowActionPullArtifact",
            "XOWLWorkflowActionPushLive"
    };
    /**
     * The supported action types
     */
    private static final Collection<String> ACTIONS_COLLECTION = Collections.unmodifiableList(Arrays.asList(ACTIONS));

    /**
     * The associated workflow service
     */
    private final XOWLWorkflowService workflowService;

    /**
     * Initializes this service
     *
     * @param workflowService The associated workflow service
     */
    public XOWLWorkflowFactoryService(XOWLWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public String getIdentifier() {
        return XOWLWorkflowFactoryService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Workflow Factory Service";
    }

    @Override
    public String getProperty(String name) {
        if (name == null)
            return null;
        if ("identifier".equals(name))
            return getIdentifier();
        if ("name".equals(name))
            return getName();
        return null;
    }

    @Override
    public Collection<String> getActionTypes() {
        return ACTIONS_COLLECTION;
    }

    @Override
    public WorkflowAction newAction(String type, ASTNode jsonDefinition) {
        switch (type) {
            case "XOWLWorkflowActionPullArtifact":
                return new XOWLWorkflowActionPullArtifact(jsonDefinition);
            case "XOWLWorkflowActionPushLive":
                return new XOWLWorkflowActionPushLive(jsonDefinition);
        }
        return null;
    }

    @Override
    public Job newJob(String type, ASTNode definition) {
        ASTNode nodePayload = null;
        for (ASTNode member : definition.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("payload".equals(head)) {
                nodePayload = member.getChildren().get(1);
                break;
            }
        }
        if (nodePayload == null)
            return null;
        switch (type) {
            case "XOWLWorkflowActionPullArtifact":
                return workflowService.newJob(definition, new XOWLWorkflowActionPullArtifact(nodePayload));
            case "XOWLWorkflowActionPushLive":
                return workflowService.newJob(definition, new XOWLWorkflowActionPushLive(nodePayload));
        }
        return null;
    }
}
