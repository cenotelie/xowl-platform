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

package org.xowl.platform.kernel.jobs;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.remote.Deserializer;

/**
 * Represents a job running on a remote platform
 *
 * @author Laurent Wouters
 */
public class JobRemote extends JobBase {
    /**
     * The payload for this job
     */
    private final Object payload;
    /**
     * The job's result
     */
    private final XSPReply result;

    /**
     * Initializes this job
     *
     * @param definition   The serialized definition
     * @param deserializer The current deserializer
     */
    public JobRemote(ASTNode definition, Deserializer deserializer) {
        super(definition);
        Object payload = null;
        XSPReply result = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("payload".equals(head)) {
                payload = XSPReplyUtils.getJSONObject(member.getChildren().get(1), deserializer);
            } else if ("result".equals(head)) {
                result = XSPReplyUtils.parseJSONResult(member.getChildren().get(1), deserializer);
            }
        }
        this.payload = payload;
        this.result = result;
    }

    @Override
    protected String getJSONSerializedPayload() {
        return TextUtils.serializeJSON(payload);
    }

    @Override
    public void doRun() {
        // do nothing
    }

    @Override
    public XSPReply getResult() {
        return result;
    }
}
