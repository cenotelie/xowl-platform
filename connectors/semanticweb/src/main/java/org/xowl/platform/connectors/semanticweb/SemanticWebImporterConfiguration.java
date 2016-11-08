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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.services.importation.ImporterConfiguration;

/**
 * Represents the configuration of the Semantic Web importer
 *
 * @author Laurent Wouters
 */
public class SemanticWebImporterConfiguration extends ImporterConfiguration {
    /**
     * Initializes this configuration for a preview
     */
    public SemanticWebImporterConfiguration() {
        super("", new String[0], null, null);
    }

    /**
     * Initializes this configuration for an importation
     *
     * @param family     The base URI of the artifact family
     * @param superseded The URI of the superseded artifacts
     * @param version    The version number of the artifact
     * @param archetype  The artifact archetype
     */
    public SemanticWebImporterConfiguration(String family, String[] superseded, String version, String archetype) {
        super(family, superseded, version, archetype);
    }

    /**
     * Loads this configuration from a serialized definition
     *
     * @param definition The definition
     */
    public SemanticWebImporterConfiguration(ASTNode definition) {
        super(definition);
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(SemanticWebImporterConfiguration.class.getName()));
        builder.append("\", ");
        serializeJSON(builder);
        builder.append("}");
        return builder.toString();
    }
}
