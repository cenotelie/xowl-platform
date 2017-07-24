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

package org.xowl.platform.services.storage;

import org.xowl.infra.server.api.XOWLDatabase;
import org.xowl.infra.utils.api.Reply;
import org.xowl.platform.kernel.artifacts.Artifact;

/**
 * Represents a triple store for the platform
 *
 * @author Laurent Wouters
 */
public interface TripleStore extends XOWLDatabase {
    /**
     * Gets the artifacts in this store
     *
     * @return The operation's result which can be casted to ReplyResultCollection in case of success
     */
    Reply getArtifacts();

    /**
     * Stores an artifact in this store
     *
     * @param artifact The artifact to store
     * @return The operation's result
     */
    Reply store(Artifact artifact);

    /**
     * Retrieves the artifact identified by the specified identifier
     *
     * @param identifier The identifier of a artifact
     * @return The operation's result which can be casted to ReplyResult in case of success
     */
    Reply retrieve(String identifier);

    /**
     * Deletes the artifact identified by the specified identifier
     *
     * @param identifier The identifier of a artifact
     * @return The operation's result
     */
    Reply delete(String identifier);
}
