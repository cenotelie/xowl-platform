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

/**
 * Implements a reply to a failed workflow action
 *
 * @author Laurent Wouters
 */
public class WorkflowActionReplyFailure implements WorkflowActionReply {
    /**
     * The default instance
     */
    public static final WorkflowActionReplyFailure INSTANCE = new WorkflowActionReplyFailure(null);

    /**
     * The message, if any
     */
    private final String message;

    /**
     * Initializes this failure
     *
     * @param message The message
     */
    public WorkflowActionReplyFailure(String message) {
        this.message = message;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String serializedString() {
        return message != null ? "ERROR: " + message : "ERROR";
    }

    @Override
    public String serializedJSON() {
        return "{\"isSuccess\":\"false\", \"message\": \"" + (message != null ? message : "") + "\"}";
    }
}
