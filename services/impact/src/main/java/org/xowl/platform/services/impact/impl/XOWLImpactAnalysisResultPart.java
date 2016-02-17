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

import org.xowl.infra.store.rdf.IRINode;
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
    private final Collection<Collection<Couple<IRINode, IRINode>>> paths;
    /**
     * All the types of the node
     */
    private final Collection<IRINode> types;

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
     * Add the new paths for reaching this node
     *
     * @param previous The previous node : where we come from
     * @param property The property between the previous node and the current node
     */
    public void addPaths(XOWLImpactAnalysisResultPart previous, IRINode property) {
        for (Collection<Couple<IRINode, IRINode>> path : previous.paths) {
            Collection<Couple<IRINode, IRINode>> newPath = new ArrayList<>();
            for (Couple<IRINode, IRINode> couple : path) {
                Couple<IRINode, IRINode> newCouple = new Couple<>(couple.x, couple.y);
                newPath.add(newCouple);
            }
            newPath.add(new Couple<>(previous.node, property));
            paths.add(newPath);
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
    public Collection<Collection<Couple<IRINode, IRINode>>> getPaths() {
        return Collections.unmodifiableCollection(paths);
    }


}
