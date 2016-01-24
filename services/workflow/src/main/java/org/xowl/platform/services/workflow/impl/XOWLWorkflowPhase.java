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
import org.xowl.infra.store.IOUtils;
import org.xowl.platform.services.workflow.WorkflowActivity;
import org.xowl.platform.services.workflow.WorkflowPhase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for workflow phase
 *
 * @author Laurent Wouters
 */
public class XOWLWorkflowPhase implements WorkflowPhase {
    /**
     * The unique identifier of this phase
     */
    protected final String identifier;
    /**
     * The name of this phase
     */
    protected final String name;
    /**
     * The description of this workflow
     */
    protected final String description;
    /**
     * The activities in this phase
     */
    protected final List<WorkflowActivity> activities;

    /**
     * Initializes this action
     *
     * @param node The specification
     */
    public XOWLWorkflowPhase(ASTNode node) {
        this.activities = new ArrayList<>();
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
            } else if ("activities".equals(head)) {
                for (ASTNode activityNode : member.getChildren().get(1).getChildren()) {
                    activities.add(new XOWLWorkflowActivity(activityNode));
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
        builder.append("\", \"activities\": [");
        for (int i = 0; i != activities.size(); i++) {
            if (i != 0)
                builder.append(", ");
            builder.append(activities.get(i).serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }

    @Override
    public List<WorkflowActivity> getActivities() {
        return Collections.unmodifiableList(activities);
    }
}
