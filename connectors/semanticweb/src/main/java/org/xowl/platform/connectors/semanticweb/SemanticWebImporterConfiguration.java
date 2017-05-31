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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.services.importation.ImporterConfiguration;

/**
 * Represents the configuration of the Semantic Web importer
 *
 * @author Laurent Wouters
 */
public class SemanticWebImporterConfiguration extends ImporterConfiguration {
    /**
     * The expected syntax od the document to import
     */
    private final String syntax;

    /**
     * Gets the expected syntax od the document to import
     *
     * @return The expected syntax od the document to import
     */
    public String getSyntax() {
        return syntax;
    }

    /**
     * Initializes this configuration for a preview
     *
     * @param name   The configuration's name
     * @param syntax The expected syntax od the document to import
     */
    public SemanticWebImporterConfiguration(String name, String syntax) {
        super(name, SemanticWebImporter.INSTANCE);
        this.syntax = syntax;
    }

    /**
     * Loads this configuration from a serialized definition
     *
     * @param definition The definition
     */
    public SemanticWebImporterConfiguration(ASTNode definition) {
        super(definition);
        String syntax = "";
        for (ASTNode pair : definition.getChildren()) {
            String key = TextUtils.unescape(pair.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "syntax": {
                    String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
                    value = value.substring(1, value.length() - 1);
                    syntax = value;
                    break;
                }
            }
        }
        this.syntax = syntax;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(SemanticWebImporterConfiguration.class.getName()));
        builder.append("\", ");
        serializedJsonBase(builder);
        builder.append(", \"syntax\": \"");
        builder.append(TextUtils.escapeStringJSON(syntax));
        builder.append("\"}");
        return builder.toString();
    }
}
