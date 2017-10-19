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
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.platform.services.impact.ImpactAnalysisFilterLink;

/**
 * Implements a property filter in an analysis
 *
 * @author Madeleine Wouters
 */
class XOWLImpactAnalysisFilterLink implements ImpactAnalysisFilterLink {
    /**
     * The filtered property
     */
    private final IRINode filtered;

    /**
     * Initializes this filter
     *
     * @param filtered The filtered property
     */
    public XOWLImpactAnalysisFilterLink(IRINode filtered) {
        this.filtered = filtered;
    }

    /**
     * Initializes this filter
     *
     * @param nodes      The current node manager
     * @param definition The filter's definition
     */
    public XOWLImpactAnalysisFilterLink(NodeManager nodes, ASTNode definition) {
        String filtered = null;
        for (ASTNode member : definition.getChildren()) {
            String memberName = TextUtils.unescape(member.getChildren().get(0).getValue());
            memberName = memberName.substring(1, memberName.length() - 1);
            switch (memberName) {
                case "filtered":
                    filtered = TextUtils.unescape(member.getChildren().get(1).getValue());
                    filtered = filtered.substring(1, filtered.length() - 1);
                    break;
            }
        }
        this.filtered = nodes.getIRINode(filtered);
    }

    @Override
    public boolean apply(IRINode link) {
        return link.equals(filtered);
    }

    @Override
    public String serializedString() {
        return filtered.getIRIValue();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(ImpactAnalysisFilterLink.class.getCanonicalName()) +
                "\", \"filtered\": \"" +
                TextUtils.escapeStringJSON(filtered.getIRIValue()) +
                "\"}";
    }
}
