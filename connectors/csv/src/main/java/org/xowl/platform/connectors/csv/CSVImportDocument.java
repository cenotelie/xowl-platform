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

import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Serializable;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.BaseStore;
import org.xowl.infra.store.storage.StoreFactory;
import org.xowl.infra.utils.Files;
import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.artifacts.ArtifactBase;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a document to be imported
 *
 * @author Laurent Wouters
 */
public class CSVImportDocument implements Identifiable, Serializable {
    /**
     * Number of lines to return when the first lines of a document are requested
     */
    private static final int FIRST_LINES_COUNT = 10;

    /**
     * The document's identifier
     */
    private final String identifier;
    /**
     * The document's name
     */
    private final String name;
    /**
     * The document's content
     */
    private final byte[] content;

    /**
     * Initializes this document
     *
     * @param name    The document's name
     * @param content The document's content
     */
    public CSVImportDocument(String name, byte[] content) {
        this.identifier = ArtifactBase.newArtifactID(KernelSchema.GRAPH_ARTIFACTS);
        this.name = name;
        this.content = content;
    }

    /**
     * Maps this document to quads
     *
     * @param mapping      The mapping to use for the import
     * @param separator    The character that separates values in rows
     * @param textMarker   The character that marks the beginning and end of raw text
     * @param skipFirstRow Whether to skip the first row
     * @return The artifact
     */
    public Collection<Quad> map(CSVImportMapping mapping, char separator, char textMarker, boolean skipFirstRow) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(content);
        InputStreamReader reader = new InputStreamReader(byteStream, Files.CHARSET);
        CSVParser parser = new CSVParser(reader, separator, textMarker);
        Iterator<Iterator<String>> document = parser.parse();
        BaseStore store = StoreFactory.create().inMemory().make();
        CSVImportMappingContext context = new CSVImportMappingContext(Character.toString(textMarker), store, identifier, identifier);
        mapping.apply(document, context, skipFirstRow);
        return context.getQuads();
    }

    /**
     * Gets the first lines (serialized in JSON) of this document
     *
     * @param separator  The character that separates values in rows
     * @param textMarker The character that marks the beginning and end of raw text
     * @return The first lines of this document
     */
    public Serializable getFirstLines(char separator, char textMarker) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(content);
        InputStreamReader reader = new InputStreamReader(byteStream, Files.CHARSET);
        CSVParser parser = new CSVParser(reader, separator, textMarker);
        Iterator<Iterator<String>> document = parser.parse();

        final List<List<String>> data = new ArrayList<>();
        int rowCount = 0;
        while (document.hasNext() && rowCount < FIRST_LINES_COUNT) {
            Iterator<String> row = document.next();
            List<String> rowData = new ArrayList<>();
            while (row.hasNext())
                rowData.add(row.next());
            data.add(rowData);
            rowCount++;
        }
        return new Serializable() {
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
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"identifier\": \"" + IOUtils.escapeStringJSON(identifier) +
                "\", \"name\":\"" + IOUtils.escapeStringJSON(name) +
                "\"}";
    }
}
