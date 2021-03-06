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

package org.xowl.platform.services.connection;

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.*;
import fr.cenotelie.commons.utils.http.HttpResponse;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Base implementation of a domain connector
 *
 * @author Laurent Wouters
 */
public abstract class ConnectorServiceBase implements ConnectorService, HttpApiService {
    /**
     * The maximum number of queued data packages for input toward the platform
     */
    protected static final int INPUT_QUEUE_MAX_CAPACITY = 16;

    /**
     * The identifier for this connector
     */
    protected final String identifier;
    /**
     * The name for this connector
     */
    protected final String name;
    /**
     * The API URIs for this connector
     */
    protected final String[] uris;
    /**
     * The queue iof input data packages, i.e. packages toward the platform
     */
    protected final BlockingQueue<Artifact> input;
    /**
     * Service action to pull an artifact from this connector
     */
    protected final SecuredAction actionPull;
    /**
     * Service action to push an artifact to the client
     */
    protected final SecuredAction actionPush;
    /**
     * The secured action for this connector
     */
    protected final SecuredAction[] actions;

    /**
     * Initializes this connector
     *
     * @param specification The specification for the new connector
     */
    protected ConnectorServiceBase(ConnectorServiceData specification) {
        this.identifier = specification.getIdentifier();
        this.name = specification.getName();
        this.uris = new String[specification.getUris().length];
        for (int i = 0; i != this.uris.length; i++)
            this.uris[i] = PlatformHttp.getUriPrefixApi() + "/" + specification.getUris()[i];
        this.input = new ArrayBlockingQueue<>(INPUT_QUEUE_MAX_CAPACITY);
        this.actionPull = new SecuredAction(identifier + ".Pull", "Connection Service - " + name + " - Pull Artifact");
        this.actionPush = new SecuredAction(identifier + ".Push", "Connection Service - " + name + " - Push Artifact");
        this.actions = new SecuredAction[]{
                actionPull,
                actionPush
        };
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
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SecuredAction[] getActions() {
        return actions;
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
    public Reply pullArtifact() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(actionPull);
        if (!reply.isSuccess())
            return reply;
        return doPullArtifact();
    }

    /**
     * Realizes the action to pull an artifact
     *
     * @return The operation's result
     */
    protected Reply doPullArtifact() {
        Artifact artifact = input.poll();
        if (artifact == null)
            return new ReplyApiError(ConnectionService.ERROR_EMPTY_QUEUE);
        return new ReplyResult<>(artifact);
    }

    @Override
    public Reply pushArtifact(Artifact artifact) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(actionPush);
        if (!reply.isSuccess())
            return reply;
        return doPushArtifact(artifact);
    }

    /**
     * Realizes the action to push an artifact to the client
     *
     * @param artifact The artifact to push
     * @return The operation's result
     */
    protected Reply doPushArtifact(Artifact artifact) {
        return ReplyUnsupported.instance();
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        for (int i = 0; i != uris.length; i++) {
            if (uris[i].equals(request.getUri()))
                return PRIORITY_NORMAL;
        }
        return CANNOT_HANDLE;
    }

    @Override
    public boolean requireAuth(HttpApiRequest request) {
        return true;
    }

    @Override
    public HttpResponse handle(SecurityService securedService, HttpApiRequest request) {
        return ReplyUtils.toHttpResponse(ReplyUnsupported.instance());
    }

    @Override
    public HttpApiResource getApiSpecification() {
        return null;
    }

    @Override
    public HttpApiResource getApiDocumentation() {
        return null;
    }

    @Override
    public HttpApiResource[] getApiOtherResources() {
        return null;
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(ConnectorService.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"specification\": ");
        if (getApiSpecification() != null)
            builder.append(getApiSpecification().serializedJSON());
        else
            builder.append("{}");
        builder.append(", \"documentation\": ");
        if (getApiDocumentation() != null)
            builder.append(getApiDocumentation().serializedJSON());
        else
            builder.append("{}");
        builder.append(", \"uris\": [");
        boolean first = true;
        for (String uri : uris) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(uri));
            builder.append("\"");
        }
        builder.append("], \"canPullInput\": ");
        builder.append(canPullInput());
        builder.append(", \"queue\": [");
        first = true;
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
