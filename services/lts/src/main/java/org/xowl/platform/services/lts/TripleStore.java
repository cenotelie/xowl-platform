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

package org.xowl.platform.services.lts;

import org.xowl.infra.server.api.XOWLDatabase;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.store.sparql.Result;
import org.xowl.platform.kernel.artifacts.Artifact;

import java.util.Collection;

/**
 * Represents a triple store for the platform
 *
 * @author Laurent Wouters
 */
public interface TripleStore extends XOWLDatabase {
    /**
     * Executes a SPARQL command on the triple store
     *
     * @param query The SPARQL command
     * @return The result
     */
    Result sparql(String query);

    /**
     * Gets the artifacts in this store
     *
     * @return The artifacts in this store
     */
    Collection<Artifact> getArtifacts();

    /**
     * Stores an artifact in this store
     *
     * @param artifact The artifact to store
     * @return The operation's result
     */
    XSPReply store(Artifact artifact);

    /**
     * Retrieves the artifact identified by the specified identifier
     *
     * @param identifier The identifier of a artifact
     * @return The operation's result
     */
    XSPReply retrieve(String identifier);

    /**
     * Deletes the artifact identified by the specified identifier
     *
     * @param identifier The identifier of a artifact
     * @return The operation's result
     */
    XSPReply delete(String identifier);
}
