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

package org.xowl.platform.connectors.doors9;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.connectors.doors9.impl.DOORS9ImportationJob;
import org.xowl.platform.connectors.doors9.impl.DOORS9UIContribution;
import org.xowl.platform.kernel.PlatformUtils;
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
import java.util.Collection;

/**
 * Implements a DOORS 9 importer
 *
 * @author Elie Soubiran
 */
public class DOORS9Importer extends Importer {
    @Override
    public String getIdentifier() {
        return DOORS9Importer.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - DOORS 9 Importer";
    }

    @Override
    protected String getWebWizardURI() {
        return DOORS9UIContribution.PREFIX + "/wizard.html";
    }

    @Override
    public ImporterConfiguration getConfiguration(String definition) {
        ASTNode root = PlatformUtils.parseJSON(Logging.getDefault(), definition);
        if (root == null)
            return null;
        return new DOORS9Configuration(root);
    }

    @Override
    public DocumentPreview getPreview(Document document, ImporterConfiguration configuration) {
        return null;
    }

    @Override
    public Job getImportJob(Document document, ImporterConfiguration configuration) {
        if (!(configuration instanceof DOORS9Configuration))
            return null;
        return new DOORS9ImportationJob(document.getIdentifier(), (DOORS9Configuration) configuration);
    }

    /**
     * Imports a document
     *
     * @param documentId The identifier of the document to import
     * @return The result
     */
    public static XSPReply doImport(String documentId, DOORS9Configuration configuration) {
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
            String json = Files.read(stream, Files.CHARSET);
            BufferedLogger logger = new BufferedLogger();
            ASTNode root = PlatformUtils.parseJSON(logger, json);
            if (root == null)
                return new XSPReplyFailure(logger.getErrorsAsString());
            DOORS9Context context = new DOORS9Context(new CachedNodes(), documentId, documentId);
            importProject(root.getChildren().get(0).getChildren().get(1), context);
            Collection<Quad> metadata = ConnectorServiceBase.buildMetadata(documentId, configuration.getFamily(), configuration.getSuperseded(), document.getName(), configuration.getVersion(), configuration.getArchetype(), DOORS9Importer.class.getCanonicalName());
            Artifact artifact = new ArtifactSimple(metadata, context.getQuads());
            XSPReply reply = storageService.store(artifact);
            if (!reply.isSuccess())
                return reply;
            importationService.drop(document);
            return new XSPReplyResult<>(artifact);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyFailure(exception.getMessage());
        }
    }

    /**
     * Imports a project
     *
     * @param project The AST node for the project
     * @param context The current context
     */
    private static void importProject(ASTNode project, DOORS9Context context) {
        String projectName;
        for (ASTNode member : project.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Name": {
                    projectName = IOUtils.unescape(member.getChildren().get(1).getValue());
                    projectName = projectName.substring(1, projectName.length() - 1);
                    break;
                }
                case "contents": {
                    importContents(member.getChildren().get(1), context);
                    break;
                }
            }
        }
    }

    /**
     * Imports a series of content objects
     *
     * @param contents The AST node for the content objects
     * @param context  The current context
     */
    private static void importContents(ASTNode contents, DOORS9Context context) {
        for (ASTNode child : contents.getChildren()) {
            for (ASTNode member : child.getChildren()) {
                String key = IOUtils.unescape(member.getChildren().get(0).getValue());
                key = key.substring(1, key.length() - 1);
                switch (key) {
                    case "Folder": {
                        importFolder(member.getChildren().get(1), context);
                        break;
                    }
                    case "Formal": {
                        importFormalModule(member.getChildren().get(1), context);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Imports a folder
     *
     * @param folder  The AST node for the folder
     * @param context The current context
     */
    private static void importFolder(ASTNode folder, DOORS9Context context) {
        String folderName;
        for (ASTNode member : folder.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Name": {
                    folderName = IOUtils.unescape(member.getChildren().get(1).getValue());
                    folderName = folderName.substring(1, folderName.length() - 1);
                    break;
                }
                case "contents": {
                    importContents(member.getChildren().get(1), context);
                    break;
                }
            }
        }
    }

    /**
     * Imports a formal module
     *
     * @param module  The AST node for the formal module
     * @param context The current context
     */
    private static void importFormalModule(ASTNode module, DOORS9Context context) {
        String moduleName;
        for (ASTNode member : module.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Name": {
                    moduleName = IOUtils.unescape(member.getChildren().get(1).getValue());
                    moduleName = moduleName.substring(1, moduleName.length() - 1);
                    break;
                }
                case "contents": {
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        importRequirement(child, context);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Imports a requirement
     *
     * @param requirement The AST node for the requirement
     * @param context     The current context
     */
    private static void importRequirement(ASTNode requirement, DOORS9Context context) {
        IRINode reqIRI = null;
        for (ASTNode member : requirement.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Absolute Number": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    reqIRI = context.resolveEntity(text);
                    break;
                }
            }
        }

        context.addQuad(
                reqIRI,
                context.getIRI(Vocabulary.rdfType),
                context.getIRI("http://toto.com/Requirement"));

        for (ASTNode member : requirement.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Object Text": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/requirmentText"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;
                }
            }
        }
    }
}
