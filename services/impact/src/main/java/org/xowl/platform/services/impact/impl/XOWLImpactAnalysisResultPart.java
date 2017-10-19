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

package org.xowl.platform.services.impact.impl;

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.collections.Couple;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.LiteralNode;
import org.xowl.platform.services.impact.ImpactAnalysisResultPart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    private final Collection<List<Couple<ImpactAnalysisResultPart, IRINode>>> paths;
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
     *
     * @param node The name
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
            // the previous node is the root
            List<Couple<ImpactAnalysisResultPart, IRINode>> newPath = new ArrayList<>();
            newPath.add(new Couple<ImpactAnalysisResultPart, IRINode>(previous, property));
            paths.add(newPath);
        } else {
            for (List<Couple<ImpactAnalysisResultPart, IRINode>> path : previous.paths) {
                boolean isValid = true; // flag whether the path already contains this node as an antecedent
                for (Couple<ImpactAnalysisResultPart, IRINode> part : path) {
                    if (part.x == this) {
                        isValid = false;
                        break;
                    }
                }
                if (!isValid)
                    continue;
                List<Couple<ImpactAnalysisResultPart, IRINode>> newPath = new ArrayList<>(path);
                newPath.add(new Couple<ImpactAnalysisResultPart, IRINode>(previous, property));
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
    public String getName() {
        return name != null ? name : node.getIRIValue();
    }

    @Override
    public Collection<List<Couple<ImpactAnalysisResultPart, IRINode>>> getPaths() {
        return Collections.unmodifiableCollection(paths);
    }

    @Override
    public String serializedString() {
        return node.getIRIValue();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(ImpactAnalysisResultPart.class.getCanonicalName()));
        builder.append("\", \"node\": \"");
        builder.append(TextUtils.escapeStringJSON(node.getIRIValue()));
        builder.append("\", \"degree\": ");
        builder.append(Integer.toString(degree));
        builder.append(", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"types\": [");
        boolean first = true;
        for (IRINode type : types) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(type.getIRIValue()));
            builder.append("\"");
        }
        builder.append("], \"paths\": [");
        first = true;
        for (List<Couple<ImpactAnalysisResultPart, IRINode>> path : paths) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("{\"elements\": [");
            for (int i = 0; i != path.size(); i++) {
                if (i > 0)
                    builder.append(", ");
                Couple<ImpactAnalysisResultPart, IRINode> part = path.get(i);
                builder.append("{\"target\": \"");
                builder.append(TextUtils.escapeStringJSON(part.x.getName()));
                builder.append("\", \"property\": \"");
                builder.append(TextUtils.escapeStringJSON(part.y.getIRIValue()));
                builder.append("\"}");
            }
            builder.append("]}");
        }
        builder.append("]}");
        return builder.toString();
    }
}
