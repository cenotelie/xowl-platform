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

package org.xowl.platform.services.importation;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.artifacts.ArtifactBase;

/**
 * Represents a document to be imported
 *
 * @author Laurent Wouters
 */
public class Document implements Identifiable, Serializable {
    /**
     * The document's identifier
     */
    private final String identifier;
    /**
     * The document's name
     */
    private final String name;
    /**
     * The file that stores this document
     */
    private final String fileName;

    /**
     * Initializes this document
     *
     * @param name The document's name
     */
    public Document(String name) {
        this.identifier = ArtifactBase.newArtifactID(KernelSchema.GRAPH_ARTIFACTS);
        this.name = name;
        this.fileName = identifier.substring(KernelSchema.GRAPH_ARTIFACTS.length() + 1);
    }

    /**
     * Initializes this document
     *
     * @param node The descriptor node to load from
     */
    public Document(ASTNode node) {
        String tIdentifier = "";
        String tName = "";
        String tFileName = "";
        for (ASTNode pair : node.getChildren()) {
            String key = TextUtils.unescape(pair.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
            value = value.substring(1, value.length() - 1);
            switch (key) {
                case "identifier":
                    tIdentifier = value;
                    break;
                case "name":
                    tName = value;
                    break;
                case "fileName":
                    tFileName = value;
                    break;
            }
        }
        this.identifier = tIdentifier;
        this.name = tName;
        this.fileName = tFileName;
    }

    /**
     * Gets the file that stores this document
     *
     * @return The file that stores this document
     */
    public String getFileName() {
        return fileName;
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
        return "{\"type\": \"" + TextUtils.escapeStringJSON(Document.class.getCanonicalName()) +
                "\", \"identifier\": \"" + TextUtils.escapeStringJSON(identifier) +
                "\", \"name\":\"" + TextUtils.escapeStringJSON(name) +
                "\", \"fileName\":\"" + TextUtils.escapeStringJSON(fileName) +
                "\"}";
    }
}
