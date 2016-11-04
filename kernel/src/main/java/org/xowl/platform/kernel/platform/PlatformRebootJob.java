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

package org.xowl.platform.kernel.platform;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobBase;

/**
 * Represents a job to reboot the platform
 *
 * @author Laurent Wouters
 */
public class PlatformRebootJob extends JobBase {
    /**
     * The job's result
     */
    private XSPReply result;

    /**
     * Initializes this job
     */
    public PlatformRebootJob() {
        super("Reboot the platform", PlatformRebootJob.class.getCanonicalName());
    }

    /**
     * Initializes this job
     *
     * @param definition The job's definition
     */
    public PlatformRebootJob(ASTNode definition) {
        super(definition);
    }

    @Override
    protected String getJSONSerializedPayload() {
        return "\"\"";
    }

    @Override
    public XSPReply getResult() {
        return result;
    }

    @Override
    public void doRun() {
        PlatformManagementService managementService = ServiceUtils.getService(PlatformManagementService.class);
        if (managementService == null) {
            result = XSPReplyServiceUnavailable.instance();
            return;
        }
        result = managementService.restart();
    }
}
