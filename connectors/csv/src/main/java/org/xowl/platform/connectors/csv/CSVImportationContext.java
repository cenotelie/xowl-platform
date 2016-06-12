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

import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.LiteralNode;
import org.xowl.infra.store.rdf.Node;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.NodeManager;

import java.util.*;

/**
 * A context when importing from CSV through a mapping
 *
 * @author Laurent Wouters
 */
class CSVImportationContext {
    /**
     * The used text marker
     */
    private final String textMarker;
    /**
     * The node manager
     */
    private final NodeManager nodes;
    /**
     * The graph for the mapped content
     */
    private final IRINode graph;
    /**
     * The base URI for the mapped entities
     */
    private final String baseURI;
    /**
     * The known entities
     */
    private final Map<String, IRINode> entities;
    /**
     * The mapped quads
     */
    private final Collection<Quad> quads;

    /**
     * Initializes this context
     *
     * @param textMarker The used text marker
     * @param nodes      The node manager
     * @param graphURI   The URI of the graph for the produced quads
     * @param baseURI    The base URI for the resolved entities
     */
    public CSVImportationContext(String textMarker, NodeManager nodes, String graphURI, String baseURI) {
        this.textMarker = textMarker;
        this.nodes = nodes;
        this.graph = nodes.getIRINode(graphURI);
        this.baseURI = baseURI;
        this.entities = new HashMap<>();
        this.quads = new ArrayList<>();
    }

    /**
     * Gets the used text marker
     *
     * @return The used text marker
     */
    public String getTextMarker() {
        return textMarker;
    }

    /**
     * Gets the mapped quads
     *
     * @return The mapped quads
     */
    public Collection<Quad> getQuads() {
        return quads;
    }

    /**
     * Adds a quad to the result
     *
     * @param entity   The entity
     * @param property The property
     * @param value    The value
     */
    public void addQuad(IRINode entity, IRINode property, Node value) {
        quads.add(new Quad(graph, entity, property, value));
    }

    /**
     * Resolves an entity from its identifier
     *
     * @param id The identifier of a mapped entity
     * @return The mapped entity
     */
    public IRINode resolveEntity(String id) {
        IRINode entity = entities.get(id);
        if (entity == null) {
            entity = nodes.getIRINode(baseURI + "#" + UUID.randomUUID().toString());
            entities.put(id, entity);
        }
        return entity;
    }

    /**
     * Creates a new entity
     *
     * @return A new entity
     */
    public IRINode newEntity() {
        return nodes.getIRINode(baseURI + "#" + UUID.randomUUID().toString());
    }

    /**
     * Gets the IRI node for a string IRI
     *
     * @param iri The string serialization of an IRI
     * @return The associated node
     */
    public IRINode getIRI(String iri) {
        return nodes.getIRINode(iri);
    }

    /**
     * Gets the literal node for a value
     *
     * @param lexical  The lexical value of the literal
     * @param datatype The required datatype URI
     * @return The literal node
     */
    public LiteralNode getLiteral(String lexical, String datatype) {
        return nodes.getLiteralNode(lexical, datatype, null);
    }
}
