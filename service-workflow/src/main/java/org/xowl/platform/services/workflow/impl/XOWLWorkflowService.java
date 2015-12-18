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

import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.config.ConfigurationService;
import org.xowl.platform.services.workflow.*;
import org.xowl.platform.utils.Utils;
import org.xowl.hime.redist.ASTNode;
import org.xowl.utils.Files;
import org.xowl.utils.config.Configuration;
import org.xowl.utils.logging.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Implements the default workflow service for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLWorkflowService implements WorkflowService {
    /**
     * The workflow
     */
    private Workflow workflow;
    /**
     * The current phase
     */
    private WorkflowPhase currentPhase;
    /**
     * The current activity
     */
    private WorkflowActivity currentActivity;

    /**
     * Retrieve the workflow from the configuration
     */
    private void retrieveWorkflow() {
        if (workflow != null)
            return;
        ConfigurationService service = ServiceUtils.getService(ConfigurationService.class);
        if (service == null)
            return;
        Configuration configuration = service.getConfigFor(this);
        String file = configuration.get("processFile");
        if (file != null) {
            try (InputStream stream = new FileInputStream(service.resolve(file))) {
                String content = Files.read(stream, Charset.forName("UTF-8"));
                ASTNode root = Utils.parseJSON(Logger.DEFAULT, content);
                workflow = new XOWLWorkflow(root);
                currentPhase = workflow.getPhases().get(0);
                currentActivity = currentPhase.getActivities().get(0);
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
        }
    }

    @Override
    public String getIdentifier() {
        return XOWLWorkflowService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Workflow Service";
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
    public Workflow getCurrentWorkflow() {
        retrieveWorkflow();
        return workflow;
    }

    @Override
    public WorkflowPhase getActivePhase() {
        retrieveWorkflow();
        return currentPhase;
    }

    @Override
    public WorkflowActivity getActiveActivity() {
        retrieveWorkflow();
        return currentActivity;
    }

    @Override
    public WorkflowActionReply execute(String action, Object parameter) {
        WorkflowActivity activity = getActiveActivity();
        if (activity == null)
            return new WorkflowActionReplyFailure("The workflow is not configured");
        for (WorkflowAction workflowAction : activity.getActions()) {
            if (workflowAction.getIdentifier().equals(action)) {
                return workflowAction.execute(parameter);
            }
        }
        return new WorkflowActionReplyFailure("Cannot find action " + action + " on activity " + activity.getIdentifier());
    }
}
