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

package org.xowl.platform.services.collaboration.jobs;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobBase;
import org.xowl.platform.services.collaboration.CollaborationNetworkService;
import org.xowl.platform.services.collaboration.CollaborationSpecification;

/**
 * Implements a job to spawn a new collaboration
 *
 * @author Laurent Wouters
 */
public class CollaborationSpawnJob extends JobBase {
    /**
     * The specification for the new collaboration
     */
    private final CollaborationSpecification specification;
    /**
     * The job's result
     */
    private XSPReply result;

    /**
     * Initializes this job
     *
     * @param specification The specification for the new collaboration
     */
    public CollaborationSpawnJob(CollaborationSpecification specification) {
        super("Spawning collaboration " + specification.getName(), CollaborationSpawnJob.class.getCanonicalName());
        this.specification = specification;
    }

    /**
     * Initializes this job
     *
     * @param definition The JSON definition
     */
    public CollaborationSpawnJob(ASTNode definition) {
        super(definition);
        this.specification = new CollaborationSpecification(getPayloadNode(definition));
    }

    @Override
    protected String getJSONSerializedPayload() {
        return specification.serializedJSON();
    }

    @Override
    public XSPReply getResult() {
        return result;
    }

    @Override
    public void doRun() {
        CollaborationNetworkService networkService = Register.getComponent(CollaborationNetworkService.class);
        if (networkService == null) {
            result = XSPReplyServiceUnavailable.instance();
            return;
        }
        result = networkService.spawn(specification);
    }
}
