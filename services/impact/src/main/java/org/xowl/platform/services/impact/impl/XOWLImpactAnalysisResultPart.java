/*******************************************************************************
 * Copyright (c) 2016 Madeleine Wouters
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General
 * Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors:
 * Madeleine Wouters - woutersmadeleine@gmail.com
 ******************************************************************************/

package org.xowl.platform.services.impact.impl;

import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.LiteralNode;
import org.xowl.infra.store.rdf.Node;
import org.xowl.infra.utils.collections.Couple;
import org.xowl.platform.services.impact.ImpactAnalysisResultPart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Define the result node
 *
 * @author Madeleine Wouters
 */
class XOWLImpactAnalysisResultPart implements ImpactAnalysisResultPart {
    /**
     * The node
     */
    private final IRINode node;
    /**
     * The degree of the node : its layer
     */
    private final int degree;
    /**
     * All the paths to reach the node
     */
    private final Collection<Collection<Couple<String, IRINode>>> paths;
    /**
     * All the types of the node
     */
    private final Collection<IRINode> types;
    /**
     * The name of the node
     */
    private String name;

    /**
     * Initialization of this result part
     *
     * @param previous The previous node : where we come from
     * @param property The property between the previous node and the current node
     * @param node     The current node
     */
    public XOWLImpactAnalysisResultPart(XOWLImpactAnalysisResultPart previous, IRINode property, IRINode node) {
        this.node = node;
        this.degree = previous.degree + 1;
        this.paths = new ArrayList<>();
        this.types = new ArrayList<>();
        this.name = node.getIRIValue();
        addPaths(previous, property);
    }

    /**
     * Initialization of this result part
     *
     * @param root The current node
     */
    public XOWLImpactAnalysisResultPart(IRINode root) {
        this.node = root;
        this.degree = 0;
        this.paths = new ArrayList<>();
        this.types = new ArrayList<>();
    }

    /**
     * Add a type to the node
     *
     * @param type The type to add
     */
    public void addTpye(IRINode type) {
        types.add(type);
    }

    /**
     * Set his name to the node
     * @param node
     */
    public void setName(LiteralNode node) {
        name = node.getLexicalValue();
    }

    /**
     * Add the new paths for reaching this node
     *
     * @param previous The previous node : where we come from
     * @param property The property between the previous node and the current node
     */
    public void addPaths(XOWLImpactAnalysisResultPart previous, IRINode property) {
        if (previous.paths.isEmpty()) {
            Collection<Couple<String, IRINode>> newPath = new ArrayList<>();
            newPath.add(new Couple<>(previous.name, property));
            paths.add(newPath);
        } else {
            for (Collection<Couple<String, IRINode>> path : previous.paths) {
                Collection<Couple<String, IRINode>> newPath = new ArrayList<>();
                for (Couple<String, IRINode> couple : path) {
                    Couple<String, IRINode> newCouple = new Couple<>(couple.x, couple.y);
                    newPath.add(newCouple);
                }
                newPath.add(new Couple<>(previous.name, property));
                paths.add(newPath);
            }
        }
    }

    @Override
    public IRINode getNode() {
        return node;
    }

    @Override
    public int getDegree() {
        return degree;
    }

    @Override
    public Collection<IRINode> getTypes() {
        return Collections.unmodifiableCollection(types);
    }

    @Override
    public Collection<Collection<Couple<String, IRINode>>> getPaths() {
        return Collections.unmodifiableCollection(paths);
    }

    @Override
    public String serializedString() {
        return node.getIRIValue();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(IOUtils.escapeStringJSON(ImpactAnalysisResultPart.class.getCanonicalName()));
        builder.append("\", \"node\": \"");
        builder.append(IOUtils.escapeStringJSON(node.getIRIValue()));
        builder.append("\", \"degree\": ");
        builder.append(Integer.toString(degree));
        builder.append(", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(name));
        builder.append("\", \"types\": [");
        boolean first = true;
        for (IRINode type : types) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(IOUtils.escapeStringJSON(type.getIRIValue()));
            builder.append("\"");
        }
        builder.append("], \"paths\": [");
        first = true;
        for (Collection<Couple<String, IRINode>> path : paths) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("{\"elements\": [");
            boolean f = true;
            for (Couple<String, IRINode> couple : path) {
                if (!f)
                    builder.append(", ");
                f = false;
                builder.append("{\"target\": \"");
                builder.append(IOUtils.escapeStringJSON(couple.x));
                builder.append("\", \"property\": \"");
                builder.append(IOUtils.escapeStringJSON(couple.y.getIRIValue()));
                builder.append("\"}");
            }
            builder.append("]}");
        }
        builder.append("]}");
        return builder.toString();
    }
}
