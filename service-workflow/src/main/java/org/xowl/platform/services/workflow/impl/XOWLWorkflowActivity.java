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
import org.xowl.platform.services.workflow.WorkflowActivity;
import org.xowl.store.IOUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for workflow activity
 *
 * @author Laurent Wouters
 */
public class XOWLWorkflowActivity implements WorkflowActivity {
    /**
     * The unique identifier of this activity
     */
    protected final String identifier;
    /**
     * The name of this activity
     */
    protected final String name;
    /**
     * The description of this workflow
     */
    protected final String description;
    /**
     * The actions on this activity
     */
    protected final List<WorkflowAction> actions;

    /**
     * Initializes this action
     *
     * @param node The specification
     */
    public XOWLWorkflowActivity(ASTNode node) {
        this.actions = new ArrayList<>();
        String id = "";
        String name = "";
        String description = "";
        for (ASTNode member : node.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                id = IOUtils.unescape(member.getChildren().get(1).getValue());
                id = id.substring(1, id.length() - 1);
            } else if ("name".equals(head)) {
                name = IOUtils.unescape(member.getChildren().get(1).getValue());
                name = name.substring(1, name.length() - 1);
            } else if ("description".equals(head)) {
                description = IOUtils.unescape(member.getChildren().get(1).getValue());
                description = description.substring(1, description.length() - 1);
            } else if ("actions".equals(head)) {
                for (ASTNode actionNode : member.getChildren().get(1).getChildren()) {
                    String type = "";
                    for (ASTNode actionMember : actionNode.getChildren()) {
                        head = IOUtils.unescape(actionMember.getChildren().get(0).getValue());
                        head = head.substring(1, head.length() - 1);
                        if ("type".equals(head)) {
                            type = IOUtils.unescape(actionMember.getChildren().get(1).getValue());
                            type = type.substring(1, type.length() - 1);
                            break;
                        }
                    }
                    actions.add(XOWLWorkflowService.newAction(type, actionNode));
                }
            }
        }
        this.identifier = id;
        this.name = name;
        this.description = description;
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
        StringBuilder builder = new StringBuilder();
        builder.append("{\"identifier\": \"");
        builder.append(IOUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(name));
        builder.append("\", \"description\": \"");
        builder.append(IOUtils.escapeStringJSON(description));
        builder.append("\", \"actions\": [");
        for (int i = 0; i != actions.size(); i++) {
            if (i != 0)
                builder.append(", ");
            builder.append(actions.get(i).serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }

    @Override
    public Collection<WorkflowAction> getActions() {
        return Collections.unmodifiableList(actions);
    }
}
