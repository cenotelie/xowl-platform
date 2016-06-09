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
import org.xowl.platform.connectors.csv.impl.CSVParser;
import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactBase;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.services.connection.ConnectorServiceBase;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

/**
 * Represents a document to be imported
 */
public class ImportDocument implements Identifiable, Serializable {
    /**
     * The document's identifier
     */
    private final String identifier;
    /**
     * The document's name
     */
    private final String name;
    /**
     * The document's base family URI
     */
    private final String base;
    /**
     * URI of the superseded document, if any
     */
    private final String[] supersede;
    /**
     * The version of this document
     */
    private final String version;
    /**
     * The archetype for this document
     */
    private final String archetype;
    /**
     * The document's content
     */
    private final byte[] content;

    /**
     * Initializes this document
     *
     * @param name      The document's name
     * @param base      The document's base family URI
     * @param supersede URI of the superseded document, if any
     * @param version   The version of this document
     * @param archetype The archetype for this document
     * @param content   The document's content
     */
    public ImportDocument(String name, String base, String[] supersede, String version, String archetype, byte[] content) {
        this.identifier = ArtifactBase.newArtifactID(KernelSchema.GRAPH_ARTIFACTS);
        this.name = name;
        this.base = base;
        this.supersede = supersede;
        this.version = version;
        this.archetype = archetype;
        this.content = content;
    }

    /**
     * Builds the artifact from this document
     *
     * @param connector    The parent connector's identifier
     * @param mapping      The mapping to use for the import
     * @param separator    The character that separates values in rows
     * @param textMarker   The character that marks the beginning and end of raw text
     * @param skipFirstRow Whether to skip the first row
     * @return The artifact
     */
    public Artifact buildArtifact(String connector, ImportMapping mapping, char separator, char textMarker, boolean skipFirstRow) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(content);
        InputStreamReader reader = new InputStreamReader(byteStream, Files.CHARSET);
        CSVParser parser = new CSVParser(reader, separator, textMarker);
        Iterator<Iterator<String>> document = parser.parse();
        BaseStore store = StoreFactory.create().inMemory().make();
        ImportMappingContext context = new ImportMappingContext(Character.toString(textMarker), store, identifier, identifier);
        mapping.apply(document, context, skipFirstRow);
        Collection<Quad> metadata = ConnectorServiceBase.buildMetadata(identifier, base, supersede, name, version, archetype, connector);
        return new ArtifactSimple(metadata, context.getQuads());
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
        StringBuilder builder = new StringBuilder("{\"identifier\": \"");
        builder.append(IOUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\":\"");
        builder.append(IOUtils.escapeStringJSON(name));
        builder.append("\", \"base\": \"");
        builder.append(IOUtils.escapeStringJSON(base));
        builder.append("\", \"version\": \"");
        builder.append(IOUtils.escapeStringJSON(version));
        builder.append("\", \"archetype\": \"");
        builder.append(IOUtils.escapeStringJSON(archetype));
        builder.append("\", \"supersede\": [");
        for (int i = 0; i != supersede.length; i++) {
            if (i != 0)
                builder.append(", ");
            builder.append("\"");
            builder.append(IOUtils.escapeStringJSON(supersede[i]));
            builder.append("\"");
        }
        builder.append("]}");
        return builder.toString();
    }
}
