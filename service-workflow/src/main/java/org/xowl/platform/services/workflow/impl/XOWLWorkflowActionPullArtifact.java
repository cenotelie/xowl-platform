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
import org.xowl.platform.services.workflow.WorkflowUtils;
import org.xowl.store.IOUtils;
import org.xowl.store.xsp.XSPReply;

/**
 * Represents an action in a workflow that consists in pulling an artifact from a connector to the platform
 *
 * @author Laurent Wouters
 */
public class XOWLWorkflowActionPullArtifact extends XOWLWorkflowAction {
    /**
     * The connector to pull from
     */
    private final String connectorId;

    /**
     * Initializes this action
     *
     * @param node The specification
     */
    public XOWLWorkflowActionPullArtifact(ASTNode node) {
        super(node);
        String connectorId = null;
        for (ASTNode member : node.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            String value = IOUtils.unescape(member.getChildren().get(1).getValue());
            head = head.substring(1, head.length() - 1);
            if ("connectorId".equals(head)) {
                connectorId = value.substring(1, value.length() - 1);
            }
        }
        this.connectorId = connectorId;
    }

    @Override
    public XSPReply execute(Object parameter) {
        return WorkflowUtils.pullArtifact(connectorId);
    }

    @Override
    public String serializedJSON() {
        return "{\"identifier\": \"" + IOUtils.escapeStringJSON(identifier) + "\", " +
                "\"name\": \"" + IOUtils.escapeStringJSON(name) + "\", " +
                "\"finishOnSuccess\": " + finishOnSuccess + ", " +
                "\"type\": \"XOWLWorkflowActionPullArtifact\", " +
                "\"connectorId\": \"" + IOUtils.escapeStringJSON(connectorId) + "\"}";
    }
}
