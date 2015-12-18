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

package org.xowl.platform.services.domain;

import org.xowl.platform.kernel.Artifact;
import org.xowl.store.IOUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Base implemented of a domain connector
 *
 * @author Laurent Wouters
 */
public abstract class BaseDomainConnector implements DomainConnectorService {
    /**
     * The maximum number of queued data packages for input toward the platform
     */
    protected static final int INPUT_QUEUE_MAX_CAPACITY = 16;

    /**
     * The queue iof input data packages, i.e. packages toward the platform
     */
    private final BlockingQueue<Artifact> input;

    /**
     * Initializes this connector
     */
    protected BaseDomainConnector() {
        this.input = new ArrayBlockingQueue<>(INPUT_QUEUE_MAX_CAPACITY);
    }

    /**
     * Queues a data package as input toward the platform
     *
     * @param artifact The data package to queue
     * @return Whether the operation succeeded
     */
    protected boolean queueInput(Artifact artifact) {
        try {
            input.put(artifact);
            return true;
        } catch (InterruptedException exception) {
            return false;
        }
    }

    @Override
    public boolean canPullInput() {
        return false;
    }

    @Override
    public List<Artifact> getQueuedInputs() {
        List<Artifact> result = new ArrayList<>(INPUT_QUEUE_MAX_CAPACITY);
        for (Artifact artifact : input) {
            result.add(artifact);
        }
        return result;
    }

    @Override
    public int getQueuedLength() {
        return input.size();
    }

    @Override
    public boolean hasQueuedInput() {
        return !input.isEmpty();
    }

    @Override
    public Artifact getNextInput(boolean block) {
        if (block) {
            try {
                return input.take();
            } catch (InterruptedException exception) {
                return null;
            }
        } else {
            return input.poll();
        }
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"connector\", \"identifier\": \"");
        builder.append(IOUtils.escapeStringJSON(getIdentifier()));
        builder.append("\", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(getName()));
        builder.append("\", \"version\": \"");
        builder.append(IOUtils.escapeStringJSON(getVersion()));
        builder.append("\", \"canPullInput\": ");
        builder.append(canPullInput());
        builder.append(", \"queue\": [");
        boolean first = true;
        for (Artifact artifact : getQueuedInputs()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(artifact.serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}
