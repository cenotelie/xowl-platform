/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.services.collaboration.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.services.collaboration.CollaborationStatus;

/**
 * Implements the data about a collaboration provisioned on the local file system
 *
 * @author Laurent Wouters
 */
public class FSCollaborationInstance implements Identifiable, Serializable {
    /**
     * The unique identifier for this instance
     */
    private final String identifier;
    /**
     * The name for the instance
     */
    private final String name;
    /**
     * The API endpoint for the instance
     */
    private final String endpoint;
    /**
     * The status of the instance
     */
    private CollaborationStatus status;
    /**
     * The selected network port for this instance
     */
    private final int port;

    /**
     * Gets the API endpoint for the instance
     *
     * @return The API endpoint for the instance
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the status of the instance
     *
     * @return The status of the instance
     */
    public CollaborationStatus getStatus() {
        return status;
    }

    /**
     * Gets the selected network port for this instance
     *
     * @return The selected network port for this instance
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the status of this instance
     *
     * @param status The status of this instance
     */
    public void setStatus(CollaborationStatus status) {
        this.status = status;
    }

    /**
     * Initializes this structure
     *
     * @param identifier The unique identifier for this instance
     * @param name       The name for the instance
     * @param endpoint   The API endpoint for the instance
     * @param status     The status of the instance
     */
    public FSCollaborationInstance(String identifier, String name, String endpoint, CollaborationStatus status, int port) {
        this.identifier = identifier;
        this.name = name;
        this.endpoint = endpoint;
        this.status = status;
        this.port = port;
    }

    /**
     * Initializes this structure
     *
     * @param definition The AST node for the serialized definition
     */
    public FSCollaborationInstance(ASTNode definition) {
        String identifier = "";
        String name = "";
        String endpoint = "";
        String status = "";
        String port = "";
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("endpoint".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                endpoint = value.substring(1, value.length() - 1);
            } else if ("status".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                status = value.substring(1, value.length() - 1);
            } else if ("port".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                port = value.substring(1, value.length() - 1);
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.endpoint = endpoint;
        this.status = CollaborationStatus.valueOf(status);
        this.port = Integer.parseInt(port);
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
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(FSCollaborationInstance.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"endpoint\": \"" +
                TextUtils.escapeStringJSON(endpoint) +
                "\", \"status\": \"" +
                TextUtils.escapeStringJSON(status.toString()) +
                "\"}";
    }
}
