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
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.BaseStore;
import org.xowl.infra.store.storage.StoreFactory;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.services.connection.ConnectorServiceBase;
import org.xowl.platform.services.importation.Document;
import org.xowl.platform.services.importation.DocumentPreview;
import org.xowl.platform.services.importation.ImportationService;
import org.xowl.platform.services.importation.Importer;

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
public class CSVImporter implements Importer {
    @Override
    public String getIdentifier() {
        return CSVImporter.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - CSV Importer";
    }

    @Override
    public DocumentPreview getPreview(Document document, String configuration) {
        ImportationService service = ServiceUtils.getService(ImportationService.class);
        if (service == null)
            return null;
        ASTNode definition = PlatformUtils.parseJSON(Logging.getDefault(), configuration);
        if (definition == null)
            return null;
        CSVConfiguration csvConfiguration = new CSVConfiguration(definition);
        try (InputStream stream = service.getStreamFor(document)) {
            InputStreamReader reader = new InputStreamReader(stream, Files.CHARSET);
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
            return new DocumentPreview() {
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
                            builder.append(IOUtils.escapeStringJSON(row.get(j)));
                            builder.append("\"");
                        }
                        builder.append("]}");
                    }
                    builder.append("]}");
                    return builder.toString();
                }
            };
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return null;
        }
    }

    @Override
    public Job getImportJob(Document document, String configuration) {
        return null;
    }

    /**
     * Imports a document
     *
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importation
     * @return The result
     */
    public static XSPReply doImport(String documentId, CSVConfiguration configuration) {
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
            CSVParser parser = new CSVParser(reader, configuration.getSeparator(), configuration.getTextMarker());
            Iterator<Iterator<String>> content = parser.parse();
            BaseStore store = StoreFactory.create().inMemory().make();
            CSVImportationContext context = new CSVImportationContext(Character.toString(configuration.getTextMarker()), store, documentId, documentId);
            configuration.getMapping().apply(content, context, configuration.getSkipFirstRow());
            Collection<Quad> quads = context.getQuads();
            Collection<Quad> metadata = ConnectorServiceBase.buildMetadata(documentId, configuration.getFamily(), configuration.getSuperseded(), document.getName(), configuration.getVersion(), configuration.getArchetype(), CSVImporter.class.getCanonicalName());
            Artifact artifact = new ArtifactSimple(metadata, quads);
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
}
