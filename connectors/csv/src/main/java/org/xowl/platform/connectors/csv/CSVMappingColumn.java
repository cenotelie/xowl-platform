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
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the import mapping for a column
 *
 * @author Laurent Wouters
 */
public class CSVMappingColumn implements Serializable {
    /**
     * The column is not mapped
     */
    private static final String TYPE_NONE = "none";
    /**
     * The column is mapped to the ID of the entity
     */
    private static final String TYPE_ID = "id";
    /**
     * The column is mapped to an attribute
     */
    private static final String TYPE_ATTRIBUTE = "attribute";
    /**
     * The column is mapped to a relation
     */
    private static final String TYPE_RELATION = "relation";

    /**
     * The type of mapping
     */
    private final String type;
    /**
     * The mapped schema property
     */
    private final String property;
    /**
     * The datatype for the values in the case of a attribute mapping
     */
    private final String datatype;
    /**
     * The regular expression matching the values
     */
    private final Pattern regexp;

    /**
     * Initializes as not mapping a column
     */
    public CSVMappingColumn() {
        this.type = TYPE_NONE;
        this.property = null;
        this.datatype = null;
        this.regexp = null;
    }

    /**
     * Initializes as an ID column
     *
     * @param property The mapped schema property, or null if the ID is not mapped as an additional property
     */
    public CSVMappingColumn(String property) {
        this.type = TYPE_ID;
        this.property = property;
        this.datatype = Vocabulary.xsdString;
        this.regexp = null;
    }

    /**
     * Initializes as a mapping to a relation
     *
     * @param property The mapped schema property
     * @param regexp   The regular expression matching the values
     */
    public CSVMappingColumn(String property, String regexp) {
        this.type = TYPE_RELATION;
        this.property = property;
        this.datatype = null;
        this.regexp = Pattern.compile(regexp);
    }

    /**
     * Initializes as a mapping to an attribute
     *
     * @param property The mapped schema property
     * @param datatype The URI of the datatype for the values
     * @param regexp   The regular expression matching the values
     */
    public CSVMappingColumn(String property, String datatype, String regexp) {
        this.type = TYPE_ATTRIBUTE;
        this.property = property;
        this.datatype = datatype;
        this.regexp = Pattern.compile(regexp);
    }

    /**
     * Initializes from the specified AST node
     *
     * @param node The AST node
     */
    public CSVMappingColumn(ASTNode node) {
        String tType = TYPE_NONE;
        String tProperty = "";
        String tDatatype = "";
        String tRegexp = "";
        for (ASTNode child : node.getChildren()) {
            String key = TextUtils.unescape(child.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            String value = TextUtils.unescape(child.getChildren().get(1).getValue());
            value = value.substring(1, value.length() - 1);
            switch (key) {
                case "type":
                    tType = value;
                    break;
                case "property":
                    tProperty = value;
                    break;
                case "datatype":
                    tDatatype = value;
                    break;
                case "multivalued":
                    tRegexp = value;
                    break;
            }
        }
        this.type = tType;
        this.property = tProperty.isEmpty() ? null : tProperty;
        this.datatype = tDatatype.isEmpty() ? null : tDatatype;
        this.regexp = tRegexp.isEmpty() ? null : Pattern.compile(tRegexp);
    }

    /**
     * Gets whether this column maps the ID
     *
     * @return Whether this column maps the ID
     */
    public boolean isIdMapping() {
        return (type.equals(TYPE_ID));
    }

    /**
     * Applies this mapping
     *
     * @param entity  The current entity
     * @param value   The column's value
     * @param context The current context
     */
    public void apply(IRINode entity, String value, CSVImportationContext context) {
        if (value.startsWith(context.getTextMarker()) && value.endsWith(context.getTextMarker()))
            value = value.substring(1, value.length() - 1);
        switch (type) {
            case TYPE_ID:
                value = value.trim();
                if (property != null)
                    context.addQuad(
                            entity,
                            context.getIRI(property),
                            context.getLiteral(value, datatype)
                    );
                break;
            case TYPE_RELATION:
                if (regexp == null) {
                    context.addQuad(
                            entity,
                            context.getIRI(property),
                            context.resolveEntity(value.trim())
                    );
                } else {
                    Matcher matcher = regexp.matcher(value);
                    while (matcher.find()) {
                        String v = matcher.group();
                        context.addQuad(
                                entity,
                                context.getIRI(property),
                                context.resolveEntity(v)
                        );
                    }
                }
                break;
            case TYPE_ATTRIBUTE:
                if (regexp == null) {
                    context.addQuad(
                            entity,
                            context.getIRI(property),
                            context.getLiteral(value.trim(), datatype)
                    );
                } else {
                    Matcher matcher = regexp.matcher(value);
                    while (matcher.find()) {
                        String v = matcher.group();
                        context.addQuad(
                                entity,
                                context.getIRI(property),
                                context.getLiteral(v, datatype)
                        );
                    }
                }
                break;
        }
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(type) +
                "\", \"property\": \"" +
                TextUtils.escapeStringJSON(property != null ? property : "") +
                "\", \"datatype\": \"" +
                TextUtils.escapeStringJSON(datatype != null ? datatype : "") +
                "\", \"regexp\": \"" +
                TextUtils.escapeStringJSON(regexp != null ? regexp.pattern() : "") +
                "\"}";
    }
}
