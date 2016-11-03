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
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.services.impact.ImpactAnalysisFilterLink;
import org.xowl.platform.services.impact.ImpactAnalysisFilterType;
import org.xowl.platform.services.impact.ImpactAnalysisSetup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements the setup parameters of an impact analysis
 *
 * @author Laurent Wouters
 */
class XOWLImpactAnalysisSetup implements ImpactAnalysisSetup {
    /**
     * The analysis's root
     */
    private final IRINode root;
    /**
     * The analysis's maximum number of hops
     */
    private final int degree;
    /**
     * The filters for the links
     */
    private final Collection<ImpactAnalysisFilterLink> filterLinks;
    /**
     * Whether the walker filters define the included links, or the excluded ones
     */
    private final boolean isFilterLinksInclusive;
    /**
     * The filters for the results
     */
    private final Collection<ImpactAnalysisFilterType> filterResults;
    /**
     * Whether the result filters define the included types, or the excluded ones
     */
    private final boolean isFilterResultsInclusive;

    /**
     * Initializes this setup
     *
     * @param root                     The analysis's root
     * @param degree                   The analysis's maximum number of hops
     * @param filterLinks              The filters for the links
     * @param isFilterLinksInclusive   Whether the walker filters define the included links, or the excluded ones
     * @param filterResults            The filters for the results
     * @param isFilterResultsInclusive Whether the result filters define the included types, or the excluded ones
     */
    public XOWLImpactAnalysisSetup(IRINode root,
                                   int degree,
                                   Collection<ImpactAnalysisFilterLink> filterLinks,
                                   boolean isFilterLinksInclusive,
                                   Collection<ImpactAnalysisFilterType> filterResults,
                                   boolean isFilterResultsInclusive) {
        this.root = root;
        this.degree = degree;
        this.filterLinks = new ArrayList<>(filterLinks);
        this.isFilterLinksInclusive = isFilterLinksInclusive;
        this.filterResults = new ArrayList<>(filterResults);
        this.isFilterResultsInclusive = isFilterResultsInclusive;
    }

    /**
     * Initializes this analysis setup
     *
     * @param definition The definition
     */
    public XOWLImpactAnalysisSetup(ASTNode definition) {
        NodeManager nodes = new CachedNodes();
        String root = null;
        String value;
        int degree = 0;
        boolean isFilterLinksInclusive = false;
        boolean isFilterResultsInclusive = false;
        this.filterLinks = new ArrayList<>();
        this.filterResults = new ArrayList<>();
        for (ASTNode member : definition.getChildren()) {
            String memberName = TextUtils.unescape(member.getChildren().get(0).getValue());
            memberName = memberName.substring(1, memberName.length() - 1);
            switch (memberName) {
                case "root":
                    root = TextUtils.unescape(member.getChildren().get(1).getValue());
                    root = root.substring(1, root.length() - 1);
                    break;
                case "degree":
                    value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    if (value.startsWith("\""))
                        value = value.substring(1, value.length() - 1);
                    degree = Integer.parseInt(value);
                    break;
                case "filterLinks":
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        this.filterLinks.add(new XOWLImpactAnalysisFilterLink(nodes, child));
                    }
                    break;
                case "isFilterLinksInclusive":
                    value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    if (value.startsWith("\""))
                        value = value.substring(1, value.length() - 1);
                    isFilterLinksInclusive = "true".equalsIgnoreCase(value);
                    break;
                case "filterResults":
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        this.filterResults.add(new XOWLImpactAnalysisFilterType(nodes, child));
                    }
                    break;
                case "isFilterResultsInclusive":
                    value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    if (value.startsWith("\""))
                        value = value.substring(1, value.length() - 1);
                    isFilterResultsInclusive = "true".equalsIgnoreCase(value);
                    break;
            }
        }
        this.root = nodes.getIRINode(root);
        this.degree = degree;
        this.isFilterLinksInclusive = isFilterLinksInclusive;
        this.isFilterResultsInclusive = isFilterResultsInclusive;
    }

    @Override
    public IRINode getRoot() {
        return root;
    }

    @Override
    public int getDegree() {
        return degree;
    }

    @Override
    public Collection<ImpactAnalysisFilterLink> getWalkerFilters() {
        return Collections.unmodifiableCollection(filterLinks);
    }

    @Override
    public boolean isWalkerFilterInclusive() {
        return isFilterLinksInclusive;
    }

    @Override
    public Collection<ImpactAnalysisFilterType> getResultFilter() {
        return Collections.unmodifiableCollection(filterResults);
    }

    @Override
    public boolean isResultFilterInclusive() {
        return isFilterResultsInclusive;
    }

    @Override
    public String serializedString() {
        return root.getIRIValue();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(ImpactAnalysisSetup.class.getCanonicalName()));
        builder.append("\", \"root\": \"");
        builder.append(TextUtils.escapeStringJSON(root.getIRIValue()));
        builder.append("\", \"degree\": ");
        builder.append(Integer.toString(degree));
        builder.append(", \"filterLinks\": [");
        boolean first = true;
        for (ImpactAnalysisFilterLink filter : filterLinks) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(filter.serializedJSON());
        }
        builder.append("], \"isFilterLinksInclusive\": ");
        builder.append(Boolean.toString(isFilterLinksInclusive));
        builder.append(", \"filterResults\": [");
        first = true;
        for (ImpactAnalysisFilterType filter : filterResults) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(filter.serializedJSON());
        }
        builder.append("], \"isFilterResultsInclusive\": ");
        builder.append(Boolean.toString(isFilterResultsInclusive));
        builder.append("}");
        return builder.toString();
    }
}
