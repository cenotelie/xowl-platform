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

package org.xowl.platform.connectors.csv;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyException;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.BaseStore;
import org.xowl.infra.store.storage.StoreFactory;
import org.xowl.infra.utils.AutoReader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformHttp;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a CSV importer
 *
 * @author Laurent Wouters
 */
public class CSVImporter extends Importer {
    /**
     * The singleton instance of the importer
     */
    public static final Importer INSTANCE = new CSVImporter();

    /**
     * Initializes this importer
     */
    private CSVImporter() {
    }

    @Override
    public String getIdentifier() {
        return CSVImporter.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "CSV Importer";
    }

    @Override
    protected String getWebWizardURI() {
        return PlatformHttp.getUriPrefixWeb() + "/contributions/importers/csv/wizard.html";
    }

    @Override
    public ImporterConfiguration getConfiguration(ASTNode definition) {
        return new CSVConfiguration(definition);
    }

    @Override
    public XSPReply doGetPreview(String documentId, ImporterConfiguration configuration) {
        ImportationService service = Register.getComponent(ImportationService.class);
        if (service == null)
            return XSPReplyServiceUnavailable.instance();
        CSVConfiguration csvConfiguration = (CSVConfiguration) configuration;
        XSPReply reply = service.getStreamFor(documentId);
        if (!reply.isSuccess())
            return reply;
        try (InputStream stream = ((XSPReplyResult<InputStream>) reply).getData()) {
            InputStreamReader reader = new InputStreamReader(stream, IOUtils.CHARSET);
            CSVParser parser = new CSVParser(reader, csvConfiguration.getSeparator(), csvConfiguration.getTextMarker());
            Iterator<Iterator<String>> content = parser.parse();
            final List<List<String>> data = new ArrayList<>();
            int count = 0;
            while (content.hasNext() && count < csvConfiguration.getRowCount()) {
                Iterator<String> row = content.next();
                List<String> rowData = new ArrayList<>();
                while (row.hasNext())
                    rowData.add(row.next());
                data.add(rowData);
                count++;
            }
            DocumentPreview preview = new DocumentPreview() {
                @Override
                public String serializedString() {
                    return serializedJSON();
                }

                @Override
                public String serializedJSON() {
                    StringBuilder builder = new StringBuilder("{\"rows\": [");
                    for (int i = 0; i != data.size(); i++) {
                        if (i != 0)
                            builder.append(", ");
                        List<String> row = data.get(i);
                        builder.append("{\"cells\": [");
                        for (int j = 0; j != row.size(); j++) {
                            if (j != 0)
                                builder.append(", ");
                            builder.append("\"");
                            builder.append(TextUtils.escapeStringJSON(row.get(j)));
                            builder.append("\"");
                        }
                        builder.append("]}");
                    }
                    builder.append("]}");
                    return builder.toString();
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
        return new XSPReplyResult<>(new CSVImportationJob(documentId, (CSVConfiguration) configuration, metadata));
    }

    /**
     * Imports a document
     *
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importation
     * @param metadata      The metadata for the artifact to produce
     * @return The result
     */
    public static XSPReply doImport(String documentId, CSVConfiguration configuration, Artifact metadata) {
        ImportationService importationService = Register.getComponent(ImportationService.class);
        if (importationService == null)
            return XSPReplyServiceUnavailable.instance();
        CSVImporter importer = (CSVImporter) importationService.getImporter(CSVImporter.class.getCanonicalName());
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
            CSVParser parser = new CSVParser(new AutoReader(stream), configuration.getSeparator(), configuration.getTextMarker());
            Iterator<Iterator<String>> content = parser.parse();
            BaseStore store = StoreFactory.create().inMemory().make();
            CSVImportationContext context = new CSVImportationContext(Character.toString(configuration.getTextMarker()), store, artifactId, artifactId);
            configuration.getMapping().apply(content, context, configuration.getSkipFirstRow());
            Collection<Quad> quads = context.getQuads();
            Artifact artifact = new ArtifactSimple(metadata, artifactId, CSVImporter.class.getCanonicalName(), quads);
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
