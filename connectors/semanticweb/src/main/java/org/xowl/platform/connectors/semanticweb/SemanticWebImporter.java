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

package org.xowl.platform.connectors.semanticweb;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyException;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.writers.NQuadsSerializer;
import org.xowl.infra.store.writers.RDFSerializer;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactBase;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.services.importation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;

/**
 * Implements an importer for Semantic Web datasets
 *
 * @author Laurent Wouters
 */
public class SemanticWebImporter extends Importer {
    /**
     * The singleton instance of the importer
     */
    public static final Importer INSTANCE = new SemanticWebImporter();

    /**
     * Initializes this importer
     */
    private SemanticWebImporter() {
    }

    @Override
    public String getIdentifier() {
        return SemanticWebImporter.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Semantic Web Importer";
    }

    @Override
    protected String getWebWizardURI() {
        return PlatformHttp.getUriPrefixWeb() + "/contributions/connectors/semanticweb/wizard.html";
    }

    @Override
    public ImporterConfiguration getConfiguration(ASTNode definition) {
        return new SemanticWebImporterConfiguration(definition);
    }

    @Override
    public XSPReply doGetPreview(String documentId, ImporterConfiguration configuration) {
        ImportationService service = Register.getComponent(ImportationService.class);
        if (service == null)
            return XSPReplyServiceUnavailable.instance();
        SemanticWebImporterConfiguration swConfig = (SemanticWebImporterConfiguration) configuration;
        XSPReply reply = service.getStreamFor(documentId);
        if (!reply.isSuccess())
            return reply;
        try (InputStream stream = ((XSPReplyResult<InputStream>) reply).getData()) {
            InputStreamReader reader = new InputStreamReader(stream, Files.CHARSET);
            SemanticWebLoader loader = new SemanticWebLoader();
            reply = loader.load(reader, documentId, swConfig.getSyntax());
            if (!reply.isSuccess())
                return reply;
            Collection<Quad> quads = ((XSPReplyResultCollection<Quad>) reply).getData();
            StringWriter writer = new StringWriter();
            RDFSerializer serializer = new NQuadsSerializer(writer);
            serializer.serialize(new BufferedLogger(), quads.iterator());
            final String result = writer.toString();
            DocumentPreview preview = new DocumentPreview() {
                @Override
                public String serializedString() {
                    return result;
                }

                @Override
                public String serializedJSON() {
                    return "{\"quads\": \"" + TextUtils.escapeStringJSON(result) + "\"}";
                }
            };
            return new XSPReplyResult<>(preview);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
    }

    @Override
    public XSPReply doGetImportJob(String documentId, ImporterConfiguration configuration, Artifact metadata) {
        return new XSPReplyResult<>(new SemanticWebImportJob(documentId, (SemanticWebImporterConfiguration) configuration, metadata));
    }

    /**
     * Imports a document
     *
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importation
     * @param metadata      The metadata for the artifact to produce
     * @return The result
     */
    public static XSPReply doImport(String documentId, SemanticWebImporterConfiguration configuration, Artifact metadata) {
        ImportationService importationService = Register.getComponent(ImportationService.class);
        if (importationService == null)
            return XSPReplyServiceUnavailable.instance();
        SemanticWebImporter importer = (SemanticWebImporter) importationService.getImporter(SemanticWebImporter.class.getCanonicalName());
        if (importer == null)
            return XSPReplyServiceUnavailable.instance();
        ArtifactStorageService storageService = Register.getComponent(ArtifactStorageService.class);
        if (storageService == null)
            return XSPReplyServiceUnavailable.instance();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(importer.actionImport);
        if (!reply.isSuccess())
            return reply;

        reply = importationService.getDocument(documentId);
        if (!reply.isSuccess())
            return reply;
        Document document = ((XSPReplyResult<Document>) reply).getData();
        reply = importationService.getStreamFor(documentId);
        if (!reply.isSuccess())
            return reply;
        String artifactId = ArtifactBase.newArtifactID();
        try (InputStream stream = ((XSPReplyResult<InputStream>) reply).getData()) {
            InputStreamReader reader = new InputStreamReader(stream, Files.CHARSET);
            SemanticWebLoader loader = new SemanticWebLoader();
            reply = loader.load(reader, artifactId, configuration.getSyntax());
            if (!reply.isSuccess())
                return reply;
            Collection<Quad> quads = ((XSPReplyResultCollection<Quad>) reply).getData();
            Artifact artifact = new ArtifactSimple(metadata, SemanticWebImporter.class.getCanonicalName(), quads);
            reply = storageService.store(artifact);
            if (!reply.isSuccess())
                return reply;
            EventService eventService = Register.getComponent(EventService.class);
            if (eventService != null)
                eventService.onEvent(new DocumentImportedEvent(document, artifact, importationService));
            return new XSPReplyResult<>(artifact.getIdentifier());
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
    }
}
