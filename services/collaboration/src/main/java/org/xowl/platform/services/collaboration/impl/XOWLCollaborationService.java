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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.platform.PlatformRoleBase;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.collaboration.*;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the collaboration service for this platform
 *
 * @author Laurent Wouters
 */
public class XOWLCollaborationService extends XOWLCollaborationLocalService implements CollaborationService, HttpApiService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/services/collaboration";
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLCollaborationService.class, "/org/xowl/platform/services/collaboration/api_service_collaboration.raml", "Collaboration Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLCollaborationService.class, "/org/xowl/platform/services/collaboration/api_service_collaboration.html", "Collaboration Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The collaboration network service
     */
    private CollaborationNetworkService networkService;

    /**
     * Initializes this service
     */
    public XOWLCollaborationService() {
        super();
    }

    /**
     * Resolves the current collaboration network service
     *
     * @return The collaboration network service
     */
    private synchronized CollaborationNetworkService getNetworkService() {
        if (networkService == null) {
            ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
            Configuration configuration = configurationService.getConfigFor(CollaborationService.class.getCanonicalName());
            String identifier = configuration.get("network", "service");
            for (CollaborationNetworkServiceProvider provider : Register.getComponents(CollaborationNetworkServiceProvider.class)) {
                networkService = provider.instantiate(identifier);
                if (networkService != null)
                    break;
            }
        }
        return networkService;
    }

    @Override
    public String getIdentifier() {
        return XOWLCollaborationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Collaboration Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public Collection<RemoteCollaboration> getNeighbours() {
        // authorization is delegated to the network service
        return getNetworkService().getNeighbours();
    }

    @Override
    public RemoteCollaboration getNeighbour(String collaborationId) {
        // authorization is delegated to the network service
        return getNetworkService().getNeighbour(collaborationId);
    }

    @Override
    public CollaborationStatus getNeighbourStatus(String collaborationId) {
        // authorization is delegated to the network service
        return getNetworkService().getNeighbourStatus(collaborationId);
    }

    @Override
    public XSPReply getNeighbourManifest(String collaborationId) {
        // authorization is delegated to the network service
        return getNetworkService().getNeighbourManifest(collaborationId);
    }

    @Override
    public XSPReply getNeighbourInputsFor(String collaborationId, String specificationId) {
        // authorization is delegated to the network service
        return getNetworkService().getNeighbourInputsFor(collaborationId, specificationId);
    }

    @Override
    public XSPReply getNeighbourOutputsFor(String collaborationId, String specificationId) {
        // authorization is delegated to the network service
        return getNetworkService().getNeighbourOutputsFor(collaborationId, specificationId);
    }

    @Override
    public Collection<ArtifactSpecification> getKnownIOSpecifications() {
        Map<String, ArtifactSpecification> result = new HashMap<>();
        for (ArtifactSpecification specification : getInputSpecifications()) {
            result.put(specification.getIdentifier(), specification);
        }
        for (ArtifactSpecification specification : getOutputSpecifications()) {
            result.put(specification.getIdentifier(), specification);
        }
        for (ArtifactSpecification specification : getNetworkService().getKnownIOSpecifications()) {
            result.put(specification.getIdentifier(), specification);
        }
        return result.values();
    }

    @Override
    public XSPReply spawn(CollaborationSpecification specification) {
        // authorization is delegated to the network service
        return getNetworkService().spawn(specification);
    }

    @Override
    public XSPReply archive(String collaborationId) {
        // authorization is delegated to the network service
        return getNetworkService().archive(collaborationId);
    }

    @Override
    public XSPReply restart(String collaborationId) {
        // authorization is delegated to the network service
        return getNetworkService().restart(collaborationId);
    }

    @Override
    public XSPReply delete(String collaborationId) {
        // authorization is delegated to the network service
        return getNetworkService().delete(collaborationId);
    }

    @Override
    public XSPReply archive() {
        // authorization is delegated to the network service
        return getNetworkService().archive(getCollaborationIdentifier());
    }

    @Override
    public XSPReply delete() {
        // authorization is delegated to the network service
        return getNetworkService().delete(getCollaborationIdentifier());
    }

    @Override
    public Collection<CollaborationPatternDescriptor> getKnownPatterns() {
        Collection<CollaborationPatternDescriptor> result = new ArrayList<>();
        for (CollaborationPatternProvider provider : Register.getComponents(CollaborationPatternProvider.class)) {
            result.addAll(provider.getPatterns());
        }
        return result;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/archive")) {
            if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
            return XSPReplyUtils.toHttpResponse(archive(), null);
        } else if (request.getUri().equals(URI_API + "/delete")) {
            if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
            return XSPReplyUtils.toHttpResponse(delete(), null);
        } else if (request.getUri().startsWith(URI_API + "/manifest")) {
            return handleManifest(request);
        } else if (request.getUri().startsWith(URI_API + "/neighbours")) {
            return handleNeighbours(request);
        } else if (request.getUri().equals(URI_API + "/specifications")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (ArtifactSpecification specification : getKnownIOSpecifications()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(specification.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        } else if (request.getUri().equals(URI_API + "/patterns")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (CollaborationPatternDescriptor descriptor : getKnownPatterns()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(descriptor.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Handles a request for the manifest resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleManifest(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/manifest")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, manifest.serializedJSON());
        }
        String rest = request.getUri().substring(URI_API.length() + "/manifest".length());
        if (rest.startsWith("/inputs"))
            return handleManifestInputs(request);
        else if (rest.startsWith("/outputs"))
            return handleManifestOutputs(request);
        else if (rest.startsWith("/roles"))
            return handleManifestRoles(request);
        else if (rest.equals("/pattern")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, getCollaborationPattern().serializedJSON());
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Handles a request to the manifest/inputs resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleManifestInputs(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/manifest/inputs")) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    boolean first = true;
                    StringBuilder builder = new StringBuilder("[");
                    for (ArtifactSpecification specification : getInputSpecifications()) {
                        if (!first)
                            builder.append(", ");
                        first = false;
                        builder.append(specification.serializedJSON());
                    }
                    builder.append("]");
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
                }
                case HttpConstants.METHOD_PUT: {
                    String content = new String(request.getContent(), Files.CHARSET);
                    if (content.isEmpty())
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                    BufferedLogger logger = new BufferedLogger();
                    ASTNode root = JSONLDLoader.parseJSON(logger, content);
                    if (root == null)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
                    ArtifactSpecification specification = new ArtifactSpecification(root);
                    return XSPReplyUtils.toHttpResponse(addInputSpecification(specification), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        }
        String rest = request.getUri().substring(URI_API.length() + "/manifest/inputs".length() + 1);
        int index = rest.indexOf("/");
        String specId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
        if (specId.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        if (index < 0) {
            if (!HttpConstants.METHOD_DELETE.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected DELETE method");
            return XSPReplyUtils.toHttpResponse(removeInputSpecification(specId), null);
        }
        rest = rest.substring(index + 1);
        if (!rest.startsWith("artifacts"))
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return handleManifestInputArtifacts(request, specId, rest);
    }

    /**
     * Handles a request to the manifest/inputs/{inputId}/artifacts resource
     *
     * @param request The request
     * @param specId  The identifier of the input specification
     * @param rest    The rest of the URI
     * @return The response
     */
    private HttpResponse handleManifestInputArtifacts(HttpApiRequest request, String specId, String rest) {
        if (rest.equals("artifacts")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (Artifact artifact : getInputsFor(specId)) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(artifact.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        }
        rest = rest.substring("artifacts".length());
        if (!rest.startsWith("/"))
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        String artifactId = URIUtils.decodeComponent(rest.substring(1));
        if (artifactId.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);

        switch (request.getMethod()) {
            case HttpConstants.METHOD_PUT:
                return XSPReplyUtils.toHttpResponse(registerInput(specId, artifactId), null);
            case HttpConstants.METHOD_DELETE:
                return XSPReplyUtils.toHttpResponse(unregisterInput(specId, artifactId), null);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: PUT, DELETE");
    }

    /**
     * Handles a request to the manifest/outputs resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleManifestOutputs(HttpApiRequest request) {
        if (request.getUri().equals(URI_API + "/manifest/outputs")) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    boolean first = true;
                    StringBuilder builder = new StringBuilder("[");
                    for (ArtifactSpecification specification : getOutputSpecifications()) {
                        if (!first)
                            builder.append(", ");
                        first = false;
                        builder.append(specification.serializedJSON());
                    }
                    builder.append("]");
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
                }
                case HttpConstants.METHOD_PUT: {
                    String content = new String(request.getContent(), Files.CHARSET);
                    if (content.isEmpty())
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                    BufferedLogger logger = new BufferedLogger();
                    ASTNode root = JSONLDLoader.parseJSON(logger, content);
                    if (root == null)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
                    ArtifactSpecification specification = new ArtifactSpecification(root);
                    return XSPReplyUtils.toHttpResponse(addOutputSpecification(specification), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        }
        String rest = request.getUri().substring(URI_API.length() + "/manifest/inputs".length() + 1);
        int index = rest.indexOf("/");
        String specId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
        if (specId.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        if (index < 0) {
            if (!HttpConstants.METHOD_DELETE.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected DELETE method");
            return XSPReplyUtils.toHttpResponse(removeOutputSpecification(specId), null);
        }
        rest = rest.substring(index + 1);
        if (!rest.startsWith("artifacts"))
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return handleManifestOutputArtifacts(request, specId, rest);
    }

    /**
     * Handles a request to the manifest/outputs/{inputId}/artifacts resource
     *
     * @param request The request
     * @param specId  The identifier of the output specification
     * @param rest    The rest of the URI
     * @return The response
     */
    private HttpResponse handleManifestOutputArtifacts(HttpApiRequest request, String specId, String rest) {
        if (rest.equals("artifacts")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (Artifact artifact : getOutputsFor(specId)) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(artifact.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        }
        rest = rest.substring("artifacts".length());
        if (!rest.startsWith("/"))
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        String artifactId = URIUtils.decodeComponent(rest.substring(1));
        if (artifactId.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);

        switch (request.getMethod()) {
            case HttpConstants.METHOD_PUT:
                return XSPReplyUtils.toHttpResponse(registerOutput(specId, artifactId), null);
            case HttpConstants.METHOD_DELETE:
                return XSPReplyUtils.toHttpResponse(unregisterOutput(specId, artifactId), null);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: PUT, DELETE");
    }

    /**
     * Handles a request to the manifest/roles resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleManifestRoles(HttpApiRequest request) {
        String rest = request.getUri().substring(URI_API.length() + "/manifest/roles".length());
        if (rest.isEmpty()) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    boolean first = true;
                    StringBuilder builder = new StringBuilder("[");
                    for (PlatformRole role : getRoles()) {
                        if (!first)
                            builder.append(", ");
                        first = false;
                        builder.append(role.serializedJSON());
                    }
                    builder.append("]");
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
                }
                case HttpConstants.METHOD_PUT: {
                    String content = new String(request.getContent(), Files.CHARSET);
                    if (content.isEmpty())
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                    BufferedLogger logger = new BufferedLogger();
                    ASTNode root = JSONLDLoader.parseJSON(logger, content);
                    if (root == null)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
                    PlatformRoleBase role = new PlatformRoleBase(root);
                    return XSPReplyUtils.toHttpResponse(createRole(role.getIdentifier(), role.getName()), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        }
        if (!rest.startsWith("/"))
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        String roleId = URIUtils.decodeComponent(rest.substring(1));
        switch (request.getMethod()) {
            case HttpConstants.METHOD_DELETE:
                return XSPReplyUtils.toHttpResponse(removeRole(roleId), null);
            case HttpConstants.METHOD_PUT:
                return XSPReplyUtils.toHttpResponse(addRole(roleId), null);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: PUT, DELETE");
    }

    /**
     * Handles a request to the neighbours resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleNeighbours(HttpApiRequest request) {
        String rest = request.getUri().substring(URI_API.length() + "/neighbours".length());
        if (rest.isEmpty()) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    boolean first = true;
                    StringBuilder builder = new StringBuilder("[");
                    for (RemoteCollaboration collaboration : getNeighbours()) {
                        if (!first)
                            builder.append(", ");
                        first = false;
                        builder.append(collaboration.serializedJSON());
                    }
                    builder.append("]");
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
                }
                case HttpConstants.METHOD_PUT: {
                    String content = new String(request.getContent(), Files.CHARSET);
                    if (content.isEmpty())
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                    BufferedLogger logger = new BufferedLogger();
                    ASTNode root = JSONLDLoader.parseJSON(logger, content);
                    if (root == null)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
                    CollaborationSpecification specification = new CollaborationSpecification(root);
                    return XSPReplyUtils.toHttpResponse(spawn(specification), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        }
        rest = rest.substring(1);
        int index = rest.indexOf("/");
        String neighbourId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
        return handleNeighbour(request, neighbourId, index > 0 ? rest.substring(index + 1) : "");
    }

    /**
     * Handles a request to the neighbours/{neighbourId} resource
     *
     * @param request     The request
     * @param neighbourId The identifier of the neighbour
     * @param rest        The rest of the URI
     * @return The response
     */
    private HttpResponse handleNeighbour(HttpApiRequest request, String neighbourId, String rest) {
        if (rest.isEmpty()) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    RemoteCollaboration remoteCollaboration = getNeighbour(neighbourId);
                    if (remoteCollaboration == null)
                        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, remoteCollaboration.serializedJSON());
                }
                case HttpConstants.METHOD_DELETE: {
                    return XSPReplyUtils.toHttpResponse(delete(neighbourId), null);
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, DELETE");
        }
        switch (rest) {
            case "manifest": {
                if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                return XSPReplyUtils.toHttpResponse(getNeighbourManifest(neighbourId), null);
            }
            case "status": {
                if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, getNeighbourStatus(neighbourId).toString());
            }
            case "archive": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                return XSPReplyUtils.toHttpResponse(archive(neighbourId), null);
            }
            case "restart": {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                return XSPReplyUtils.toHttpResponse(restart(neighbourId), null);
            }
        }
        if (rest.startsWith("manifest/inputs/") && rest.endsWith("/artifacts")) {
            rest = rest.substring("manifest/inputs/".length(), rest.length() - "/artifacts".length());
            String specId = URIUtils.decodeComponent(rest);
            return XSPReplyUtils.toHttpResponse(getNeighbourInputsFor(neighbourId, specId), null);
        }
        if (rest.startsWith("manifest/outputs/") && rest.endsWith("/artifacts")) {
            rest = rest.substring("manifest/outputs/".length(), rest.length() - "/artifacts".length());
            String specId = URIUtils.decodeComponent(rest);
            return XSPReplyUtils.toHttpResponse(getNeighbourOutputsFor(neighbourId, specId), null);
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
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
