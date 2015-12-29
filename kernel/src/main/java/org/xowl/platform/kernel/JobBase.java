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

package org.xowl.platform.kernel;

import org.xowl.hime.redist.ASTNode;
import org.xowl.store.IOUtils;
import org.xowl.utils.concurrent.SafeRunnable;
import org.xowl.utils.logging.Logger;

import java.util.UUID;

/**
 * Base implementation of a job on the platform
 *
 * @author Laurent Wouters
 */
public abstract class JobBase extends SafeRunnable implements Job {
    /**
     * The job's identifier
     */
    protected final String identifier;
    /**
     * The job's name
     */
    protected final String name;
    /**
     * The job's type
     */
    protected final String type;

    /**
     * Initializes this job
     *
     * @param name The job's name
     * @param type The job's type
     */
    public JobBase(String name, String type) {
        super(Logger.DEFAULT);
        this.identifier = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
    }

    /**
     * Initializes this job
     *
     * @param definition The JSON definition
     */
    public JobBase(ASTNode definition) {
        super(Logger.DEFAULT);
        String id = null;
        String name = null;
        String type = null;
        for (ASTNode member : definition.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            String value = IOUtils.unescape(member.getChildren().get(1).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                id = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                name = value.substring(1, value.length() - 1);
            } else if ("type".equals(head)) {
                type = value;
            }
        }
        this.identifier = id;
        this.name = name;
        this.type = type;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"identifier\": \""
                + IOUtils.escapeStringJSON(identifier)
                + "\", \"name\":\""
                + IOUtils.escapeStringJSON(name)
                + "\", \"type\": \""
                + IOUtils.escapeStringJSON(type)
                + "\", \"payload\": "
                + getJSONSerializedPayload()
                + "}";
    }

    /**
     * Gets the JSON serialization of the job's payload
     *
     * @return The serialization
     */
    protected abstract String getJSONSerializedPayload();
}
