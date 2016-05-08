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

package org.xowl.platform.services.connection.jobs;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobFactory;

/**
 * The factory for connector-related jobs
 *
 * @author Laurent Wouters
 */
public class ConnectorJobFactory implements JobFactory {
    @Override
    public String getIdentifier() {
        return ConnectorJobFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Domain Job Factory";
    }

    @Override
    public boolean canDeserialize(String type) {
        return (type.equals(PullArtifactJob.class.getCanonicalName()) ||
                type.equals(PushArtifactJob.class.getCanonicalName()));
    }

    @Override
    public Job newJob(String type, ASTNode definition) {
        if (type.equals(PullArtifactJob.class.getCanonicalName()))
            return new PullArtifactJob(definition);
        if (type.equals(PushArtifactJob.class.getCanonicalName()))
            return new PushArtifactJob(definition);
        return null;
    }
}
