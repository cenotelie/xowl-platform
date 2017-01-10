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

package org.xowl.platform.services.collaboration.impl;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.collaboration.CollaborationPattern;
import org.xowl.platform.services.collaboration.CollaborationService;
import org.xowl.platform.services.collaboration.CollaborationSpecification;
import org.xowl.platform.services.collaboration.RemoteCollaboration;
import org.xowl.platform.services.collaboration.network.CollaborationNetworkService;

import java.util.Collection;

/**
 * Implements the collaboration service for this platform
 *
 * @author Laurent Wouters
 */
public class XOWLCollaborationService implements CollaborationService, HttpApiService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/services/collaboration";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(MasterNetworkService.class, "/org/xowl/platform/services/collaboration/api_collaboration.raml", "Collaboration Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(MasterNetworkService.class, "/org/xowl/platform/services/collaboration/api_collaboration.html", "Collaboration Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The collaboration network service
     */
    private final CollaborationNetworkService networkService;

    /**
     * Initializes this service
     */
    public XOWLCollaborationService() {
        ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
        Configuration configuration = configurationService.getConfigFor(CollaborationService.class.getCanonicalName());
        this.networkService = ServiceUtils.instantiate(configuration.get("network", "service"));
    }

    @Override
    public String getIdentifier() {
        return XOWLCollaborationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - Collaboration Service";
    }

    @Override
    public Collection<ArtifactSpecification> getInputSpecifications() {
        return null;
    }

    @Override
    public Collection<ArtifactSpecification> getOutputSpecifications() {
        return null;
    }

    @Override
    public Collection<Artifact> getInputFor(ArtifactSpecification specification) {
        return null;
    }

    @Override
    public Collection<Artifact> getOutputFor(ArtifactSpecification specification) {
        return null;
    }

    @Override
    public XSPReply addInput(ArtifactSpecification specification, Artifact artifact) {
        return null;
    }

    @Override
    public XSPReply publishOutput(ArtifactSpecification specification, Artifact artifact) {
        return null;
    }

    @Override
    public Collection<PlatformRole> getRoles() {
        return null;
    }

    @Override
    public CollaborationPattern getCollaborationPattern() {
        return null;
    }

    @Override
    public Collection<RemoteCollaboration> getNeighbours() {
        return networkService.getCollaborations();
    }

    @Override
    public XSPReply spawn(CollaborationSpecification specification) {
        return networkService.spawn(specification);
    }

    @Override
    public XSPReply terminate(RemoteCollaboration collaboration) {
        return networkService.terminate(collaboration);
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
