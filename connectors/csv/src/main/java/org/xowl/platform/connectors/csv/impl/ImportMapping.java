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

package org.xowl.platform.connectors.csv.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Serializable;
import org.xowl.infra.store.rdf.IRINode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the import mapping for a document
 * The hypothesis is one entity per line.
 *
 * @author Laurent Wouters
 */
class ImportMapping implements Serializable {
    /**
     * The mapping for the columns
     */
    private final List<ImportMappingColumn> columns;

    /**
     * Initializes an empty mapping
     */
    public ImportMapping() {
        this.columns = new ArrayList<>();
    }

    /**
     * Initializes from the specified AST node
     *
     * @param node The AST node
     */
    public ImportMapping(ASTNode node) {
        this.columns = new ArrayList<>();
        for (ASTNode child : node.getChildren()) {
            String key = IOUtils.unescape(child.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            if (key.equals("columns")) {
                for (ASTNode columnNode : child.getChildren().get(1).getChildren()) {
                    this.columns.add(new ImportMappingColumn(columnNode));
                }
            }
        }
    }

    /**
     * Applies the mapping to an input document
     *
     * @param document The input document
     * @param context  The context
     */
    public void apply(Iterator<Iterator<String>> document, ImportMappingContext context) {
        while (document.hasNext()) {
            Iterator<String> row = document.next();
            applyRow(row, context);
        }
    }

    /**
     * Applies the mapping to a row
     *
     * @param row     The input row
     * @param context The context
     */
    private void applyRow(Iterator<String> row, ImportMappingContext context) {
        String[] values = new String[columns.size()];
        int i = 0;
        while (row.hasNext()) {
            String value = row.next();
            if (i < values.length)
                values[i++] = value;
        }

        IRINode id = null;
        for (i = 0; i != columns.size(); i++) {
            if (columns.get(i).isIdMapping()) {
                if (values[i] == null || values[i].isEmpty())
                    // no id => skip this
                    return;
                id = context.resolveEntity(values[i]);
                break;
            }
        }
        if (id == null)
            id = context.newEntity();

        for (i = 0; i != columns.size(); i++) {
            if (values[i] != null && !values[i].isEmpty() && !columns.get(i).isIdMapping()) {
                columns.get(i).apply(id, values[i], context);
            }
        }
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"columns\": [");
        for (int i = 0; i != columns.size(); i++) {
            if (i != 0)
                builder.append(", ");
            builder.append(columns.get(i).serializedJSON());
        }
        builder.append("]");
        return builder.toString();
    }
}
