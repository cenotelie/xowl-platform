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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.security.OwnedResourceBase;

import java.util.UUID;

/**
 * Represents the basic configuration for an importer
 *
 * @author Laurent Wouters
 */
public class ImporterConfiguration extends OwnedResourceBase {
    /**
     * The base URI for importer configurations
     */
    private static final String URI = "http://xowl.org/platform/services/importation/ImporterConfiguration#";

    /**
     * The identifier of the parent importer
     */
    protected final String importer;

    /**
     * Initializes this configuration
     *
     * @param name     The name of this configuration
     * @param importer The parent importer
     */
    public ImporterConfiguration(String name, Importer importer) {
        super(URI + UUID.randomUUID().toString(), name);
        this.importer = importer.getIdentifier();
    }

    /**
     * Initializes this configuration
     *
     * @param definition The definition of this configuration
     */
    public ImporterConfiguration(ASTNode definition) {
        super(definition);
        String importer = "";
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("importer".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                importer = value.substring(1, value.length() - 1);
            }
        }
        this.importer = importer;
    }

    /**
     * Gets the storage identifier of this document
     *
     * @return The storage identifier of this document
     */
    public String getStorageId() {
        return identifier.substring(URI.length());
    }

    /**
     * Gets the identifier of the parent importer
     *
     * @return The identifier of the parent importer
     */
    public String getImporter() {
        return importer;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(ImporterConfiguration.class.getCanonicalName()));
        builder.append("\", ");
        serializedJsonBase(builder);
        builder.append("}");
        return builder.toString();
    }

    @Override
    protected void serializedJsonBase(StringBuilder builder) {
        super.serializedJsonBase(builder);
        builder.append("\", \"importer\": \"");
        builder.append(TextUtils.escapeStringJSON(importer));
        builder.append("\"");
    }
}
