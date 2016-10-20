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
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Serializable;

/**
 * Represents the basic configuration for an importer
 *
 * @author Laurent Wouters
 */
public class ImporterConfiguration implements Serializable {
    /**
     * The base URI of the artifact family
     */
    private final String family;
    /**
     * The URI of the superseded artifacts
     */
    private final String[] superseded;
    /**
     * The version number of the artifact
     */
    private final String version;
    /**
     * The artifact archetype
     */
    private final String archetype;

    /**
     * Initializes this configuration
     *
     * @param family     The base URI of the artifact family
     * @param superseded The URI of the superseded artifacts
     * @param version    The version number of the artifact
     * @param archetype  The artifact archetype
     */
    public ImporterConfiguration(String family, String[] superseded, String version, String archetype) {
        this.family = family;
        this.superseded = superseded;
        this.version = version;
        this.archetype = archetype;
    }

    /**
     * Initializes this configuration
     *
     * @param definition The definition of this configuration
     */
    public ImporterConfiguration(ASTNode definition) {
        String tFamily = "";
        String[] tSuperseded = new String[0];
        String tVersion = "";
        String tArchetype = "";

        for (ASTNode member : definition.getChildren()) {
            String head = IOUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("family".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                tFamily = value.substring(1, value.length() - 1);
            } else if ("version".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                tVersion = value.substring(1, value.length() - 1);
            } else if ("archetype".equals(head)) {
                String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                tArchetype = value.substring(1, value.length() - 1);
            } else if ("superseded".equals(head)) {
                ASTNode nodeSuperseded = member.getChildren().get(1);
                tSuperseded = new String[nodeSuperseded.getChildren().size()];
                for (int i = 0; i != tSuperseded.length; i++) {
                    String value = IOUtils.unescape(nodeSuperseded.getChildren().get(i).getValue());
                    tSuperseded[i] = value.substring(1, value.length() - 1);
                }
            }
        }

        this.family = tFamily;
        this.superseded = tSuperseded;
        this.version = tVersion;
        this.archetype = tArchetype;
    }

    /**
     * Gets the base URI of the artifact family
     *
     * @return The base URI of the artifact family
     */
    public String getFamily() {
        return family;
    }

    /**
     * Gets the URI of the superseded artifacts
     *
     * @return The URI of the superseded artifacts
     */
    public String[] getSuperseded() {
        return superseded;
    }

    /**
     * Gets the version number of the artifact
     *
     * @return The version number of the artifact
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the artifact archetype
     *
     * @return The artifact archetype
     */
    public String getArchetype() {
        return archetype;
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(IOUtils.escapeStringJSON(ImporterConfiguration.class.getCanonicalName()));
        builder.append("\", ");
        serializeJSON(builder);
        builder.append("}");
        return builder.toString();
    }

    /**
     * Serialize the base data in JSON
     *
     * @param builder The string builder to use
     */
    protected void serializeJSON(StringBuilder builder) {
        builder.append("\"family\": \"");
        builder.append(IOUtils.escapeStringJSON(family));
        builder.append("\", \"superseded\": [");
        for (int i = 0; i != superseded.length; i++) {
            if (i != 0)
                builder.append(", ");
            builder.append("\"");
            builder.append(superseded[i]);
            builder.append("\"");
        }
        builder.append("], \"version\": \"");
        builder.append(IOUtils.escapeStringJSON(version));
        builder.append("\", \"archetype\": \"");
        builder.append(IOUtils.escapeStringJSON(archetype));
        builder.append("\"");
    }
}
