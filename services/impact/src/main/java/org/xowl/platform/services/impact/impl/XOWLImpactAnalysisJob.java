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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.RDFUtils;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.*;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultFailure;
import org.xowl.infra.store.sparql.ResultQuads;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobBase;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.services.impact.ImpactAnalysisFilterLink;
import org.xowl.platform.services.impact.ImpactAnalysisFilterType;
import org.xowl.platform.services.impact.ImpactAnalysisService;
import org.xowl.platform.services.impact.ImpactAnalysisSetup;
import org.xowl.platform.services.storage.StorageService;
import org.xowl.platform.services.storage.TripleStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null) {
            result = XSPReplyServiceUnavailable.instance();
            return;
        }
        XSPReply reply = securityService.checkAction(ImpactAnalysisService.ACTION_PERFORM);
        if (!reply.isSuccess()) {
            result = reply;
            return;
        }
        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null) {
            result = XSPReplyServiceUnavailable.instance();
            return;
        }
        TripleStore live = storageService.getLiveStore();
        List<XOWLImpactAnalysisResultPart> finalParts = new ArrayList<>();
        result = new XSPReplyResult<>(new XOWLImpactAnalysisResult(finalParts));
        browseGraph(live, finalParts);
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
            for (ImpactAnalysisFilterType filter : setup.getResultFilter()) {
                boolean match = filter.apply(type, type);
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
     * @param live       The live data base
     * @param finalParts The result parts to build
     */
    private void browseGraph(TripleStore live, List<XOWLImpactAnalysisResultPart> finalParts) {
        List<XOWLImpactAnalysisResultPart> parts = new ArrayList<>();
        parts.add(new XOWLImpactAnalysisResultPart(setup.getRoot()));
        int i = 0;
        while (i != parts.size()) {
            int length = browseGraph(live, parts, i);
            if (length == 0)
                return;
            for (int j = i; j != i + length; j++) {
                XOWLImpactAnalysisResultPart current = parts.get(j);
                if (applyTypeFilter(current))
                    finalParts.add(current);
            }
            i += length;
        }
    }

    /**
     * Browses the graph for a range of nodes
     *
     * @param live  The live data base
     * @param parts The current parts
     * @param start The starting index of the parts to explorer
     * @return The number of parts explored
     */
    private int browseGraph(TripleStore live, List<XOWLImpactAnalysisResultPart> parts, int start) {
        int length = parts.size() - start;
        StringBuilder builder = new StringBuilder("DESCRIBE");
        for (int i = start; i != parts.size(); i++) {
            builder.append(" <");
            builder.append(TextUtils.escapeAbsoluteURIW3C(parts.get(i).getNode().getIRIValue()));
            builder.append(">");
        }
        Result result = live.sparql(builder.toString());
        if (!result.isSuccess()) {
            Logging.getDefault().error(((ResultFailure) result).getMessage());
            return 0;
        }
        Collection<Quad> quads = ((ResultQuads) result).getQuads();
        Map<SubjectNode, Collection<Quad>> data = PlatformUtils.mapBySubject(quads);
        for (Map.Entry<SubjectNode, Collection<Quad>> entry : data.entrySet()) {
            if (entry.getKey().getNodeType() == Node.TYPE_IRI)
                browseGraph(parts, start, (IRINode) entry.getKey(), entry.getValue());
        }
        return length;
    }

    /**
     * Browses the graph for a set of new quads
     *
     * @param parts   The current parts
     * @param start   The starting index of the parts to explorer
     * @param subject The subject to look for
     * @param quads   The incoming quads
     */
    private void browseGraph(List<XOWLImpactAnalysisResultPart> parts, int start, IRINode subject, Collection<Quad> quads) {
        for (int i = start; i != parts.size(); i++) {
            if (RDFUtils.same(parts.get(i).getNode(), subject)) {
                browseGraph(parts, parts.get(i), quads);
                return;
            }
        }
    }

    /**
     * Browses the graph for a set of new quads
     *
     * @param parts   The current parts
     * @param current The current part to explore
     * @param quads   The incoming quads
     */
    private void browseGraph(List<XOWLImpactAnalysisResultPart> parts, XOWLImpactAnalysisResultPart current, Collection<Quad> quads) {
        for (Quad quad : quads) {
            IRINode property = (IRINode) quad.getProperty();
            String propertyName = property.getIRIValue();
            if (quad.getObject().getNodeType() == Node.TYPE_LITERAL) {
                boolean isName = propertyName.endsWith("label") || propertyName.endsWith("name") || propertyName.endsWith("title");
                if (isName) {
                    current.setName((LiteralNode) quad.getObject());
                }
            } else if (quad.getObject().getNodeType() == Node.TYPE_IRI) {
                if (Vocabulary.rdfType.equals(propertyName)) {
                    current.addTpye((IRINode) quad.getObject());
                    continue;
                }
                if (!applyLinkFilters(property))
                    continue;
                if (current.getDegree() >= setup.getDegree())
                    continue;
                boolean found = false;
                for (XOWLImpactAnalysisResultPart part : parts) {
                    if (part.getNode().equals(quad.getObject())) {
                        part.addPaths(current, property);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    parts.add(new XOWLImpactAnalysisResultPart(current, property, (IRINode) quad.getObject()));
                }
            }
        }
    }
}
