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
import org.xowl.platform.services.workflow.WorkflowAction;
import org.xowl.platform.services.workflow.WorkflowActionReply;
import org.xowl.platform.services.workflow.WorkflowActionReplySuccess;
import org.xowl.store.IOUtils;

/**
 * Base class for workflow actions
 *
 * @author Laurent Wouters
 */
public class XOWLWorkflowAction implements WorkflowAction {
    /**
     * The unique identifier of this action
     */
    protected final String identifier;
    /**
     * The name of this action
     */
    protected final String name;
    /**
     * Whether the current activity is finished by this successful action
     */
    protected final boolean finishOnSuccess;

    /**
     * Gets whether the current activity is finished by this successful action
     *
     * @return Whether the current activity is finished by this successful action
     */
    public boolean isFinishOnSuccess() {
        return finishOnSuccess;
    }

    /**
     * Initializes this action
     *
     * @param node The specification
     */
    public XOWLWorkflowAction(ASTNode node) {
        String id = null;
        String name = null;
        String finish = null;
        for (ASTNode member : node.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            String value = IOUtils.unescape(member.getChildren().get(1).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                id = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                name = value.substring(1, value.length() - 1);
            } else if ("finishOnSuccess".equals(head)) {
                finish = value;
            }
        }
        this.identifier = id;
        this.name = name;
        this.finishOnSuccess = "true".equalsIgnoreCase(finish);
    }

    @Override
    public WorkflowActionReply execute(Object parameter) {
        return WorkflowActionReplySuccess.INSTANCE;
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
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        return "{\"identifier\": \"" + IOUtils.escapeStringJSON(identifier) + "\", \"name\": \"" + IOUtils.escapeStringJSON(name) + "\", \"finishOnSuccess\": " + finishOnSuccess + "}";
    }
}
