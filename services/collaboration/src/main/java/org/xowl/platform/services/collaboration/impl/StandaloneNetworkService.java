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
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.platform.services.collaboration.CollaborationNetworkService;
import org.xowl.platform.services.collaboration.CollaborationSpecification;
import org.xowl.platform.services.collaboration.CollaborationStatus;
import org.xowl.platform.services.collaboration.RemoteCollaboration;

import java.util.Collection;
import java.util.Collections;

/**
 * Implements a collaboration network service for stand-alone platforms
 *
 * @author Laurent Wouters
 */
public class StandaloneNetworkService implements CollaborationNetworkService {

    @Override
    public String getIdentifier() {
        return StandaloneNetworkService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - Collaboration Network Service (Standalone)";
    }

    @Override
    public Collection<RemoteCollaboration> getNeighbours() {
        return Collections.emptyList();
    }

    @Override
    public RemoteCollaboration getNeighbour(String collaborationId) {
        return null;
    }

    @Override
    public CollaborationStatus getNeighbourStatus(String collaborationId) {
        return CollaborationStatus.Invalid;
    }

    @Override
    public XSPReply getNeighbourManifest(String collaborationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply getNeighbourInputsFor(String collaborationId, String specificationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply getNeighbourOutputsFor(String collaborationId, String specificationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply lookupSpecifications(String input) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply spawn(CollaborationSpecification specification) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply archive(String collaborationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply restart(String collaborationId) {
        return XSPReplyNotFound.instance();
    }

    @Override
    public XSPReply delete(String collaborationId) {
        return XSPReplyNotFound.instance();
    }
}
