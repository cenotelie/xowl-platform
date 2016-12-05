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

package org.xowl.platform.services.consistency.impl;

import org.xowl.infra.server.api.XOWLRule;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.IRIs;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.loaders.RDFLoaderResult;
import org.xowl.infra.store.loaders.RDFTLoader;
import org.xowl.infra.store.rdf.*;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultFailure;
import org.xowl.infra.store.sparql.ResultQuads;
import org.xowl.infra.store.sparql.ResultSolutions;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.SHA1;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.collections.Couple;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricBase;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.infra.utils.metrics.MetricSnapshotInt;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.consistency.*;
import org.xowl.platform.services.storage.TripleStore;
import org.xowl.platform.services.storage.TripleStoreService;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements a consistency service for the xOWL platform
 *
 * @author Laurent Wouters
 */
public class XOWLConsistencyService implements ConsistencyService, MeasurableService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/services/consistency";

    /**
     * The URI of the schema for the consistency concepts
     */
    private static final String IRI_SCHEMA = "http://xowl.org/platform/services/consistency";
    /**
     * The URI of the graph for metadata on the consistency rules
     */
    private static final String IRI_RULE_METADATA = IRI_SCHEMA + "/metadata";
    /**
     * The base URI for a consistency rule
     */
    private static final String IRI_RULE_BASE = IRI_SCHEMA + "/rule";
    /**
     * The base URI for a consistency rule
     */
    private static final String IRI_INCONSISTENCY_BASE = IRI_SCHEMA + "/inconsistency";
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
     * The URI for the concept of producedBy
     */
    private static final String IRI_PRODUCED_BY = IRI_SCHEMA + "#producedBy";
    /**
     * The URI for the concept of antecedent
     */
    private static final String IRI_ANTECEDENT = IRI_SCHEMA + "#antecedent_";

    /**
     * The inconsistency count metric
     */
    private static final Metric METRIC_INCONSISTENCY_COUNT = new MetricBase(XOWLConsistencyService.class.getCanonicalName() + ".InconsistencyCount",
            "Consistency Service - Inconsistency count",
            "inconsistencies",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));

    /**
     * Initializes this service
     */
    public XOWLConsistencyService() {
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
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        if (uri.equals("services/core/inconsistencies"))
            return XSPReplyUtils.toHttpResponse(getInconsistencies(), Collections.singletonList(accept));
        if (!uri.equals("services/core/consistency"))
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
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        XSPReply reply = live.getRules();
        if (!reply.isSuccess())
            return reply;
        Collection<XOWLRule> rules = new ArrayList<>(((XSPReplyResultCollection<XOWLRule>) reply).getData());
        Result sparqlResult = live.sparql("SELECT DISTINCT ?r ?n ?d WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) +
                "> { ?r a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_RULE) +
                "> . ?r <" +
                TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n . ?r <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_DEFINITION) +
                "> ?d } }");
        if (!sparqlResult.isSuccess())
            return new XSPReplyFailure(((ResultFailure) sparqlResult).getMessage());
        ResultSolutions solutions = (ResultSolutions) sparqlResult;
        Collection<XOWLConsistencyRule> result = new ArrayList<>();
        for (RDFPatternSolution solution : solutions.getSolutions()) {
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
        XSPReply reply = getRules();
        if (!reply.isSuccess())
            return reply;
        Collection<XOWLConsistencyRule> rules = ((XSPReplyResultCollection<XOWLConsistencyRule>) reply).getData();

        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        Result result = live.sparql("DESCRIBE ?i WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRIs.GRAPH_INFERENCE) +
                "> { ?i a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_INCONSISTENCY) +
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
                if (IRI_PRODUCED_BY.equals(((IRINode) quad.getProperty()).getIRIValue())) {
                    ruleId = ((IRINode) quad.getObject()).getIRIValue();
                } else if (IRI_MESSAGE.equals(((IRINode) quad.getProperty()).getIRIValue())) {
                    msg = ((LiteralNode) quad.getObject()).getLexicalValue();
                } else if (((IRINode) quad.getProperty()).getIRIValue().startsWith(IRI_ANTECEDENT)) {
                    String name = ((IRINode) quad.getProperty()).getIRIValue().substring(IRI_ANTECEDENT.length());
                    antecedents.put(name, quad.getObject());
                }
            }
            XOWLConsistencyRule rule = null;
            for (XOWLConsistencyRule potential : rules) {
                if (potential.getIdentifier().equals(ruleId)) {
                    rule = potential;
                    break;
                }
            }
            if (rule != null)
                inconsistencies.add(new XOWLInconsistency(IRI_INCONSISTENCY_BASE + UUID.randomUUID().toString(), msg, rule, antecedents));
        }
        return new XSPReplyResultCollection<>(inconsistencies);
    }

    /**
     * Gets the number of inconsistencies
     *
     * @return The number of inconsistencies
     */
    private int getInconsistenciesCount() {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return -1;
        TripleStore live = lts.getLiveStore();
        Result result = live.sparql("SELECT (COUNT(?i) AS ?c) WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRIs.GRAPH_INFERENCE) +
                "> { ?i a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_INCONSISTENCY) +
                "> } }");
        if (!result.isSuccess())
            return -1;
        RDFPatternSolution solution = ((ResultSolutions) result).getSolutions().iterator().next();
        return Integer.parseInt(((LiteralNode) solution.get("c")).getLexicalValue());
    }

    @Override
    public XSPReply getRule(String identifier) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        XSPReply reply = live.getRule(identifier);
        if (!reply.isSuccess())
            return reply;
        XOWLRule original = ((XSPReplyResult<XOWLRule>) reply).getData();
        Result sparqlResult = live.sparql("SELECT DISTINCT ?n WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) +
                "> { <" +
                TextUtils.escapeAbsoluteURIW3C(identifier) +
                "> a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_RULE) +
                "> . <" +
                TextUtils.escapeAbsoluteURIW3C(identifier) +
                "> <" +
                TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n . } }");
        if (!sparqlResult.isSuccess())
            return new XSPReplyFailure(((ResultFailure) sparqlResult).getMessage());
        ResultSolutions solutions = (ResultSolutions) sparqlResult;
        if (solutions.getSolutions().size() == 0)
            return XSPReplyNotFound.instance();
        RDFPatternSolution solution = solutions.getSolutions().iterator().next();
        String ruleName = ((LiteralNode) solution.get("n")).getLexicalValue();
        return new XSPReplyResult<>(new XOWLConsistencyRule(original, ruleName));
    }

    @Override
    public XSPReply createRule(String name, String message, String prefixes, String conditions) {
        String id = IRI_RULE_BASE + "#" + SHA1.hashSHA1(name);
        String definition = prefixes + " rule distinct <" + TextUtils.escapeAbsoluteURIW3C(id) + "> {\n" + conditions + "\n} => {}";
        BufferedLogger logger = new BufferedLogger();
        RDFTLoader loader = new RDFTLoader(new CachedNodes());
        RDFLoaderResult rdfResult = loader.loadRDF(logger, new StringReader(definition), IRI_RULE_METADATA, IRI_RULE_METADATA);
        if (!logger.getErrorMessages().isEmpty())
            return new XSPReplyFailure(logger.getErrorsAsString());
        if (rdfResult == null || rdfResult.getRules().isEmpty())
            return new XSPReplyFailure("Failed to load the rule");
        Collection<VariableNode> variables = rdfResult.getRules().get(0).getAntecedentVariables();
        StringBuilder builder = new StringBuilder(prefixes);
        builder.append(" rule distinct <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(id));
        builder.append("> {\n");
        builder.append(conditions);
        builder.append("\n} => {\n");
        builder.append("?e <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(Vocabulary.rdfType));
        builder.append("> <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(IRI_INCONSISTENCY));
        builder.append(">.\n");
        builder.append("?e <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(IRI_MESSAGE));
        builder.append("> \"");
        builder.append(TextUtils.escapeStringW3C(message));
        builder.append("\".\n");
        builder.append("?e <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(IRI_PRODUCED_BY));
        builder.append("> <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(id));
        builder.append(">.\n");
        for (VariableNode variable : variables) {
            builder.append("?e <");
            builder.append(TextUtils.escapeAbsoluteURIW3C(IRI_ANTECEDENT + variable.getName()));
            builder.append("> ?");
            builder.append(variable.getName());
            builder.append(".\n");
        }
        builder.append("}");
        definition = builder.toString();

        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        XSPReply reply = live.addRule(definition, false);
        if (!reply.isSuccess())
            return reply;
        XOWLRule original = ((XSPReplyResult<XOWLRule>) reply).getData();
        Result result = live.sparql("INSERT DATA { GRAPH <" + TextUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) + "> {" +
                "<" + TextUtils.escapeAbsoluteURIW3C(id) + "> <" + TextUtils.escapeAbsoluteURIW3C(Vocabulary.rdfType) + "> <" + TextUtils.escapeAbsoluteURIW3C(IRI_RULE) + "> ." +
                "<" + TextUtils.escapeAbsoluteURIW3C(id) + "> <" + TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) + "> \"" + TextUtils.escapeStringW3C(name) + "\" ." +
                "<" + TextUtils.escapeAbsoluteURIW3C(id) + "> <" + TextUtils.escapeAbsoluteURIW3C(IRI_DEFINITION) + "> \"" + TextUtils.escapeStringW3C(definition) + "\" ." +
                "} }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        XOWLConsistencyRule rule = new XOWLConsistencyRule(original, name);
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConsistencyRuleCreatedEvent(rule, this));
        return new XSPReplyResult<>(rule);
    }

    @Override
    public XSPReply activateRule(String identifier) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        XSPReply reply = getRule(identifier);
        if (!reply.isSuccess())
            return reply;
        XOWLConsistencyRule rule = ((XSPReplyResult<XOWLConsistencyRule>) reply).getData();
        reply = live.activateRule(rule);
        if (!reply.isSuccess())
            return reply;
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConsistencyRuleActivatedEvent(rule, this));
        return reply;
    }

    @Override
    public XSPReply activateRule(ConsistencyRule rule) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        XSPReply reply = live.activateRule(rule);
        if (!reply.isSuccess())
            return reply;
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConsistencyRuleActivatedEvent(rule, this));
        return reply;
    }

    @Override
    public XSPReply deactivateRule(String identifier) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        XSPReply reply = getRule(identifier);
        if (!reply.isSuccess())
            return reply;
        XOWLConsistencyRule rule = ((XSPReplyResult<XOWLConsistencyRule>) reply).getData();
        reply = live.deactivateRule(rule);
        if (!reply.isSuccess())
            return reply;
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConsistencyRuleDeactivatedEvent(rule, this));
        return reply;
    }

    @Override
    public XSPReply deactivateRule(ConsistencyRule rule) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        XSPReply reply = live.deactivateRule(rule);
        if (!reply.isSuccess())
            return reply;
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConsistencyRuleDeactivatedEvent(rule, this));
        return reply;
    }

    @Override
    public XSPReply deleteRule(String identifier) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        XSPReply reply = getRule(identifier);
        if (!reply.isSuccess())
            return reply;
        XOWLConsistencyRule rule = ((XSPReplyResult<XOWLConsistencyRule>) reply).getData();
        reply = live.removeRule(rule);
        if (!reply.isSuccess())
            return reply;
        Result result = live.sparql("DELETE WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) +
                "> { <" +
                TextUtils.escapeAbsoluteURIW3C(identifier) +
                "> ?p ?o } }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConsistencyRuleDeletedEvent(rule, this));
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply deleteRule(ConsistencyRule rule) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        TripleStore live = lts.getLiveStore();
        XSPReply reply = live.removeRule(rule);
        if (!reply.isSuccess())
            return reply;
        Result result = live.sparql("DELETE WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_RULE_METADATA) +
                "> { <" +
                TextUtils.escapeAbsoluteURIW3C(rule.getIdentifier()) +
                "> ?p ?o } }");
        if (!result.isSuccess())
            return new XSPReplyFailure(((ResultFailure) result).getMessage());
        EventService eventService = ServiceUtils.getService(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConsistencyRuleDeletedEvent(rule, this));
        return XSPReplySuccess.instance();
    }

    @Override
    public Collection<Metric> getMetrics() {
        return Collections.singletonList(METRIC_INCONSISTENCY_COUNT);
    }

    @Override
    public MetricSnapshot pollMetric(Metric metric) {
        if (metric != METRIC_INCONSISTENCY_COUNT)
            return null;
        int count = getInconsistenciesCount();
        return new MetricSnapshotInt(count);
    }
}
