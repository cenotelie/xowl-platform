/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.consistency.impl;

import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.consistency.ConsistencyRule;
import org.xowl.platform.services.consistency.ConsistencyService;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;
import org.xowl.store.IOUtils;
import org.xowl.store.xsp.XSPReply;
import org.xowl.store.xsp.XSPReplyFailure;
import org.xowl.store.xsp.XSPReplyResultCollection;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Implements a consistency service for the xOWL platform
 * @author Laurent Wouters
 */
public class XOWLConsistencyService implements ConsistencyService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "consistency"
    };

    /**
     * The URI of the graph for metadata on the consistency rules
     */
    private static final String IRI_RULE_METADATA = "http://xowl.org/platform/consistency/metadata";
    /**
     * The base URI for a consistency rule
     */
    private static final String IRI_RULE_BASE = "http://xowl.org/platform/consistency/rule";

    @Override
    public String getIdentifier() {
        return XOWLConsistencyService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Consistency Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public IOUtils.HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {

        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Override
    public XSPReply getRules() {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        XSPReply reply = live.execute("LIST RULES");
        if (!reply.isSuccess())
            return reply;
        Collection<String> ruleNames = ((XSPReplyResultCollection<String>)reply).getData();
        reply = live.execute("LIST ACTIVE RULES");
        if (!reply.isSuccess())
            return reply;
        Collection<String> activeRuleNames = ((XSPReplyResultCollection<String>)reply).getData();

        

        return null;
    }

    @Override
    public XSPReply getInconsistencies() {
        return null;
    }

    @Override
    public XSPReply createRule(String name, String message, String prefixes, String conditions) {
        return null;
    }

    @Override
    public XSPReply activateRule(String identifier) {
        return null;
    }

    @Override
    public XSPReply activateRule(ConsistencyRule rule) {
        return null;
    }

    @Override
    public XSPReply deactivateRule(String identifier) {
        return null;
    }

    @Override
    public XSPReply deactivateRule(ConsistencyRule rule) {
        return null;
    }

    @Override
    public XSPReply deleteRule(String identifier) {
        return null;
    }

    @Override
    public XSPReply deleteRule(ConsistencyRule rule) {
        return null;
    }
}
