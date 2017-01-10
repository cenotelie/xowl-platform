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

package org.xowl.platform.services.collaboration.network.impl;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.collaboration.CollaborationService;
import org.xowl.platform.services.collaboration.CollaborationSpecification;
import org.xowl.platform.services.collaboration.RemoteCollaboration;
import org.xowl.platform.services.collaboration.network.CollaborationInstance;
import org.xowl.platform.services.collaboration.network.CollaborationNetworkService;
import org.xowl.platform.services.collaboration.network.CollaborationProvisioner;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implements a simple collaboration network management service
 *
 * @author Laurent Wouters
 */
public class SimpleCollaborationNetworkService implements CollaborationNetworkService, HttpApiService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/services/collaboration/network";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(SimpleCollaborationNetworkService.class, "/org/xowl/platform/services/collaboration/api_network.raml", "Collaboration Network Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(SimpleCollaborationNetworkService.class, "/org/xowl/platform/services/collaboration/api_network.html", "Collaboration Network Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The provisioner to use
     */
    private final CollaborationProvisioner provisioner;

    /**
     * Initializes this service
     */
    public SimpleCollaborationNetworkService() {
        ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
        Configuration configuration = configurationService.getConfigFor(CollaborationService.class.getCanonicalName());
        this.provisioner = ServiceUtils.instantiate(configuration.get("network", "provisioner"));
    }

    @Override
    public String getIdentifier() {
        return SimpleCollaborationNetworkService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - Collaboration Network Service";
    }

    @Override
    public Collection<RemoteCollaboration> getCollaborations() {
        Collection<RemoteCollaboration> result = new ArrayList<>();
        for (CollaborationInstance instance : provisioner.getInstances()) {
            result.add(new NetworkRemoteCollaboration(instance));
        }
        return result;
    }

    @Override
    public XSPReply spawn(CollaborationSpecification specification) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply terminate(RemoteCollaboration collaboration) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        return null;
    }

    @Override
    public HttpApiResource getApiSpecification() {
        return RESOURCE_SPECIFICATION;
    }

    @Override
    public HttpApiResource getApiDocumentation() {
        return RESOURCE_DOCUMENTATION;
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
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(HttpApiService.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"specification\": " +
                RESOURCE_SPECIFICATION.serializedJSON() +
                ", \"documentation\": " +
                RESOURCE_DOCUMENTATION.serializedJSON() +
                "}";
    }
}
