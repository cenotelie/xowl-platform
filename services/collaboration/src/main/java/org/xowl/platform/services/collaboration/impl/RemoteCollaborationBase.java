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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.services.collaboration.CollaborationStatus;
import org.xowl.platform.services.collaboration.RemoteCollaboration;
import org.xowl.platform.services.collaboration.network.CollaborationInstance;
import org.xowl.platform.services.collaboration.network.CollaborationNetworkService;

import java.util.Collection;
import java.util.Collections;

/**
 * Implements a remote collaboration access through the network
 *
 * @author Laurent Wouters
 */
public class RemoteCollaborationBase implements RemoteCollaboration {
    /**
     * The associated collaboration instance
     */
    private final CollaborationInstance collaborationInstance;
    /**
     * The parent network service
     */
    private final CollaborationNetworkService networkService;

    /**
     * Initializes this remote collaboration
     *
     * @param collaborationInstance The associated collaboration instance
     * @param networkService        The parent network service
     */
    public RemoteCollaborationBase(CollaborationInstance collaborationInstance, CollaborationNetworkService networkService) {
        this.collaborationInstance = collaborationInstance;
        this.networkService = networkService;
    }

    @Override
    public String getIdentifier() {
        return collaborationInstance.getIdentifier();
    }

    @Override
    public String getName() {
        return collaborationInstance.getName();
    }

    @Override
    public CollaborationStatus getStatus() {
        return collaborationInstance.getStatus();
    }

    @Override
    public XSPReply archive() {
        return networkService.archive(collaborationInstance.getIdentifier());
    }

    @Override
    public XSPReply restart() {
        return networkService.restart(collaborationInstance.getIdentifier());
    }

    @Override
    public XSPReply delete() {
        return networkService.delete(collaborationInstance.getIdentifier());
    }

    @Override
    public Collection<ArtifactSpecification> getInputSpecifications() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ArtifactSpecification> getOutputSpecifications() {
        return Collections.emptyList();
    }

    @Override
    public XSPReply getInputFor(String specificationId) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply getOutputFor(String specificationId) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply sendInput(ArtifactSpecification specificationId, Artifact artifact) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply retrieveOutput(String specificationId, String artifactId) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(RemoteCollaboration.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(getIdentifier()));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(getName()));
        builder.append("\", \"inputs\": [");
        boolean first = true;
        for (ArtifactSpecification specification : getInputSpecifications()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(specification);
        }
        builder.append("], \"outputs\": [");
        first = true;
        for (ArtifactSpecification specification : getOutputSpecifications()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(specification);
        }
        builder.append("]}");
        return builder.toString();
    }
}
