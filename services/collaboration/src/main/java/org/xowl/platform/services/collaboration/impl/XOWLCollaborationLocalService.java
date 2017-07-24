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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSpecification;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.platform.PlatformRole;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.services.collaboration.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
        this.fileManifest = PlatformUtils.resolve(configuration.get("manifest"));
        CollaborationManifest manifest = null;
        if (fileManifest.exists()) {
            try (Reader reader = IOUtils.getReader(fileManifest)) {
                ASTNode definition = Json.parse(Logging.get(), reader);
                if (definition != null)
                    manifest = new CollaborationManifest(definition);
            } catch (IOException exception) {
                Logging.get().error(exception);
            }
        }
        if (manifest == null) {
            manifest = new CollaborationManifest(
                    UUID.randomUUID().toString(),
                    "Local collaboration",
                    CollaborationPatternFreeStyle.DESCRIPTOR
            );
        }
        if (!fileManifest.exists()) {
            try (Writer writer = IOUtils.getWriter(fileManifest)) {
                writer.write(manifest.serializedJSON());
                writer.flush();
            } catch (IOException exception) {
                Logging.get().error(exception);
            }
        }
        this.manifest = manifest;
    }

    /**
     * Serializes the manifest for this collaboration
     *
     * @return The protocol reply
     */
    private Reply serializeManifest() {
        try (Writer writer = IOUtils.getWriter(fileManifest)) {
            writer.write(manifest.serializedJSON());
            writer.flush();
            return ReplySuccess.instance();
        } catch (IOException exception) {
            Logging.get().error(exception);
            return new ReplyException(exception);
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
    public Reply addInputSpecification(ArtifactSpecification specification) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_ADD_INPUT_SPEC);
        if (!reply.isSuccess())
            return reply;

        manifest.addInputSpecification(specification);
        reply = serializeManifest();
        if (!reply.isSuccess())
            return reply;
        return new ReplyResult<>(specification);
    }

    @Override
    public Reply addOutputSpecification(ArtifactSpecification specification) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_ADD_OUTPUT_SPEC);
        if (!reply.isSuccess())
            return reply;

        manifest.addOutputSpecification(specification);
        reply = serializeManifest();
        if (!reply.isSuccess())
            return reply;
        return new ReplyResult<>(specification);
    }

    @Override
    public Reply removeInputSpecification(String specificationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_REMOVE_INPUT_SPEC);
        if (!reply.isSuccess())
            return reply;

        if (!manifest.removeInputSpecification(specificationId))
            return ReplyNotFound.instance();
        return serializeManifest();
    }

    @Override
    public Reply removeOutputSpecification(String specificationId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_REMOVE_OUTPUT_SPEC);
        if (!reply.isSuccess())
            return reply;

        if (!manifest.removeOutputSpecification(specificationId))
            return ReplyNotFound.instance();
        return serializeManifest();
    }

    @Override
    public Collection<Artifact> getInputsFor(String specificationId) {
        ArtifactStorageService storageService = Register.getComponent(ArtifactStorageService.class);
        if (storageService == null)
            return Collections.emptyList();
        Collection<Artifact> artifacts = new ArrayList<>();
        for (String artifactId : manifest.getArtifactsForInput(specificationId)) {
            Reply reply = storageService.retrieve(artifactId);
            if (reply.isSuccess())
                artifacts.add(((ReplyResult<Artifact>) reply).getData());
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
            Reply reply = storageService.retrieve(artifactId);
            if (reply.isSuccess())
                artifacts.add(((ReplyResult<Artifact>) reply).getData());
        }
        return artifacts;
    }

    @Override
    public Reply registerInput(String specificationId, String artifactId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_REGISTER_INPUT);
        if (!reply.isSuccess())
            return reply;

        manifest.addInputArtifact(specificationId, artifactId);
        return serializeManifest();
    }

    @Override
    public Reply unregisterInput(String specificationId, String artifactId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_UNREGISTER_INPUT);
        if (!reply.isSuccess())
            return reply;

        manifest.removeInputArtifact(specificationId, artifactId);
        return serializeManifest();
    }

    @Override
    public Reply registerOutput(String specificationId, String artifactId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_REGISTER_OUTPUT);
        if (!reply.isSuccess())
            return reply;

        manifest.addOutputArtifact(specificationId, artifactId);
        return serializeManifest();
    }

    @Override
    public Reply unregisterOutput(String specificationId, String artifactId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_UNREGISTER_OUTPUT);
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
    public Reply createRole(String identifier, String name) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_ADD_ROLE);
        if (!reply.isSuccess())
            return reply;

        reply = securityService.getRealm().createRole(identifier, name);
        if (!reply.isSuccess())
            return reply;
        PlatformRole role = ((ReplyResult<PlatformRole>) reply).getData();
        manifest.addRole(role);
        reply = serializeManifest();
        if (!reply.isSuccess())
            return reply;
        return new ReplyResult<>(role);
    }

    @Override
    public Reply addRole(String roleId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_ADD_ROLE);
        if (!reply.isSuccess())
            return reply;

        PlatformRole role = securityService.getRealm().getRole(roleId);
        if (role == null)
            return ReplyNotFound.instance();
        manifest.addRole(role);
        reply = serializeManifest();
        if (!reply.isSuccess())
            return reply;
        return new ReplyResult<>(role);
    }

    @Override
    public Reply removeRole(String roleId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_REMOVE_ROLE);
        if (!reply.isSuccess())
            return reply;

        if (!manifest.removeRole(roleId))
            return ReplyNotFound.instance();
        return serializeManifest();
    }

    @Override
    public CollaborationPatternDescriptor getCollaborationPattern() {
        return manifest.getCollaborationPattern();
    }
}
