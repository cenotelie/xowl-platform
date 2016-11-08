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
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.services.connection.ConnectorServiceBase;
import org.xowl.platform.services.importation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implements an importer for Semantic Web datasets
 *
 * @author Laurent Wouters
 */
public class SemanticWebImporter extends Importer {
    @Override
    public String getIdentifier() {
        return SemanticWebImporter.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Semantic Web Importer";
    }

    @Override
    protected String getWebWizardURI() {
        return SemanticWebUIContribution.PREFIX + "/wizard.html";
    }

    @Override
    public ImporterConfiguration getConfiguration(String definition) {
        ASTNode root = JSONLDLoader.parseJSON(Logging.getDefault(), definition);
        if (root == null)
            return null;
        return new SemanticWebImporterConfiguration(root);
    }

    @Override
    public DocumentPreview getPreview(Document document, ImporterConfiguration configuration) {
        ImportationService service = ServiceUtils.getService(ImportationService.class);
        if (service == null)
            return null;
        if (!(configuration instanceof SemanticWebImporterConfiguration))
            return null;
        SemanticWebImporterConfiguration swConfig = (SemanticWebImporterConfiguration) configuration;
        try (InputStream stream = service.getStreamFor(document)) {
            InputStreamReader reader = new InputStreamReader(stream, Files.CHARSET);
            // TODO: complete this
            return null;
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return null;
        }
    }

    @Override
    public Job getImportJob(Document document, ImporterConfiguration configuration) {
        if (!(configuration instanceof SemanticWebImporterConfiguration))
            return null;
        return new SemanticWebImportJob(document.getIdentifier(), (SemanticWebImporterConfiguration) configuration);
    }

    /**
     * Imports a document
     *
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importation
     * @return The result
     */
    public static XSPReply doImport(String documentId, SemanticWebImporterConfiguration configuration) {
        ImportationService importationService = ServiceUtils.getService(ImportationService.class);
        if (importationService == null)
            return XSPReplyServiceUnavailable.instance();
        ArtifactStorageService storageService = ServiceUtils.getService(ArtifactStorageService.class);
        if (storageService == null)
            return XSPReplyServiceUnavailable.instance();

        Document document = importationService.getDocument(documentId);
        if (document == null)
            return XSPReplyNotFound.instance();

        try (InputStream stream = importationService.getStreamFor(document)) {
            InputStreamReader reader = new InputStreamReader(stream, Files.CHARSET);
            // TODO: complete this
            Collection<Quad> quads = new ArrayList<>();
            Collection<Quad> metadata = ConnectorServiceBase.buildMetadata(
                    documentId,
                    configuration.getFamily(),
                    configuration.getSuperseded(),
                    document.getName(),
                    configuration.getVersion(),
                    configuration.getArchetype(),
                    SemanticWebImporter.class.getCanonicalName());
            Artifact artifact = new ArtifactSimple(metadata, quads);
            XSPReply reply = storageService.store(artifact);
            if (!reply.isSuccess())
                return reply;
            return new XSPReplyResult<>(artifact.getIdentifier());
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyFailure(exception.getMessage());
        }
    }
}
