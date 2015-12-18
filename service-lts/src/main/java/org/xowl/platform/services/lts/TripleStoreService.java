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

import org.xowl.platform.kernel.Artifact;
import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.Service;
import org.xowl.store.sparql.Result;

import java.util.Collection;

/**
 * Represents a triple-store service for the platform
 *
 * @author Laurent Wouters
 */
public interface TripleStoreService extends Service {
    /**
     * Executes a SPARQL command on the triple store
     *
     * @param query The SPARQL command
     * @return The result
     */
    Result sparql(String query);

    /**
     * Sends the metadata of an artifact to the triple store
     *
     * @param artifact The artifact
     * @return Whether the operation succeeded
     */
    boolean store(Artifact artifact);

    /**
     * Deletes an artifact from the triple store
     *
     * @param artifact The artifact
     * @return Whether the operation succeeded
     */
    boolean delete(Artifact artifact);

    /**
     * Deletes an artifact from the triple store
     *
     * @param identifier The identifier of the artifact to delete
     * @return Whether the operation succeeded
     */
    boolean delete(String identifier);

    /**
     * Retrieves the artifact from the triple store
     *
     * @param identifier The identifier of a artifact
     * @return The artifact, or null it is unknown or cannot be retrieved
     */
    Artifact retrieve(String identifier);

    /**
     * Lists the artifacts in the triple store
     *
     * @return The list of the stored artifacts
     */
    Collection<Artifact> listArtifacts();
}
