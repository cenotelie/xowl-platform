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
        ASTNode root = IOUtils.parseJSON(Logging.getDefault(), definition);
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
            ASTNode root = IOUtils.parseJSON(logger, json);
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
            return new XSPReplyResult<>(artifact.getIdentifier());
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
        String projectName = "";
        for (ASTNode member : project.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Name": {
                    projectName = IOUtils.unescape(member.getChildren().get(1).getValue());
                    projectName = projectName.substring(1, projectName.length() - 1);
                    break;
                }
            }
        }
        for (ASTNode member : project.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "contents": {
                    importContents(member.getChildren().get(1), context, "/" + projectName);
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
    private static void importContents(ASTNode contents, DOORS9Context context, String path) {
        for (ASTNode child : contents.getChildren()) {
            for (ASTNode member : child.getChildren()) {
                String key = IOUtils.unescape(member.getChildren().get(0).getValue());
                key = key.substring(1, key.length() - 1);
                switch (key) {
                    case "Folder": {
                        importFolder(member.getChildren().get(1), context, path);
                        break;
                    }
                    case "Formal": {
                        importFormalModule(member.getChildren().get(1), context, path);
                        break;
                    }
                    case "Link": {
                        importLinkModule(member.getChildren().get(1), context, path);
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
    private static void importFolder(ASTNode folder, DOORS9Context context, String path) {
        String folderName = "";
        for (ASTNode member : folder.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Name": {
                    folderName = IOUtils.unescape(member.getChildren().get(1).getValue());
                    folderName = folderName.substring(1, folderName.length() - 1);
                    break;
                }
            }
        }
        for (ASTNode member : folder.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "contents": {
                    importContents(member.getChildren().get(1), context, path + "/" + folderName);
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
    private static void importFormalModule(ASTNode module, DOORS9Context context, String path) {
        String moduleName = "";
        IRINode modIRI = null;
        for (ASTNode member : module.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Name": {
                    moduleName = IOUtils.unescape(member.getChildren().get(1).getValue());
                    moduleName = moduleName.substring(1, moduleName.length() - 1);
                    modIRI = context.resolveEntity(path + "/" + moduleName);

                    break;
                }
            }
        }
        context.addQuad(
                modIRI,
                context.getIRI(Vocabulary.rdfType),
                context.getIRI("http://toto.com/FormalModule"));

        for (ASTNode member : module.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "contents": {
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        importRequirement(child, context, path + "/" + moduleName);
                    }
                    break;
                }
                case "Created By": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/createdBy"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;
                }
                case "Created On": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/createdBy"),
                            context.getLiteral(text, Vocabulary.xsdDate));
                    break;

                }
                case "Last Modified By": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/lastModifiedBy"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "Last Modified On": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/lastModifiedOn"),
                            context.getLiteral(text, Vocabulary.xsdDate));
                    break;

                }
                case "URB ID Prefix - Requirement": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/urbPrefix"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "PRJ Project Baseline": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/prjBaseline"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
            }
        }
    }

    /**
     * Imports a link module
     *
     * @param module  The AST node for the formal module
     * @param context The current context
     */
    private static void importLinkModule(ASTNode module, DOORS9Context context, String path) {
        String moduleName = "";
        IRINode modIRI = null;
        for (ASTNode member : module.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Name": {
                    moduleName = IOUtils.unescape(member.getChildren().get(1).getValue());
                    moduleName = moduleName.substring(1, moduleName.length() - 1);
                    modIRI = context.resolveEntity(path + "/" + moduleName);

                    break;
                }
            }
        }
        context.addQuad(
                modIRI,
                context.getIRI(Vocabulary.rdfType),
                context.getIRI("http://toto.com/LinkModule"));

        for (ASTNode member : module.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "contents": {
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        importLink(child, context, moduleName, path + "/" + moduleName);
                    }
                    break;
                }
                case "Created By": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/createdBy"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;
                }
                case "Created On": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/createdBy"),
                            context.getLiteral(text, Vocabulary.xsdDate));
                    break;

                }
                case "Last Modified By": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/lastModifiedBy"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "Last Modified On": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            modIRI,
                            context.getIRI("http://toto.com/lastModifiedOn"),
                            context.getLiteral(text, Vocabulary.xsdDate));
                    break;

                }
            }
        }
    }

    /**
     * Imports a link
     *
     * @param requirement The AST node for the requirement
     * @param context     The current context
     */
    private static void importLink(ASTNode requirement, DOORS9Context context, String relation, String path) {
        IRINode reqIRI = null;
        for (ASTNode member : requirement.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Absolute Number": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    reqIRI = context.resolveEntity(path + "#" + text);
                    break;
                }
            }
        }

        context.addQuad(
                reqIRI,
                context.getIRI(Vocabulary.rdfType),
                context.getIRI("http://toto.com/Link"));

        context.addQuad(
                reqIRI,
                context.getIRI("http://toto.com/Relation"),
                context.getIRI("http://toto.com/" + relation));
        context.addQuad(
                reqIRI,
                context.getIRI("http://toto.com/inModule"),
                context.resolveEntity(path));

        for (ASTNode member : requirement.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Created By": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/createdBy"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;
                }
                case "Created On": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/createdBy"),
                            context.getLiteral(text, Vocabulary.xsdDate));
                    break;

                }
                case "Last Modified By": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/lastModifiedBy"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "Last Modified On": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/lastModifiedOn"),
                            context.getLiteral(text, Vocabulary.xsdDate));
                    break;

                }
                case "Source": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    IRINode sourceIRI = context.resolveEntity(text);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/source"),
                            sourceIRI);
                    break;

                }
                case "Target": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    IRINode targetIRI = context.resolveEntity(text);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/target"),
                            targetIRI);
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
    private static void importRequirement(ASTNode requirement, DOORS9Context context, String path) {
        IRINode reqIRI = null;
        for (ASTNode member : requirement.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "Absolute Number": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    reqIRI = context.resolveEntity(path + "#" + text);
                    break;
                }
            }
        }

        context.addQuad(
                reqIRI,
                context.getIRI(Vocabulary.rdfType),
                context.getIRI("http://toto.com/Requirement"));
        context.addQuad(
                reqIRI,
                context.getIRI("http://toto.com/inModule"),
                context.resolveEntity(path));

        for (ASTNode member : requirement.getChildren()) {
            String key = IOUtils.unescape(member.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {

                /********************************************************************/
                /*           REGULAR DOORS ATTRIBUTES                                */
                /********************************************************************/

                case "Object Text": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/requirmentText"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;
                }
                case "Created By": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/createdBy"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "Created On": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/createdOn"),
                            context.getLiteral(text, Vocabulary.xsdDate));
                    break;

                }
                case "Created Thru": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/createdThru"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "Last Modified By": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/lastModifiedBy"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "Last Modified On": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/lastModifiedOn"),
                            context.getLiteral(text, Vocabulary.xsdDate));
                    break;

                }
                /********************************************************************/
                /*           link references                                        */
                /********************************************************************/

                case "incomingLinks": {
                    ASTNode inLinks = member.getChildren().get(1);
                    for (ASTNode inLink : inLinks.getChildren()) {
                        String abs_name = "";
                        String type_link = "";
                        for (ASTNode pair : inLink.getChildren()) {
                            String attr = IOUtils.unescape(pair.getChildren().get(0).getValue());
                            attr = attr.substring(1, attr.length() - 1);
                            switch (attr) {
                                case "Type": {
                                    type_link = IOUtils.unescape(pair.getChildren().get(1).getValue());
                                    type_link = type_link.substring(1, type_link.length() - 1);
                                    break;
                                }
                                case "Source": {
                                    abs_name = IOUtils.unescape(pair.getChildren().get(1).getValue());
                                    abs_name = abs_name.substring(1, abs_name.length() - 1);
                                    break;
                                }
                            }
                        }
                        IRINode sourceIRI = context.resolveEntity(abs_name);
                        context.addQuad(
                                reqIRI,
                                context.getIRI("http://toto.com/Rev"+type_link),
                                sourceIRI);
                        // add a direct link to destination?
                    }
                    break;
                }
                case "outcomingLinks": {
                    ASTNode inLinks = member.getChildren().get(1);
                    for (ASTNode inLink : inLinks.getChildren()) {
                        String abs_name = "";
                        String type_link = "";
                        for (ASTNode pair : inLink.getChildren()) {
                            String attr = IOUtils.unescape(pair.getChildren().get(0).getValue());
                            attr = attr.substring(1, attr.length() - 1);
                            switch (attr) {
                                case "Type": {
                                    type_link = IOUtils.unescape(pair.getChildren().get(1).getValue());
                                    type_link = type_link.substring(1, type_link.length() - 1);
                                    break;
                                }
                                case "Target": {
                                    abs_name = IOUtils.unescape(pair.getChildren().get(1).getValue());
                                    abs_name = abs_name.substring(1, abs_name.length() - 1);
                                    break;
                                }
                            }
                        }
                        IRINode targetIRI = context.resolveEntity(abs_name);
                        context.addQuad(
                                reqIRI,
                                context.getIRI("http://toto.com/"+type_link),
                                targetIRI);
                        // add a direct link to source?
                    }
                    break;
                }
                /********************************************************************/
                /*           URBALIS ATTRIBUTES                                     */
                /********************************************************************/

                case "URB Review Status": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/urbReviewStatus"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "URB Category Type": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/urbCategoryType"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "URB Object Type": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/urbObjectType"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "URB Activity": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/urbActivity"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "URB Compliance Status": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/urbComplianceStatus"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                /********************************************************************/
                /*           TGS ATTRIBUTES                                         */
                /********************************************************************/

                case "TGS Category": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tgsCategory"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TGS Status": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tgsStatus"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TGS ID": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tgsID"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TGS PBS": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    String[] parts = text.split("\n");
                    for (int i = 0; i != parts.length; i++) {
                        context.addQuad(
                                reqIRI,
                                context.getIRI("http://toto.com/tgsPBS"),
                                context.getLiteral(parts[i], Vocabulary.xsdString));
                    }
                    break;
                }

                /********************************************************************/
                /*           TIS ATTRIBUTES                                         */
                /********************************************************************/

                case "TIS Object Identifier": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tisObjectIdentifier"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TIS ID": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tisID"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TIS Status": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tisStatus"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TIS Type": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tisStatus"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TIS Rationale": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tisRationale"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TIS Allocation": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tisAllocation"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TIS Assignment": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tisAssignment"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TIS Comment": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tisComment"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }


                /********************************************************************/
                /*           other ATTRIBUTES TO BE Discussed                       */
                /********************************************************************/

                case "PRJ Coverage Level": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/prjCoverageLevel"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "Section": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/section"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "InputModule": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/inputModule"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "TMP upstream ID": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    context.addQuad(
                            reqIRI,
                            context.getIRI("http://toto.com/tmpUpstreamID"),
                            context.getLiteral(text, Vocabulary.xsdString));
                    break;

                }
                case "OBS": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    String[] parts = text.split("\n");
                    for (int i = 0; i != parts.length; i++) {
                        context.addQuad(
                                reqIRI,
                                context.getIRI("http://toto.com/OBS"),
                                context.getLiteral(parts[i], Vocabulary.xsdString));
                    }
                    break;
                }
                case "ABS": {
                    String text = IOUtils.unescape(member.getChildren().get(1).getValue());
                    text = text.substring(1, text.length() - 1);
                    String[] parts = text.split("\n");
                    for (int i = 0; i != parts.length; i++) {
                        context.addQuad(
                                reqIRI,
                                context.getIRI("http://toto.com/ABS"),
                                context.getLiteral(parts[i], Vocabulary.xsdString));
                    }
                    break;
                }
            }
        }
    }


}
