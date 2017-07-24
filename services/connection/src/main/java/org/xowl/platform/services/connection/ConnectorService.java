/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.services.connection;

import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.Serializable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.List;

/**
 * Represents a service that handles the connection of a technology for a domain to the platform.
 * In the context of the platform, a domain is a scientific or technological field of study that is supposed to be supported by some tooling.
 * A connector service must:
 * - handle the technology specific to a domain.
 * - capture the intention of the human users in the domain, i.e. the concepts and related information.
 * - translates this intention into information that can be federated into the platform.
 *
 * @author Laurent Wouters
 */
public interface ConnectorService extends SecuredService, Serializable {
    /**
     * Gets whether this connector supports pulling artifacts from the client
     * Clients are expected to push artifacts to the platform, but sometimes they can be pulled without a client explicitly pushing them.
     *
     * @return Whether artifacts can be pulled
     */
    boolean canPullInput();

    /**
     * Gets the queued artifacts for input in order
     * This method cannot be used to update the queue. The contained artifacts should not be modified.
     *
     * @return The queued artifacts
     */
    List<Artifact> getQueuedInputs();

    /**
     * Gets the number of queued artifacts for input
     *
     * @return The number of queued artifacts
     */
    int getQueuedLength();

    /**
     * Gets whether an artifact is queued for input
     *
     * @return Whether an artifact is queued for input
     */
    boolean hasQueuedInput();

    /**
     * Gets the next queued artifact
     *
     * @return The operation's result which can be casted to ReplyResult in case of success
     */
    Reply pullArtifact();

    /**
     * Pushes an artifact to the associated client
     *
     * @param artifact The artifact to push
     * @return The operation's result
     */
    Reply pushArtifact(Artifact artifact);
}
