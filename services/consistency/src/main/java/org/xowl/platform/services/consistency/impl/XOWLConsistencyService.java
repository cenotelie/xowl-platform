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

import org.xowl.infra.server.api.XOWLRule;
import org.xowl.infra.server.api.base.BaseRule;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.IRIs;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.store.loaders.RDFLoaderResult;
import org.xowl.infra.store.loaders.RDFTLoader;
import org.xowl.infra.store.rdf.*;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultFailure;
import org.xowl.infra.store.sparql.ResultQuads;
import org.xowl.infra.store.sparql.ResultSolutions;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.consistency.ConsistencyRule;
import org.xowl.platform.services.consistency.ConsistencyService;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;
import org.xowl.platform.kernel.PlatformUtils;

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
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (uri.equals(URI_API + "/inconsistencies"))
            return XSPReplyUtils.toHttpResponse(getInconsistencies(), Collections.singletonList(accept));
        if (!uri.equals(URI_API + "/consistency"))
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        if ("GET".equals(method)) {
            String[] ids = parameters.get("id");
            if (ids != null && ids.length > 0)
                return XSPReplyUtils.toHttpResponse(getRule(ids[0]), Collections.singletonList(accept));
            return XSPReplyUtils.toHttpResponse(getRules(), Collections.singletonList(accept));
        }
        String[] actions = parameters.get("action");
        String[] ids = parameters.get("id");
        if (actions == null || actions.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        switch (actions[0]) {
            case "create":
                String[] names = parameters.get("name");
                String[] messages = parameters.get("message");
                String[] prefixes = parameters.get("prefixes");
                String[] conditions = parameters.get("conditions");
                if (names == null || names.length == 0 || messages == null || messages.length == 0 || prefixes == null || prefixes.length == 0 || conditions == null || conditions.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(createRule(names[0], messages[0], prefixes[0], conditions[0]), Collections.singletonList(accept));
            case "activate":
                if (ids == null || ids.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(activateRule(ids[0]), Collections.singletonList(accept));
            case "deactivate":
                if (ids == null || ids.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(deactivateRule(ids[0]), Collections.singletonList(accept));
            case "delete":
                if (ids == null || ids.length == 0)
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
                return XSPReplyUtils.toHttpResponse(deleteRule(ids[0]), Collections.singletonList(accept));
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Override
    public XSPReply getRules() {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        XSPReply reply = live.getRules();
        if (!reply.isSuccess())
            return reply;
        Collection<XOWLRule> rules = new ArrayList<>(((XSPReplyResultCollection<XOWLRule>) reply).getData());
        Result sparqlResult = live.sparql("SELECT DISTINCT ?r ?n ?d WHERE { GRAPH <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) +
                "> { ?r a <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_RULE) +
                "> . ?r <" +
                IOUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n . ?r <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_DEFINITION) +
                "> ?d } }");
        if (!sparqlResult.isSuccess())
            return new XSPReplyFailure(((ResultFailure) sparqlResult).getMessage());
        ResultSolutions solutions = (ResultSolutions) sparqlResult;
        Collection<XOWLConsistencyRule> result = new ArrayList<>();
        for (QuerySolution solution : solutions.getSolutions()) {
            String ruleId = ((IRINode) solution.get("r")).getIRIValue();
            String ruleName = ((LiteralNode) solution.get("n")).getLexicalValue();
            for (XOWLRule rule : rules) {
                if (rule.getName().equals(ruleId)) {
                    result.add(new XOWLConsistencyRule(rule, ruleName));
                    rules.remove(rule);
                    break;
                }
            }
        }
        return new XSPReplyResultCollection<>(result);
    }

    @Override
    public XSPReply getInconsistencies() {
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
        Map<SubjectNode, Collection<Quad>> map = PlatformUtils.mapBySubject(quads);
        Collection<XOWLInconsistency> inconsistencies = new ArrayList<>();
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
        return new XSPReplyResultCollection<>(inconsistencies);
    }

    @Override
    public XSPReply getRule(String identifier) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return new XSPReplyFailure("Failed to retrieve the LTS service");
        TripleStore live = lts.getLiveStore();
        if (live == null)
            return new XSPReplyFailure("Failed to resolve the live store");
        XSPReply reply = live.getRule(identifier);
        if (!reply.isSuccess())
            return reply;
        XOWLRule original = ((XSPReplyResult<XOWLRule>) reply).getData();
        Result sparqlResult = live.sparql("SELECT DISTINCT ?n WHERE { GRAPH <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) +
                "> { <" +
                IOUtils.escapeAbsoluteURIW3C(identifier) +
                "> a <" +
                IOUtils.escapeAbsoluteURIW3C(IRI_RULE) +
                "> . <" +
                IOUtils.escapeAbsoluteURIW3C(identifier) +
                "> <" +
                IOUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n . } }");
        if (!sparqlResult.isSuccess())
            return new XSPReplyFailure(((ResultFailure) sparqlResult).getMessage());
        ResultSolutions solutions = (ResultSolutions) sparqlResult;
        if (solutions.getSolutions().size() == 0)
            return XSPReplyNotFound.instance();
        QuerySolution solution = solutions.getSolutions().iterator().next();
        String ruleName = ((LiteralNode) solution.get("n")).getLexicalValue();
        return new XSPReplyResult<>(new XOWLConsistencyRule(original, ruleName));
    }

    @Override
    public XSPReply createRule(String name, String message, String prefixes, String conditions) {
        String id = IRI_RULE_BASE + "#" + PlatformUtils.encode(name);
        String definition = prefixes + " rule <" + IOUtils.escapeAbsoluteURIW3C(id) + "> distinct {\n" + conditions + "\n} => {}";
        BufferedLogger logger = new BufferedLogger();
        RDFTLoader loader = new RDFTLoader(new CachedNodes());
        RDFLoaderResult rdfResult = loader.loadRDF(logger, new StringReader(definition), IRI_RULE_METADATA, IRI_RULE_METADATA);
        if (!logger.getErrorMessages().isEmpty())
            return new XSPReplyFailure(PlatformUtils.getLog(logger));
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
        XSPReply reply = live.addRule(definition, false);
        if (!reply.isSuccess())
            return reply;
        XOWLRule original = ((XSPReplyResult<XOWLRule>) reply).getData();
        Result result = live.sparql("INSERT DATA { GRAPH <" + IOUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) + "> {" +
                "<" + IOUtils.escapeAbsoluteURIW3C(id) + "> <" + IOUtils.escapeAbsoluteURIW3C(Vocabulary.rdfType) + "> <" + IOUtils.escapeAbsoluteURIW3C(IRI_RULE) + "> ." +
                "<" + IOUtils.escapeAbsoluteURIW3C(id) + "> <" + IOUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) + "> \"" + IOUtils.escapeStringW3C(name) + "\" ." +
                "<" + IOUtils.escapeAbsoluteURIW3C(id) + "> <" + IOUtils.escapeAbsoluteURIW3C(IRI_DEFINITION) + "> \"" + IOUtils.escapeStringW3C(definition) + "\" ." +
                "} }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        XOWLConsistencyRule rule = new XOWLConsistencyRule(original, name);
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
        return live.activateRule(new BaseRule(identifier, null, false));
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
        return live.deactivateRule(new BaseRule(identifier, null, false));
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
        XSPReply reply = live.removeRule(new BaseRule(identifier, null, false));
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
