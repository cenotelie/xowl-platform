/*******************************************************************************
 * Copyright (c) 2016 Madeleine Wouters
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
 *
 * Contributors:
 *     Madeleine Wouters - woutersmadeleine@gmail.com
 ******************************************************************************/

package org.xowl.platform.services.impact.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.Node;
import org.xowl.infra.store.rdf.QuerySolution;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultSolutions;
import org.xowl.infra.utils.collections.Couple;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobBase;
import org.xowl.platform.services.impact.ImpactAnalysisFilterLink;
import org.xowl.platform.services.impact.ImpactAnalysisSetup;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a job that performs an impact analysis
 *
 * @author Laurent Wouters
 */
class XOWLImpactAnalysisJob extends JobBase {
    /**
     * The analysis setup
     */
    private final ImpactAnalysisSetup setup;
    /**
     * The result, if any
     */
    private XSPReply result;

    /**
     * Initializes this job
     *
     * @param setup The analysis setup
     */
    public XOWLImpactAnalysisJob(ImpactAnalysisSetup setup) {
        super("Impact Analysis from " + setup.serializedString(), XOWLImpactAnalysisJob.class.getCanonicalName());
        this.setup = setup;
    }

    /**
     * Initializes this job
     *
     * @param definition The payload definition
     */
    public XOWLImpactAnalysisJob(ASTNode definition) {
        super(definition);
        this.setup = new XOWLImpactAnalysisSetup(getPayloadNode(definition));
    }

    @Override
    protected String getJSONSerializedPayload() {
        return setup.serializedJSON();
    }

    @Override
    public XSPReply getResult() {
        return result;
    }

    @Override
    public void doRun() {
        TripleStoreService tripleStoreService = ServiceUtils.getService(TripleStoreService.class);
        if (tripleStoreService == null) {
            result = XSPReplyServiceUnavailable.instance();
            return;
        }
        TripleStore live = tripleStoreService.getLiveStore();
        List<XOWLImpactAnalysisResultPart> parts = browseGraph(live);
        List<XOWLImpactAnalysisResultPart> finalParts = new ArrayList<>();
        for (XOWLImpactAnalysisResultPart part : parts) {
            if (applyTypeFilter(part))
                finalParts.add(part);
        }
        result = new XSPReplyResult<>(new XOWLImpactAnalysisResult(finalParts));
    }

    /**
     * Find all neighbours and their property associated of a subject (the current node)
     *
     * @param subject The current node
     * @param live    Live data base
     * @return The collection of neighbours founded and their property
     */
    private Collection<Couple<IRINode, IRINode>> neighbours(IRINode subject, TripleStore live) {
        Result result = live.sparql("SELECT DISTINCT ?x ?p WHERE { GRAPH ?g { <" + IOUtils.escapeStringW3C(subject.getIRIValue()) + "> ?p ?x" + " } }");
        Collection<Couple<IRINode, IRINode>> values = new ArrayList<>();
        for (QuerySolution solution : ((ResultSolutions) result).getSolutions()) {
            Node neighbour = solution.get("x");
            Node property = solution.get("p");
            if (neighbour.getNodeType() == Node.TYPE_IRI)
                values.add(new Couple<>((IRINode) neighbour, (IRINode) property));
        }
        result = live.sparql("SELECT DISTINCT ?x ?p WHERE { GRAPH ?g {?x ?p <" + IOUtils.escapeStringW3C(subject.getIRIValue()) + "> " + "}}");
        for (QuerySolution solution : ((ResultSolutions) result).getSolutions()) {
            Node neighbour = solution.get("x");
            Node property = solution.get("p");
            if (neighbour.getNodeType() == Node.TYPE_IRI)
                values.add(new Couple<>((IRINode) neighbour, (IRINode) property));
        }
        return values;
    }

    /**
     * Compare the current link to the filters
     *
     * @param link The current link
     * @return Whether the link is accepted or not
     */
    private boolean applyLinkFilters(IRINode link) {
        for (ImpactAnalysisFilterLink filter : setup.getWalkerFilters()) {
            boolean match = filter.apply(link);
            if (setup.isWalkerFilterInclusive()) {
                if (match)
                    return true;
            } else {
                if (match)
                    return false;
            }
        }
        return !setup.isWalkerFilterInclusive();
    }

    /**
     * Compare all the types of the current node to the filters
     *
     * @param part The current node
     * @return Whether the node is accepted or not
     */
    private boolean applyTypeFilter(XOWLImpactAnalysisResultPart part) {
        for (IRINode type : part.getTypes()) {
            for (ImpactAnalysisFilterLink filter : setup.getWalkerFilters()) {
                boolean match = filter.apply(type);
                if (setup.isResultFilterInclusive()) {
                    if (match)
                        return true;
                } else {
                    if (match)
                        return false;
                }
            }
        }
        return !setup.isResultFilterInclusive();
    }

    /**
     * Browse the graph while applying the link filters on it
     *
     * @param live The live data base
     * @return The list of nodes founded with their characteristics
     */
    private List<XOWLImpactAnalysisResultPart> browseGraph(TripleStore live) {
        List<XOWLImpactAnalysisResultPart> parts = new ArrayList<>();
        parts.add(new XOWLImpactAnalysisResultPart(setup.getRoot()));
        int i = 0;
        while (i != parts.size()) {
            XOWLImpactAnalysisResultPart current = parts.get(i);
            if (current.getDegree() < setup.getDegree()) {
                Collection<Couple<IRINode, IRINode>> neighbours = neighbours(current.getNode(), live);
                for (Couple<IRINode, IRINode> couple : neighbours) {
                    if (Vocabulary.rdfType.equals(couple.y.getIRIValue())) {
                        current.addTpye(couple.x);
                        continue;
                    }
                    if (!applyLinkFilters(couple.y))
                        continue;
                    boolean found = false;
                    for (XOWLImpactAnalysisResultPart part : parts) {
                        if (part.getNode().equals(couple.x)) {
                            part.addPaths(current, couple.y);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        parts.add(new XOWLImpactAnalysisResultPart(current, couple.y, couple.x));
                    }
                }
            }
            i++;
        }
        return parts;
    }
}
