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

import fr.cenotelie.commons.utils.AutoReader;
import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.commons.utils.api.ReplyException;
import fr.cenotelie.commons.utils.api.ReplyResult;
import fr.cenotelie.commons.utils.csv.Csv;
import fr.cenotelie.commons.utils.csv.CsvDocument;
import fr.cenotelie.commons.utils.csv.CsvRow;
import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.BaseStore;
import org.xowl.infra.store.storage.StoreFactory;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactBase;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.events.EventService;
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
        return "xOWL - CSV Importer";
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
    public Reply getPreview(String documentId, ImporterConfiguration configuration) {
        ImportationService service = Register.getComponent(ImportationService.class);
        if (service == null)
            return ReplyServiceUnavailable.instance();
        CSVConfiguration csvConfiguration = (CSVConfiguration) configuration;
        Reply reply = service.getStreamFor(documentId);
        if (!reply.isSuccess())
            return reply;
        try (InputStream stream = ((ReplyResult<InputStream>) reply).getData()) {
            InputStreamReader reader = new InputStreamReader(stream, IOUtils.CHARSET);
            CsvDocument document = Csv.parse(reader, csvConfiguration.getSeparator(), csvConfiguration.getTextMarker());
            final List<List<String>> data = new ArrayList<>();
            int count = 0;
            while (document.hasNext() && count < csvConfiguration.getRowCount()) {
                CsvRow row = document.next();
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
            return new ReplyResult<>(preview);
        } catch (IOException exception) {
            Logging.get().error(exception);
            return new ReplyException(exception);
        }
    }

    @Override
    public Reply getImportJob(String documentId, ImporterConfiguration configuration, Artifact metadata) {
        return new ReplyResult<>(new CSVImportationJob(documentId, (CSVConfiguration) configuration, metadata));
    }

    /**
     * Imports a document
     *
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importation
     * @param metadata      The metadata for the artifact to produce
     * @return The result
     */
    public static Reply doImport(String documentId, CSVConfiguration configuration, Artifact metadata) {
        ImportationService importationService = Register.getComponent(ImportationService.class);
        if (importationService == null)
            return ReplyServiceUnavailable.instance();
        CSVImporter importer = (CSVImporter) importationService.getImporter(CSVImporter.class.getCanonicalName());
        if (importer == null)
            return ReplyServiceUnavailable.instance();
        ArtifactStorageService storageService = Register.getComponent(ArtifactStorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();

        Reply reply = importationService.getDocument(documentId);
        if (!reply.isSuccess())
            return reply;
        Document document = ((ReplyResult<Document>) reply).getData();
        reply = importationService.getStreamFor(documentId);
        if (!reply.isSuccess())
            return reply;
        String artifactId = ArtifactBase.newArtifactID();
        try (InputStream stream = ((ReplyResult<InputStream>) reply).getData()) {
            CsvDocument content = Csv.parse(new AutoReader(stream), configuration.getSeparator(), configuration.getTextMarker());
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
            return new ReplyResult<>(artifact.getIdentifier());
        } catch (IOException exception) {
            Logging.get().error(exception);
            return new ReplyException(exception);
        }
    }
}
