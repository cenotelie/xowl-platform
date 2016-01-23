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
import org.xowl.store.loaders.RDFLoaderResult;
import org.xowl.store.loaders.RDFTLoader;
import org.xowl.store.rdf.*;
import org.xowl.store.sparql.Result;
import org.xowl.store.sparql.ResultFailure;
import org.xowl.store.sparql.ResultQuads;
import org.xowl.store.sparql.ResultSolutions;
import org.xowl.store.storage.cache.CachedNodes;
import org.xowl.store.xsp.*;
import org.xowl.utils.logging.BufferedLogger;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.*;

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
            "consistency",
            "inconsistencies"
    };

    /**
     * The URI of the graph for metadata on the consistency rules
     */
    private static final String IRI_RULE_METADATA = "http://xowl.org/platform/consistency/metadata";
    /**
     * IRI of the schema for inconsistencies
     */
    private static final String IRI_SCHEMA = "http://xowl.org/platform/schemas/consistency";
    /**
     * The URI for the concept of rule
     */
    private static final String IRI_RULE = IRI_SCHEMA + "#Rule";
    /**
     * The URI for the concept of inconsistency
     */
    private static final String IRI_INCONSISTENCY = IRI_SCHEMA + "#Inconsistency";
    /**
     * The URI for the concept of definition
     */
    private static final String IRI_DEFINITION = IRI_SCHEMA + "#definition";
    /**
     * The URI for the concept of message
     */
    private static final String IRI_MESSAGE = IRI_SCHEMA + "#message";
    /**
     * The URI for the concept of antecedent
     */
    private static final String IRI_ANTECEDENT = IRI_SCHEMA + "/antecedent#";
    /**
     * The base URI for a consistency rule
     */
    private static final String IRI_RULE_BASE = IRI_SCHEMA + "/rule";

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
        this.rulesTimestamp = System.nanoTime();
        this.inconsistencies = new ArrayList<>(150);
        this.inconsistenciesTimestamp = rulesTimestamp;
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
        if (uri.equals(URI_API + "/inconsistencies"))
            return XSPReplyUtils.toHttpResponse(getInconsistencies(), Collections.singletonList(accept));
        if (!uri.equals(URI_API + "/consistency"))
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        if ("GET".equals(method)) {
            String[] ids = parameters.get("id");
            if (ids != null && ids.length > 0)
                return XSPReplyUtils.toHttpResponse(getRule(ids[0]), Collections.singletonList(accept));
            return XSPReplyUtils.toHttpResponse(getRules(), Collections.singletonList(accept));
        }
        String[] actions = parameters.get("action");
        String[] ids = parameters.get("id");
        if (actions == null || actions.length == 0)
            return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        switch (actions[0]) {
            case "create":
                String[] names = parameters.get("name");
                String[] messages = parameters.get("message");
                String[] prefixes = parameters.get("prefixes");
                String[] conditions = parameters.get("conditions");
                if (names == null || names.length == 0 || messages == null || messages.length == 0 || prefixes == null || prefixes.length == 0 || conditions == null || conditions.length == 0)
                    return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(createRule(names[0], messages[0], prefixes[0], conditions[0]), Collections.singletonList(accept));
            case "activate":
                if (ids == null || ids.length == 0)
                    return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(activateRule(ids[0]), Collections.singletonList(accept));
            case "deactivate":
                if (ids == null || ids.length == 0)
                    return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(deactivateRule(ids[0]), Collections.singletonList(accept));
            case "delete":
                if (ids == null || ids.length == 0)
                    return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(deleteRule(ids[0]), Collections.singletonList(accept));
        }
        return new IOUtils.HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
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
        Result result = live.sparql("DESCRIBE ?i WHERE { GRAPH <" +
                IOUtils.escapeAbsoluteURIW3C(IRIs.GRAPH_INFERENCE) +
                "> { ?i a <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_INCONSISTENCY) +
                "> } }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        Collection<Quad> quads = ((ResultQuads) result).getQuads();
        Map<SubjectNode, Collection<Quad>> map = Utils.mapBySubject(quads);
        inconsistencies.clear();
        for (Map.Entry<SubjectNode, Collection<Quad>> entry : map.entrySet()) {
            String ruleId = null;
            String msg = null;
            Map<String, Node> antecedents = new HashMap<>();
            for (Quad quad : entry.getValue()) {
                if (IRI_DEFINITION.equals(((IRINode) quad.getProperty()).getIRIValue())) {
                    ruleId = ((IRINode) quad.getObject()).getIRIValue();
                } else if (IRI_MESSAGE.equals(((IRINode) quad.getProperty()).getIRIValue())) {
                    msg = ((LiteralNode) quad.getObject()).getLexicalValue();
                } else if (((IRINode) quad.getProperty()).getIRIValue().startsWith(IRI_ANTECEDENT)) {
                    String name = ((IRINode) quad.getProperty()).getIRIValue().substring(IRI_ANTECEDENT.length());
                    antecedents.put(name, quad.getObject());
                }
            }
            XSPReply reply = getRule(ruleId);
            if (reply.isSuccess())
                inconsistencies.add(new XOWLInconsistency(IRI_SCHEMA + "/inconsistency#" + UUID.randomUUID().toString(), msg, ((XSPReplyResult<XOWLConsistencyRule>) reply).getData(), antecedents));
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
            ruleDefinition = IOUtils.unescape(ruleDefinition);
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
        String definition = prefixes + " rule <" + IOUtils.escapeAbsoluteURIW3C(id) + "> distinct {\n" + conditions + "\n} => {}";
        BufferedLogger logger = new BufferedLogger();
        RDFTLoader loader = new RDFTLoader(new CachedNodes());
        RDFLoaderResult rdfResult = loader.loadRDF(logger, new StringReader(definition), IRI_RULE_METADATA, IRI_RULE_METADATA);
        if (!logger.getErrorMessages().isEmpty())
            return new XSPReplyFailure(Utils.getLog(logger));
        if (rdfResult == null || rdfResult.getRules().isEmpty())
            return new XSPReplyFailure("Failed to load the rule");
        Collection<String> variables = getVariablesIn(rdfResult.getRules().get(0));
        StringBuilder builder = new StringBuilder(prefixes);
        builder.append(" rule <");
        builder.append(IOUtils.escapeAbsoluteURIW3C(id));
        builder.append("> distinct {\n");
        builder.append(conditions);
        builder.append("\n} => {\n");
        builder.append("?e <");
        builder.append(IOUtils.escapeAbsoluteURIW3C(Vocabulary.rdfType));
        builder.append("> <");
        builder.append(IOUtils.escapeAbsoluteURIW3C(IRI_INCONSISTENCY));
        builder.append(">\n");
        builder.append("?e <");
        builder.append(IOUtils.escapeAbsoluteURIW3C(IRI_MESSAGE));
        builder.append("> \"");
        builder.append(IOUtils.escapeStringW3C(message));
        builder.append("\"\n");
        builder.append("?e <");
        builder.append(IOUtils.escapeAbsoluteURIW3C(IRI_DEFINITION));
        builder.append("> <");
        builder.append(IOUtils.escapeAbsoluteURIW3C(id));
        builder.append(">\n");
        for (String var : variables) {
            builder.append("?e <");
            builder.append(IOUtils.escapeAbsoluteURIW3C(IRI_ANTECEDENT + var));
            builder.append("> ?");
            builder.append(var);
            builder.append("\n");
        }
        builder.append("}");
        definition = builder.toString();

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

    /**
     * Gets all the variables used on the antecedents of a rule
     *
     * @param rule The rule
     * @return The variables
     */
    private static Collection<String> getVariablesIn(Rule rule) {
        Collection<String> result = new ArrayList<>(10);
        for (Quad quad : rule.getAntecedentSourcePositives())
            getVariablesIn(result, quad);
        for (Quad quad : rule.getAntecedentMetaPositives())
            getVariablesIn(result, quad);
        return result;
    }

    /**
     * Gets all the variables used in a quad
     *
     * @param buffer The buffer of variable names
     * @param quad   The quad
     */
    private static void getVariablesIn(Collection<String> buffer, Quad quad) {
        getVariablesIn(buffer, quad.getSubject());
        getVariablesIn(buffer, quad.getProperty());
        getVariablesIn(buffer, quad.getObject());
    }

    /**
     * Gets the variable for the RDF node
     *
     * @param buffer The buffer of variable names
     * @param node   The node
     */
    private static void getVariablesIn(Collection<String> buffer, Node node) {
        if (node.getNodeType() == Node.TYPE_VARIABLE) {
            String name = ((VariableNode) node).getName();
            if (!buffer.contains(name))
                buffer.add(name);
        }
    }
}
