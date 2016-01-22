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

package org.xowl.platform.satellites.activiti;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.JobBase;
import org.xowl.store.IOUtils;
import org.xowl.store.xsp.XSPReply;
import org.xowl.store.xsp.XSPReplyUtils;

/**
 * Represents a job on the xOWL platform
 *
 * @author Laurent Wouters
 */
public class ForeignJob extends JobBase {
    /**
     * The XSP result, if any
     */
    private final XSPReply result;

    /**
     * Initializes this job
     *
     * @param definition The job definition
     */
    public ForeignJob(ASTNode definition) {
        super(definition);
        XSPReply res = null;
        for (ASTNode member : definition.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("result".equals(head)) {
                ASTNode value = member.getChildren().get(1);
                if (!value.getChildren().isEmpty())
                    res = XSPReplyUtils.parseJSONResult(value);
            }
        }
        result = res;
    }

    @Override
    protected String getJSONSerializedPayload() {
        return "";
    }

    @Override
    public XSPReply getResult() {
        return result;
    }

    @Override
    public void doRun() {
        // do nothing
    }
}