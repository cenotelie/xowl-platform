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

import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.consistency.ConsistencyRule;
import org.xowl.platform.services.consistency.ConsistencyService;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;
import org.xowl.platform.utils.Utils;
import org.xowl.store.IOUtils;
import org.xowl.store.IRIs;
import org.xowl.store.Vocabulary;
import org.xowl.store.rdf.IRINode;
import org.xowl.store.rdf.LiteralNode;
import org.xowl.store.rdf.QuerySolution;
import org.xowl.store.sparql.Result;
import org.xowl.store.sparql.ResultFailure;
import org.xowl.store.sparql.ResultSolutions;
import org.xowl.store.xsp.*;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Implements a consistency service for the xOWL platform
 *
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
     * The URI for the concept of rule
     */
    private static final String IRI_RULE = "http://xowl.org/platform/schemas/consistency#Rule";
    /**
     * The URI for the concept of inconsistency
     */
    private static final String IRI_INCONSISTENCY = "http://xowl.org/platform/schemas/consistency#Inconsistency";
    /**
     * The URI for the concept of definition
     */
    private static final String IRI_DEFINITION = "http://xowl.org/platform/schemas/consistency#definition";
    /**
     * The URI for the concept of message
     */
    private static final String IRI_MESSAGE = "http://xowl.org/platform/schemas/consistency#message";
    /**
     * The base URI for a consistency rule
     */
    private static final String IRI_RULE_BASE = "http://xowl.org/platform/consistency/rule";

    /**
     * The timeout for cache invalidation, in nano-seconds
     * Set to 2 second (1 billion nano-seconds)
     */
    private static final long CACHE_INVALIDATION_TIMEOUT = 2000000000L;

    /**
     * The current consistency rules
     */
    private final Collection<XOWLConsistencyRule> rules;
    /**
     * Timestamp of the last time the rules were updated
     */
    private long rulesTimestamp;
    /**
     * The current inconsistencies
     */
    private final Collection<XOWLInconsistency> inconsistencies;
    /**
     * Time stamp of the last time the inconsistencies were updated
     */
    private long inconsistenciesTimestamp;

    /**
     * Initializes this service
     */
    public XOWLConsistencyService() {
        this.rules = new ArrayList<>(15);
        this.rulesTimestamp = 0;
        this.inconsistencies = new ArrayList<>(150);
        this.inconsistenciesTimestamp = 0;
    }

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
        long now = System.nanoTime();
        long elapsed = now - rulesTimestamp;
        if (elapsed < CACHE_INVALIDATION_TIMEOUT)
            return new XSPReplyResultCollection<>(rules);

        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        XSPReply reply = live.execute("LIST RULES");
        if (!reply.isSuccess())
            return reply;
        Collection<String> ruleNames = ((XSPReplyResultCollection<String>) reply).getData();
        reply = live.execute("LIST ACTIVE RULES");
        if (!reply.isSuccess())
            return reply;
        Collection<String> activeRuleNames = ((XSPReplyResultCollection<String>) reply).getData();
        Result result = live.sparql("SELECT DISTINCT ?r ?n ?d WHERE { GRAPH <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) +
                "> { ?r a <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_RULE) +
                "> . ?r <" +
                IOUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n . ?r <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_DEFINITION) +
                "> ?d } }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        ResultSolutions solutions = (ResultSolutions) result;
        rules.clear();
        for (QuerySolution solution : solutions.getSolutions()) {
            String ruleId = ((IRINode) solution.get("r")).getIRIValue();
            String ruleName = ((LiteralNode) solution.get("n")).getLexicalValue();
            String ruleDefinition = ((LiteralNode) solution.get("d")).getLexicalValue();
            if (!ruleNames.contains(ruleId))
                continue;
            rules.add(new XOWLConsistencyRule(ruleId, ruleName, activeRuleNames.contains(ruleId), ruleDefinition));
        }
        rulesTimestamp = System.nanoTime();
        return new XSPReplyResultCollection<>(rules);
    }

    @Override
    public XSPReply getInconsistencies() {
        long now = System.nanoTime();
        long elapsed = now - inconsistenciesTimestamp;
        if (elapsed < CACHE_INVALIDATION_TIMEOUT)
            return new XSPReplyResultCollection<>(inconsistencies);

        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        Result result = live.sparql("SELECT DISTINCT ?i ?r ?m WHERE { GRAPH <" +
                IOUtils.escapeAbsoluteURIW3C(IRIs.GRAPH_INFERENCE) +
                "> { ?i a <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_INCONSISTENCY) +
                "> . ?i <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_MESSAGE) +
                "> ?m . ?i <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_DEFINITION) +
                "> ?r } }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        ResultSolutions solutions = (ResultSolutions) result;
        inconsistencies.clear();
        for (QuerySolution solution : solutions.getSolutions()) {
            String inconsistencyId = ((IRINode) solution.get("i")).getIRIValue();
            String inconsistencyRuleId = ((LiteralNode) solution.get("r")).getLexicalValue();
            String inconsistencyMessage = ((LiteralNode) solution.get("m")).getLexicalValue();
            XSPReply reply = getRule(inconsistencyId);
            if (reply.isSuccess())
                inconsistencies.add(new XOWLInconsistency(inconsistencyRuleId, inconsistencyMessage, ((XSPReplyResult<XOWLConsistencyRule>) reply).getData(), null));
        }
        inconsistenciesTimestamp = System.nanoTime();
        return new XSPReplyResultCollection<>(inconsistencies);
    }

    @Override
    public XSPReply getRule(String identifier) {
        long now = System.nanoTime();
        long elapsed = now - rulesTimestamp;
        if (elapsed < CACHE_INVALIDATION_TIMEOUT) {
            for (XOWLConsistencyRule rule : rules) {
                if (rule.getIdentifier().equals(identifier))
                    return new XSPReplyResult<>(rule);
            }
            return new XSPReplyFailure("Not found");
        }

        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        XSPReply reply = live.execute("LIST RULES");
        if (!reply.isSuccess())
            return reply;
        Collection<String> ruleNames = ((XSPReplyResultCollection<String>) reply).getData();
        reply = live.execute("LIST ACTIVE RULES");
        if (!reply.isSuccess())
            return reply;
        Collection<String> activeRuleNames = ((XSPReplyResultCollection<String>) reply).getData();
        Result result = live.sparql("SELECT DISTINCT ?r ?n ?d WHERE { GRAPH <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) +
                "> { ?r a <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_RULE) +
                "> . ?r <" +
                IOUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n . ?r <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_DEFINITION) +
                "> ?d } }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        ResultSolutions solutions = (ResultSolutions) result;
        XOWLConsistencyRule target = null;
        rules.clear();
        for (QuerySolution solution : solutions.getSolutions()) {
            String ruleId = ((IRINode) solution.get("r")).getIRIValue();
            String ruleName = ((LiteralNode) solution.get("n")).getLexicalValue();
            String ruleDefinition = ((LiteralNode) solution.get("d")).getLexicalValue();
            if (!ruleNames.contains(ruleId))
                continue;
            XOWLConsistencyRule rule = new XOWLConsistencyRule(ruleId, ruleName, activeRuleNames.contains(ruleId), ruleDefinition);
            if (ruleId.equals(identifier))
                target = rule;
            rules.add(rule);
        }
        rulesTimestamp = System.nanoTime();
        if (target == null)
            return new XSPReplyFailure("Not found");
        return new XSPReplyResult<>(target);
    }

    @Override
    public XSPReply createRule(String name, String message, String prefixes, String conditions) {
        String id = IRI_RULE_BASE + "#" + Utils.encode(name);
        String definition = prefixes + " rule <" + IOUtils.escapeAbsoluteURIW3C(id) + "> distinct {\n" + conditions + "\n} => {\n" +
                "?e <" + IOUtils.escapeAbsoluteURIW3C(Vocabulary.rdfType) + "> <" + IOUtils.escapeAbsoluteURIW3C(IRI_INCONSISTENCY) + ">\n" +
                "?e <" + IOUtils.escapeAbsoluteURIW3C(IRI_MESSAGE) + "> \"" + IOUtils.escapeStringW3C(message) + "\"\n" +
                "?e <" + IOUtils.escapeAbsoluteURIW3C(IRI_DEFINITION) + "> <" + IOUtils.escapeAbsoluteURIW3C(id) + ">\n" +
                "}";
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        XSPReply reply = live.execute("ADD RULE " + definition);
        if (!reply.isSuccess())
            return reply;
        Result result = live.sparql("INSERT DATA { GRAPH <" + IOUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) + "> {" +
                "<" + IOUtils.escapeAbsoluteURIW3C(id) + "> <" + IOUtils.escapeAbsoluteURIW3C(Vocabulary.rdfType) + "> <" + IOUtils.escapeAbsoluteURIW3C(IRI_RULE) + "> ." +
                "<" + IOUtils.escapeAbsoluteURIW3C(id) + "> <" + IOUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) + "> \"" + IOUtils.escapeStringW3C(name) + "\" ." +
                "<" + IOUtils.escapeAbsoluteURIW3C(id) + "> <" + IOUtils.escapeAbsoluteURIW3C(IRI_DEFINITION) + "> \"" + IOUtils.escapeStringW3C(definition) + "\" ." +
                "} }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        XOWLConsistencyRule rule = new XOWLConsistencyRule(id, name, false, definition);
        rules.add(rule);
        return new XSPReplyResult<>(rule);
    }

    @Override
    public XSPReply activateRule(String identifier) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        return live.execute("ACTIVATE " + identifier);
    }

    @Override
    public XSPReply activateRule(ConsistencyRule rule) {
        return activateRule(rule.getIdentifier());
    }

    @Override
    public XSPReply deactivateRule(String identifier) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        return live.execute("DEACTIVATE " + identifier);
    }

    @Override
    public XSPReply deactivateRule(ConsistencyRule rule) {
        return deactivateRule(rule.getIdentifier());
    }

    @Override
    public XSPReply deleteRule(String identifier) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        XSPReply reply = live.execute("REMOVE RULE " + identifier);
        if (!reply.isSuccess())
            return reply;
        Result result = live.sparql("DELETE WHERE { GRAPH <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) +
                "> { <" +
                IOUtils.escapeAbsoluteURIW3C(identifier) +
                "> ?p ?o } }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply deleteRule(ConsistencyRule rule) {
        return deleteRule(rule.getIdentifier());
    }
}
