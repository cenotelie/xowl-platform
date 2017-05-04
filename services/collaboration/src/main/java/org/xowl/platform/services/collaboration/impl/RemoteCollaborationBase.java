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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.services.collaboration.CollaborationStatus;
import org.xowl.platform.services.collaboration.RemoteCollaboration;

/**
 * Implements a remote collaboration access through the network
 *
 * @author Laurent Wouters
 */
public class RemoteCollaborationBase implements RemoteCollaboration {
    /**
     * The identifier of the remote collaboration
     */
    protected final String identifier;
    /**
     * The name of the remote collaboration
     */
    protected final String name;
    /**
     * The API endpoint for the remote collaboration
     */
    protected final String endpoint;
    /**
     * The status of the collaboration
     */
    protected final CollaborationStatus status;

    /**
     * Initializes this remote collaboration
     *
     * @param identifier The identifier of the remote collaboration
     * @param name       The name of the remote collaboration
     * @param endpoint   The API endpoint for the remove collaboration
     */
    public RemoteCollaborationBase(String identifier, String name, String endpoint) {
        this.identifier = identifier;
        this.name = name;
        this.endpoint = endpoint;
        this.status = CollaborationStatus.Invalid;
    }

    /**
     * Initializes this remote collaboration
     *
     * @param definition The AST node for the serialized definition
     */
    public RemoteCollaborationBase(ASTNode definition) {
        String identifier = "";
        String name = "";
        String endpoint = "";
        String status = CollaborationStatus.Invalid.toString();
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            switch (head) {
                case "identifier": {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    identifier = value.substring(1, value.length() - 1);
                    break;
                }
                case "name": {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    name = value.substring(1, value.length() - 1);
                    break;
                }
                case "endpoint": {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    endpoint = value.substring(1, value.length() - 1);
                    break;
                }
                case "status": {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    status = value.substring(1, value.length() - 1);
                    break;
                }
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.endpoint = endpoint;
        this.status = CollaborationStatus.valueOf(status);
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
    public String getApiEndpoint() {
        return endpoint;
    }

    @Override
    public CollaborationStatus getStatus() {
        return status;
    }

    @Override
    public XSPReply getManifest() {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply getArtifactsForInput(String specificationId) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply getArtifactsForOutput(String specificationId) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply archive() {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply restart() {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply delete() {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply retrieveOutput(String specificationId, String artifactId) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(RemoteCollaboration.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"endpoint\": \"" +
                TextUtils.escapeStringJSON(endpoint) +
                "\", \"status\": \"" +
                TextUtils.escapeStringJSON(getStatus().toString()) +
                "\"}";
    }
}
