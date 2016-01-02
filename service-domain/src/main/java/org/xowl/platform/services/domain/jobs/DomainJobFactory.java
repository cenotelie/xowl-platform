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

package org.xowl.platform.services.domain.jobs;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.Job;
import org.xowl.platform.kernel.JobFactory;

/**
 * The factory for domain jobs
 *
 * @author Laurent Wouters
 */
public class DomainJobFactory implements JobFactory {
    @Override
    public String getIdentifier() {
        return DomainJobFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Domain Job Factory";
    }

    @Override
    public boolean canDeserialize(String type) {
        return (type.equals(PullArtifactJob.class.getCanonicalName())
                || type.equals(PushArtifactToLiveJob.class.getCanonicalName()));
    }

    @Override
    public Job newJob(String type, ASTNode definition) {
        if (type.equals(PullArtifactJob.class.getCanonicalName()))
            return new PullArtifactJob(definition);
        if (type.equals(PushArtifactToLiveJob.class.getCanonicalName()))
            return new PushArtifactToLiveJob(definition);
        return null;
    }
}
