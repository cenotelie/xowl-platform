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
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.IRINode;

/**
 * Represents the import mapping for a column
 *
 * @author Laurent Wouters
 */
public class ImportMappingColumn implements Serializable {
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
     * The mapped schema relation
     */
    private final String schemaRelation;
    /**
     * The datatype for the values in the case of a attribute mapping
     */
    private final String schemaAttributeType;
    /**
     * Whether the column is multivalued
     */
    private final boolean multivalued;

    /**
     * Initializes as not mapping a column
     */
    public ImportMappingColumn() {
        this.type = TYPE_NONE;
        this.schemaRelation = null;
        this.schemaAttributeType = null;
        this.multivalued = false;
    }

    /**
     * Initializes as an ID column
     *
     * @param relation The URI of the mapped relation, or null if the ID is noe mapped as an additional property
     */
    public ImportMappingColumn(String relation) {
        this.type = TYPE_ID;
        this.schemaRelation = relation;
        this.schemaAttributeType = Vocabulary.xsdString;
        this.multivalued = false;
    }

    /**
     * Initializes as a mapping to a relation
     *
     * @param relation    The URI of the relation
     * @param multivalued Whether the column is multivalued
     */
    public ImportMappingColumn(String relation, boolean multivalued) {
        this.type = TYPE_RELATION;
        this.schemaRelation = relation;
        this.schemaAttributeType = null;
        this.multivalued = multivalued;
    }

    /**
     * Initializes as a mapping to an attribute
     *
     * @param attribute   The URI of the attribute
     * @param datatype    The URI of the datatype for the values
     * @param multivalued Whether the column is multivalued
     */
    public ImportMappingColumn(String attribute, String datatype, boolean multivalued) {
        this.type = TYPE_ATTRIBUTE;
        this.schemaRelation = attribute;
        this.schemaAttributeType = datatype;
        this.multivalued = multivalued;
    }

    /**
     * Initializes from the specified AST node
     *
     * @param node The AST node
     */
    public ImportMappingColumn(ASTNode node) {
        String tType = TYPE_NONE;
        String tRelation = "";
        String tAttribute = "";
        String tMulti = "false";
        for (ASTNode child : node.getChildren()) {
            String key = IOUtils.unescape(child.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            String value = IOUtils.unescape(child.getChildren().get(1).getValue());
            value = value.substring(1, value.length() - 1);
            switch (key) {
                case "type":
                    tType = value;
                    break;
                case "schemaRelation":
                    tRelation = value;
                    break;
                case "schemaAttributeType":
                    tAttribute = value;
                    break;
                case "multivalued":
                    tMulti = value;
                    break;
            }
        }
        this.type = tType;
        this.schemaRelation = tRelation.isEmpty() ? null : tRelation;
        this.schemaAttributeType = tAttribute.isEmpty() ? null : tAttribute;
        this.multivalued = tMulti.equalsIgnoreCase("true");
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
    public void apply(IRINode entity, String value, ImportMappingContext context) {
        if (value.startsWith(context.getTextMarker()) && value.endsWith(context.getTextMarker()))
            value = value.substring(1, value.length() - 1);
        switch (type) {
            case TYPE_ID:
                if (schemaRelation != null)
                    context.addQuad(
                            entity,
                            context.getIRI(schemaRelation),
                            context.getLiteral(value, schemaAttributeType)
                    );
                break;
            case TYPE_RELATION:
                context.addQuad(
                        entity,
                        context.getIRI(schemaRelation),
                        context.resolveEntity(value)
                );
                break;
            case TYPE_ATTRIBUTE:
                context.addQuad(
                        entity,
                        context.getIRI(schemaRelation),
                        context.getLiteral(value, schemaAttributeType)
                );
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
                IOUtils.escapeStringJSON(type) +
                "\", \"schemaRelation\": \"" +
                IOUtils.escapeStringJSON(schemaRelation != null ? schemaRelation : "") +
                "\", \"schemaAttributeType\": \"" +
                IOUtils.escapeStringJSON(schemaAttributeType != null ? schemaAttributeType : "") +
                "\", \"multivalued\": \"" +
                Boolean.toString(multivalued) +
                "\"}";
    }
}
