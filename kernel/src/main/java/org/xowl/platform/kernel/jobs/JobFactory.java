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

package org.xowl.platform.kernel.jobs;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.Registrable;

/**
 * A factory of jobs that is used to produce jobs from their serialized representation
 *
 * @author Laurent Wouters
 */
public interface JobFactory extends Registrable {
    /**
     * Gets whether this factory can de-serialize a job of the given type
     *
     * @param type The job type
     * @return Whether the factory can handle it
     */
    boolean canDeserialize(String type);

    /**
     * Creates a job from the specified definition
     *
     * @param type       The job type
     * @param definition The definition of a job
     * @return The created job
     */
    Job newJob(String type, ASTNode definition);
}
