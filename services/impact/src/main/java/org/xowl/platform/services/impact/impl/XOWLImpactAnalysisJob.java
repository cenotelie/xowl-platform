/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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

package org.xowl.platform.services.impact.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.platform.kernel.JobBase;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.impact.ImpactAnalysisSetup;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;

/**
 * Represents a job that performs an impact analysis
 *
 * @author Laurent Wouters
 */
class XOWLImpactAnalysisJob extends JobBase {
    /**
     * The analysis setup
     */
    private final ImpactAnalysisSetup setup;
    /**
     * The result, if any
     */
    private XSPReply result;

    /**
     * Initializes this job
     *
     * @param setup The analysis setup
     */
    public XOWLImpactAnalysisJob(ImpactAnalysisSetup setup) {
        super(setup.getName(), XOWLImpactAnalysisJob.class.getCanonicalName());
        this.setup = setup;
    }

    /**
     * Initializes this job
     *
     * @param definition The payload definition
     */
    public XOWLImpactAnalysisJob(ASTNode definition) {
        super(definition);
        this.setup = new XOWLImpactAnalysisSetup(getPayloadNode(definition));
    }

    @Override
    protected String getJSONSerializedPayload() {
        return setup.serializedJSON();
    }

    @Override
    public XSPReply getResult() {
        return result;
    }

    @Override
    public void doRun() {
        TripleStoreService tripleStoreService = ServiceUtils.getService(TripleStoreService.class);
        if (tripleStoreService == null) {
            result = new XSPReplyFailure("Failed to resolve the triple store service");
            return;
        }
        TripleStore live = tripleStoreService.getLiveStore();
        // FIXME: run the analysis here
    }
}
