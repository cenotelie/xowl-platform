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
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.services.collaboration.*;

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
public class XOWLCollaborationLocalService implements CollaborationLocalService {
    /**
     * The file for storing the collaboration manifest
     */
    private final File fileManifest;
    /**
     * The manifest for this local collaboration
     */
    protected final CollaborationManifest manifest;

    /**
     * Initializes this service
     */
    public XOWLCollaborationLocalService() {
        ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
        Configuration configuration = configurationService.getConfigFor(CollaborationService.class.getCanonicalName());
        this.fileManifest = new File(System.getenv(Env.ROOT), configuration.get("manifest"));
        CollaborationManifest manifest = null;
        if (fileManifest.exists()) {
            try (Reader reader = IOUtils.getReader(fileManifest)) {
                ASTNode definition = JSONLDLoader.parseJSON(Logging.getDefault(), reader);
                if (definition != null)
                    manifest = new CollaborationManifest(definition);
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
            }
        }
        if (manifest == null)
            manifest = new CollaborationManifest(
                    UUID.randomUUID().toString(),
                    "Local collaboration",
                    CollaborationPatternFreeStyle.DESCRIPTOR
            );
        this.manifest = manifest;
    }

    /**
     * Serializes the manifest for this collaboration
     *
     * @return The protocol reply
     */
    private XSPReply serializeManifest() {
        try (OutputStream stream = new FileOutputStream(fileManifest)) {
            OutputStreamWriter writer = new OutputStreamWriter(stream, IOUtils.CHARSET);
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
        return XOWLCollaborationLocalService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Local Collaboration Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS_LOCAL;
    }

    @Override
    public String getCollaborationIdentifier() {
        return manifest.getIdentifier();
    }

    @Override
    public String getCollaborationName() {
        return manifest.getName();
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
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_ADD_INPUT_SPEC);
        if (!reply.isSuccess())
            return reply;

        manifest.addInputSpecification(specification);
        reply = serializeManifest();
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(specification);
    }

    @Override
    public XSPReply addOutputSpecification(ArtifactSpecification specification) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_ADD_OUTPUT_SPEC);
        if (!reply.isSuccess())
            return reply;

        manifest.addOutputSpecification(specification);
        reply = serializeManifest();
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(specification);
    }

    @Override
    public XSPReply removeInputSpecification(String specificationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_REMOVE_INPUT_SPEC);
        if (!reply.isSuccess())
            return reply;

        if (!manifest.removeInputSpecification(specificationId))
            return XSPReplyNotFound.instance();
        return serializeManifest();
    }

    @Override
    public XSPReply removeOutputSpecification(String specificationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_REMOVE_OUTPUT_SPEC);
        if (!reply.isSuccess())
            return reply;

        if (!manifest.removeOutputSpecification(specificationId))
            return XSPReplyNotFound.instance();
        return serializeManifest();
    }

    @Override
    public Collection<Artifact> getInputsFor(String specificationId) {
        ArtifactStorageService storageService = Register.getComponent(ArtifactStorageService.class);
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
    public Collection<Artifact> getOutputsFor(String specificationId) {
        ArtifactStorageService storageService = Register.getComponent(ArtifactStorageService.class);
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
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_REGISTER_INPUT);
        if (!reply.isSuccess())
            return reply;

        manifest.addInputArtifact(specificationId, artifactId);
        return serializeManifest();
    }

    @Override
    public XSPReply unregisterInput(String specificationId, String artifactId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_UNREGISTER_INPUT);
        if (!reply.isSuccess())
            return reply;

        manifest.removeInputArtifact(specificationId, artifactId);
        return serializeManifest();
    }

    @Override
    public XSPReply registerOutput(String specificationId, String artifactId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_REGISTER_OUTPUT);
        if (!reply.isSuccess())
            return reply;

        manifest.addOutputArtifact(specificationId, artifactId);
        return serializeManifest();
    }

    @Override
    public XSPReply unregisterOutput(String specificationId, String artifactId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_UNREGISTER_OUTPUT);
        if (!reply.isSuccess())
            return reply;

        manifest.removeOutputArtifact(specificationId, artifactId);
        return serializeManifest();
    }

    @Override
    public Collection<PlatformRole> getRoles() {
        return manifest.getRoles();
    }

    @Override
    public XSPReply createRole(String identifier, String name) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_ADD_ROLE);
        if (!reply.isSuccess())
            return reply;

        reply = securityService.getRealm().createRole(identifier, name);
        if (!reply.isSuccess())
            return reply;
        PlatformRole role = ((XSPReplyResult<PlatformRole>) reply).getData();
        manifest.addRole(role);
        reply = serializeManifest();
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(role);
    }

    @Override
    public XSPReply addRole(String roleId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_ADD_ROLE);
        if (!reply.isSuccess())
            return reply;

        PlatformRole role = securityService.getRealm().getRole(roleId);
        if (role == null)
            return XSPReplyNotFound.instance();
        manifest.addRole(role);
        reply = serializeManifest();
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(role);
    }

    @Override
    public XSPReply removeRole(String roleId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_REMOVE_ROLE);
        if (!reply.isSuccess())
            return reply;

        if (!manifest.removeRole(roleId))
            return XSPReplyNotFound.instance();
        return serializeManifest();
    }

    @Override
    public CollaborationPatternDescriptor getCollaborationPattern() {
        return manifest.getCollaborationPattern();
    }
}
