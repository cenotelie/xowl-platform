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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.services.importation.ImporterConfiguration;

/**
 * Represents the configuration for the CSV importer
 *
 * @author Laurent Wouters
 */
public class CSVConfiguration extends ImporterConfiguration {
    /**
     * The default separator
     */
    private static final char DEFAULT_SEPARATOR = ',';
    /**
     * The default text marker
     */
    private static final char DEFAULT_TEXT_MARKER = '"';
    /**
     * The default number of rows for a preview
     */
    private static final int DEFAULT_ROW_COUNT = 4;

    /**
     * The cell separator
     */
    private final char separator;
    /**
     * The text delimiter
     */
    private final char textMarker;
    /**
     * For preview, the number or rows
     */
    private final int rowCount;
    /**
     * For import, the mapping
     */
    private final CSVMapping mapping;
    /**
     * For import, whether to skip the first row
     */
    private final boolean skipFirstRow;

    /**
     * Gets the cell separator
     *
     * @return The cell separator
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Gets the text delimiter
     *
     * @return The text delimiter
     */
    public char getTextMarker() {
        return textMarker;
    }

    /**
     * Gets the number or rows for preview
     *
     * @return The number or rows for preview
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Gets the mapping for importation
     *
     * @return The mapping for importation
     */
    public CSVMapping getMapping() {
        return mapping;
    }

    /**
     * Gets whether to skip the first row when importing
     *
     * @return Whether to skip the first row when importing
     */
    public boolean getSkipFirstRow() {
        return skipFirstRow;
    }

    /**
     * Initializes this configuration for an importation
     *
     * @param name         The name for the configuration
     * @param separator    The cell separator
     * @param textMarker   The text delimiter
     * @param mapping      The mapping
     * @param skipFirstRow Whether to skip the first row when importing
     */
    public CSVConfiguration(String name, char separator, char textMarker, CSVMapping mapping, boolean skipFirstRow) {
        super(name, CSVImporter.INSTANCE);
        this.separator = separator;
        this.textMarker = textMarker;
        this.rowCount = DEFAULT_ROW_COUNT;
        this.mapping = mapping;
        this.skipFirstRow = skipFirstRow;
    }

    /**
     * Loads this configuration from a serialized definition
     *
     * @param definition The definition
     */
    public CSVConfiguration(ASTNode definition) {
        super(definition);
        char tSeparator = DEFAULT_SEPARATOR;
        char tTextMarker = DEFAULT_TEXT_MARKER;
        int tRowCount = DEFAULT_ROW_COUNT;
        CSVMapping tMapping = null;
        boolean tSkipFirstRow = false;
        for (ASTNode pair : definition.getChildren()) {
            String key = TextUtils.unescape(pair.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "separator": {
                    String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
                    value = value.substring(1, value.length() - 1);
                    tSeparator = value.charAt(0);
                    break;
                }
                case "textMarker": {
                    String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
                    value = value.substring(1, value.length() - 1);
                    tTextMarker = value.charAt(0);
                    break;
                }
                case "rowCount": {
                    String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
                    value = value.substring(1, value.length() - 1);
                    tRowCount = Integer.parseInt(value);
                    break;
                }
                case "mapping": {
                    tMapping = new CSVMapping(pair.getChildren().get(1));
                    break;
                }
                case "skipFirstRow": {
                    String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
                    value = value.substring(1, value.length() - 1);
                    tSkipFirstRow = value.equalsIgnoreCase("true");
                    break;
                }
            }
        }

        this.separator = tSeparator;
        this.textMarker = tTextMarker;
        this.rowCount = tRowCount;
        this.mapping = tMapping;
        this.skipFirstRow = tSkipFirstRow;
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(CSVConfiguration.class.getName()));
        builder.append("\", ");
        serializedJsonBase(builder);
        builder.append(", \"separator\": \"");
        builder.append(TextUtils.escapeStringJSON(Character.toString(separator)));
        builder.append("\", \"textMarker\": \"");
        builder.append(TextUtils.escapeStringJSON(Character.toString(textMarker)));
        builder.append("\", \"rowCount\": \"");
        builder.append(Integer.toString(rowCount));
        builder.append("\"");
        if (mapping != null) {
            builder.append(", \"mapping\": ");
            builder.append(mapping.serializedJSON());
        }
        builder.append(", \"skipFirstRow\": \"");
        builder.append(Boolean.toString(skipFirstRow));
        builder.append("\"}");
        return builder.toString();
    }
}
