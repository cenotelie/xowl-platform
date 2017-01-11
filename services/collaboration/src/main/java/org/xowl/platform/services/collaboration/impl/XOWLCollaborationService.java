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
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.collaboration.*;
import org.xowl.platform.services.collaboration.network.CollaborationNetworkService;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

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
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLCollaborationService.class, "/org/xowl/platform/services/collaboration/api_collaboration.raml", "Collaboration Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLCollaborationService.class, "/org/xowl/platform/services/collaboration/api_collaboration.html", "Collaboration Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The file for storing the collaboration manifest
     */
    private final File fileManifest;
    /**
     * The manifest for this local collaboration
     */
    private final CollaborationManifest manifest;
    /**
     * The collaboration network service
     */
    private CollaborationNetworkService networkService;

    /**
     * Initializes this service
     */
    public XOWLCollaborationService() {
        ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
        Configuration configuration = configurationService.getConfigFor(CollaborationService.class.getCanonicalName());
        this.fileManifest = new File(System.getenv(Env.ROOT), configuration.get("manifest"));
        CollaborationManifest manifest = null;
        if (fileManifest.exists()) {
            try (InputStream stream = new FileInputStream(fileManifest)) {
                String content = Files.read(stream, Files.CHARSET);
                ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), content);
                if (definition != null)
                    manifest = new CollaborationManifest(definition);
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
            }
        }
        if (manifest == null)
            manifest = new CollaborationManifest(
                    CollaborationManifest.class.getCanonicalName() + "." + UUID.randomUUID().toString(),
                    "Local collaboration",
                    CollaborationPatternFreeStyle.INSTANCE
            );
        this.manifest = manifest;
    }

    /**
     * Resolves the current collaboration network service
     *
     * @return The collaboration network service
     */
    private synchronized CollaborationNetworkService getNetworkService() {
        if (networkService == null) {
            ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
            Configuration configuration = configurationService.getConfigFor(CollaborationService.class.getCanonicalName());
            networkService = ServiceUtils.instantiate(configuration.get("network", "service"));
        }
        return networkService;
    }

    /**
     * Serializes the manifest for this collaboration
     *
     * @return The protocol reply
     */
    private XSPReply serializeManifest() {
        try (OutputStream stream = new FileOutputStream(fileManifest)) {
            OutputStreamWriter writer = new OutputStreamWriter(stream, Files.CHARSET);
            writer.write(manifest.serializedJSON());
            writer.flush();
            writer.close();
            return XSPReplySuccess.instance();
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
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
    public XSPReply archive() {
        return getNetworkService().archive(manifest.getIdentifier());
    }

    @Override
    public XSPReply delete() {
        return getNetworkService().delete(manifest.getIdentifier());
    }

    @Override
    public Collection<ArtifactSpecification> getInputSpecifications() {
        return manifest.getInputSpecifications();
    }

    @Override
    public Collection<ArtifactSpecification> getOutputSpecifications() {
        return manifest.getOutputSpecifications();
    }

    @Override
    public XSPReply addInputSpecification(ArtifactSpecification specification) {
        manifest.addInputSpecification(specification);
        return serializeManifest();
    }

    @Override
    public XSPReply addOutputSpecification(ArtifactSpecification specification) {
        manifest.addOutputSpecification(specification);
        return serializeManifest();
    }

    @Override
    public XSPReply removeInputSpecification(String specificationId) {
        if (!manifest.removeInputSpecification(specificationId))
            return XSPReplyNotFound.instance();
        return serializeManifest();
    }

    @Override
    public XSPReply removeOutputSpecification(String specificationId) {
        if (!manifest.removeOutputSpecification(specificationId))
            return XSPReplyNotFound.instance();
        return serializeManifest();
    }

    @Override
    public Collection<Artifact> getInputFor(String specificationId) {
        ArtifactStorageService storageService = ServiceUtils.getService(ArtifactStorageService.class);
        if (storageService == null)
            return Collections.emptyList();
        Collection<Artifact> artifacts = new ArrayList<>();
        for (String artifactId : manifest.getArtifactsForInput(specificationId)) {
            XSPReply reply = storageService.retrieve(artifactId);
            if (reply.isSuccess())
                artifacts.add(((XSPReplyResult<Artifact>) reply).getData());
        }
        return artifacts;
    }

    @Override
    public Collection<Artifact> getOutputFor(String specificationId) {
        ArtifactStorageService storageService = ServiceUtils.getService(ArtifactStorageService.class);
        if (storageService == null)
            return Collections.emptyList();
        Collection<Artifact> artifacts = new ArrayList<>();
        for (String artifactId : manifest.getArtifactsForOutput(specificationId)) {
            XSPReply reply = storageService.retrieve(artifactId);
            if (reply.isSuccess())
                artifacts.add(((XSPReplyResult<Artifact>) reply).getData());
        }
        return artifacts;
    }

    @Override
    public XSPReply registerInput(String specificationId, String artifactId) {
        manifest.addInputArtifact(specificationId, artifactId);
        return serializeManifest();
    }

    @Override
    public XSPReply registerOutput(String specificationId, String artifactId) {
        manifest.addOutputArtifact(specificationId, artifactId);
        return serializeManifest();
    }

    @Override
    public Collection<PlatformRole> getRoles() {
        return manifest.getRoles();
    }

    @Override
    public XSPReply addRole(String name) {
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.getRealm().createRole(
                PlatformRole.class.getCanonicalName() + "." + UUID.randomUUID().toString(),
                name
        );
        if (!reply.isSuccess())
            return reply;
        PlatformRole role = ((XSPReplyResult<PlatformRole>) reply).getData();
        manifest.addRole(role);
        return serializeManifest();
    }

    @Override
    public XSPReply removeRole(String identifier) {
        manifest.removeRole(identifier);
        return serializeManifest();
    }

    @Override
    public CollaborationPattern getCollaborationPattern() {
        return manifest.getCollaborationPattern();
    }

    @Override
    public Collection<RemoteCollaboration> getNeighbours() {
        return getNetworkService().getCollaborations();
    }

    @Override
    public XSPReply spawn(CollaborationSpecification specification) {
        return getNetworkService().spawn(specification);
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
