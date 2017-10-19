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

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.ApiDeserializer;
import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.commons.utils.api.ReplyUtils;
import fr.cenotelie.commons.utils.json.Json;
import fr.cenotelie.hime.redist.ASTNode;

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
    private final Reply result;

    /**
     * Initializes this job
     *
     * @param definition The serialized definition
     * @param parent     The parent deserializer
     */
    public JobRemote(ASTNode definition, ApiDeserializer parent) {
        super(definition);
        Object payload = null;
        Reply result = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("payload".equals(head)) {
                payload = parent.deserialize(member.getChildren().get(1), null);
            } else if ("result".equals(head)) {
                result = ReplyUtils.parse(member.getChildren().get(1), parent);
            }
        }
        this.payload = payload;
        this.result = result;
    }

    @Override
    protected String getJSONSerializedPayload() {
        return Json.serialize(payload);
    }

    @Override
    public void doRun() {
        // do nothing
    }

    @Override
    public Reply getResult() {
        return result;
    }
}
