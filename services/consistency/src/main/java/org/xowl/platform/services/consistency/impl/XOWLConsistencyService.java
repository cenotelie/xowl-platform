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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.api.XOWLRule;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.IRIs;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.store.loaders.RDFLoaderResult;
import org.xowl.infra.store.loaders.xRDFLoader;
import org.xowl.infra.store.rdf.*;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultFailure;
import org.xowl.infra.store.sparql.ResultQuads;
import org.xowl.infra.store.sparql.ResultSolutions;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.SHA1;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricSnapshot;
import org.xowl.infra.utils.metrics.MetricSnapshotInt;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.consistency.*;
import org.xowl.platform.services.storage.StorageService;
import org.xowl.platform.services.storage.TripleStore;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Implements a consistency service for the xOWL platform
 *
 * @author Laurent Wouters
 */
public class XOWLConsistencyService implements ConsistencyService, HttpApiService {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLConsistencyService.class, "/org/xowl/platform/services/consistency/api_service_consistency.raml", "Consistency Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLConsistencyService.class, "/org/xowl/platform/services/consistency/api_service_consistency.html", "Consistency Service - Documentation", HttpApiResource.MIME_HTML);
    /**
     * The resource for the API's schema
     */
    private static final HttpApiResource RESOURCE_SCHEMA = new HttpApiResourceBase(XOWLConsistencyService.class, "/org/xowl/platform/services/consistency/schema_platform_consistency.json", "Consistency Service - Schema", HttpConstants.MIME_JSON);


    /**
     * The URI of the schema for the consistency concepts
     */
    private static final String IRI_SCHEMA = "http://xowl.org/platform/services/consistency";
    /**
     * The URI of the graph for metadata on the consistency elements
     */
    private static final String IRI_GRAPH_METADATA = IRI_SCHEMA + "/metadata";
    /**
     * The URI for the concept of reasoning rule
     */
    private static final String IRI_CONCEPT_REASONING_RULE = IRI_SCHEMA + "#ReasoningRule";
    /**
     * The URI for the concept of consistency constraint rule
     */
    private static final String IRI_CONCEPT_CONSISTENCY_CONSTRAINT = IRI_SCHEMA + "#ConsistencyConstraint";
    /**
     * The URI for the concept of reasoning inconsistency
     */
    private static final String IRI_CONCEPT_INCONSISTENCY = IRI_SCHEMA + "#Inconsistency";
    /**
     * The URI for the concept of message
     */
    private static final String IRI_CONCEPT_MESSAGE = IRI_SCHEMA + "#message";
    /**
     * The URI for the concept of producedBy
     */
    private static final String IRI_CONCEPT_PRODUCED_BY = IRI_SCHEMA + "#producedBy";
    /**
     * The URI for the concept of antecedent
     */
    private static final String IRI_CONCEPT_ANTECEDENT = IRI_SCHEMA + "#antecedent_";
    /**
     * The base URI for a consistency constraint
     */
    private static final String IRI_PREFIX_CONSISTENCY_CONSTRAINT = IRI_SCHEMA + "/constraint";
    /**
     * The base URI for an inconsistency
     */
    private static final String IRI_PREFIX_INCONSISTENCY = IRI_SCHEMA + "/inconsistency";


    /**
     * The URI for the API services
     */
    private final String apiUri;

    /**
     * Initializes this service
     */
    public XOWLConsistencyService() {
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/services/consistency";
    }

    @Override
    public String getIdentifier() {
        return XOWLConsistencyService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Consistency Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public boolean requireAuth(HttpApiRequest request) {
        return true;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (request.getUri().equals(apiUri + "/inconsistencies"))
            return handleInconsistencies(request);
        if (request.getUri().startsWith(apiUri + "/rules"))
            return handleRules(request);
        if (request.getUri().startsWith(apiUri + "/constraints"))
            return handleConstraints(request);
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Handles the requests for the inconsistencies resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleInconsistencies(HttpApiRequest request) {
        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
        Reply reply = getInconsistencies();
        if (!reply.isSuccess())
            return ReplyUtils.toHttpResponse(reply, null);
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Inconsistency inconsistency : ((ReplyResultCollection<Inconsistency>) reply).getData()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(inconsistency.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Handles the requests for the rules resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleRules(HttpApiRequest request) {
        if (request.getUri().equals(apiUri + "/rules")) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    Reply reply = getReasoningRules();
                    if (!reply.isSuccess())
                        return ReplyUtils.toHttpResponse(reply, null);
                    StringBuilder builder = new StringBuilder("[");
                    boolean first = true;
                    for (ReasoningRule rule : ((ReplyResultCollection<ReasoningRule>) reply).getData()) {
                        if (!first)
                            builder.append(", ");
                        first = false;
                        builder.append(rule.serializedJSON());
                    }
                    builder.append("]");
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
                }
                case HttpConstants.METHOD_PUT: {
                    String name = request.getParameter("name");
                    if (name == null)
                        return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                    String definition = new String(request.getContent(), IOUtils.CHARSET);
                    if (definition.isEmpty())
                        return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                    Reply reply = createReasoningRule(name, definition);
                    if (!reply.isSuccess())
                        return ReplyUtils.toHttpResponse(reply, null);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, ((ReplyResult<ReasoningRule>) reply).getData().serializedJSON());
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        }
        if (request.getUri().startsWith(apiUri + "/rules")) {
            String rest = request.getUri().substring(apiUri.length() + "/rules".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String ruleId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                switch (request.getMethod()) {
                    case HttpConstants.METHOD_GET: {
                        Reply reply = getReasoningRule(ruleId);
                        if (!reply.isSuccess())
                            return ReplyUtils.toHttpResponse(reply, null);
                        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, ((ReplyResult<ReasoningRule>) reply).getData().serializedJSON());
                    }
                    case HttpConstants.METHOD_PUT: {
                        String content = new String(request.getContent(), IOUtils.CHARSET);
                        if (content.isEmpty())
                            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                        ASTNode definition = Json.parse(Logging.get(), content);
                        if (definition == null)
                            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_CONTENT_PARSING_FAILED), null);
                        ReasoningRule rule = new XOWLReasoningRule(definition);
                        if (!ruleId.equals(rule.getIdentifier()))
                            return ReplyUtils.toHttpResponse(ReplyNotFound.instance(), null);
                        return ReplyUtils.toHttpResponse(addReasoningRule(rule), null);
                    }
                    case HttpConstants.METHOD_DELETE: {
                        Reply reply = deleteReasoningRule(ruleId);
                        return ReplyUtils.toHttpResponse(reply, null);
                    }
                }
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
            } else {
                switch (rest.substring(index)) {
                    case "/activate": {
                        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                        Reply reply = activateReasoningRule(ruleId);
                        return ReplyUtils.toHttpResponse(reply, null);
                    }
                    case "/deactivate": {
                        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                        Reply reply = deactivateReasoningRule(ruleId);
                        return ReplyUtils.toHttpResponse(reply, null);
                    }
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Handles the requests for the constraints resource
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleConstraints(HttpApiRequest request) {
        if (request.getUri().equals(apiUri + "/constraints")) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET: {
                    Reply reply = getConsistencyConstraints();
                    if (!reply.isSuccess())
                        return ReplyUtils.toHttpResponse(reply, null);
                    StringBuilder builder = new StringBuilder("[");
                    boolean first = true;
                    for (ConsistencyConstraint constraint : ((ReplyResultCollection<ConsistencyConstraint>) reply).getData()) {
                        if (!first)
                            builder.append(", ");
                        first = false;
                        builder.append(constraint.serializedJSON());
                    }
                    builder.append("]");
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
                }
                case HttpConstants.METHOD_PUT: {
                    String name = request.getParameter("name");
                    if (name == null)
                        return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'name'"), null);
                    String message = request.getParameter("message");
                    if (message == null)
                        return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'message'"), null);
                    String prefixes = request.getParameter("prefixes");
                    if (prefixes == null)
                        return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'prefixes'"), null);
                    String antecedents = request.getParameter("antecedents");
                    if (antecedents == null)
                        return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'antecedents'"), null);
                    String guard = request.getParameter("guard");
                    Reply reply = createConsistencyConstraint(name, message, prefixes, antecedents, guard);
                    if (!reply.isSuccess())
                        return ReplyUtils.toHttpResponse(reply, null);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, ((ReplyResult<ConsistencyConstraint>) reply).getData().serializedJSON());
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        }
        if (request.getUri().startsWith(apiUri + "/constraints")) {
            String rest = request.getUri().substring(apiUri.length() + "/constraints".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String constraintId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                switch (request.getMethod()) {
                    case HttpConstants.METHOD_GET: {
                        Reply reply = getConsistencyConstraint(constraintId);
                        if (!reply.isSuccess())
                            return ReplyUtils.toHttpResponse(reply, null);
                        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, ((ReplyResult<ConsistencyConstraint>) reply).getData().serializedJSON());
                    }
                    case HttpConstants.METHOD_PUT: {
                        String content = new String(request.getContent(), IOUtils.CHARSET);
                        if (content.isEmpty())
                            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                        ASTNode definition = Json.parse(Logging.get(), content);
                        if (definition == null)
                            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_CONTENT_PARSING_FAILED), null);
                        ConsistencyConstraint constraint = new XOWLConsistencyConstraint(definition);
                        if (!constraintId.equals(constraint.getIdentifier()))
                            return ReplyUtils.toHttpResponse(ReplyNotFound.instance(), null);
                        return ReplyUtils.toHttpResponse(addConsistencyConstraint(constraint), null);
                    }
                    case HttpConstants.METHOD_DELETE: {
                        Reply reply = deleteConsistencyConstraint(constraintId);
                        return ReplyUtils.toHttpResponse(reply, null);
                    }
                }
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT, DELETE");
            } else {
                switch (rest.substring(index)) {
                    case "/activate": {
                        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                        Reply reply = activateConsistencyConstraint(constraintId);
                        return ReplyUtils.toHttpResponse(reply, null);
                    }
                    case "/deactivate": {
                        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                        Reply reply = deactivateConsistencyConstraint(constraintId);
                        return ReplyUtils.toHttpResponse(reply, null);
                    }
                }
            }
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Override
    public HttpApiResource getApiSpecification() {
        return RESOURCE_SPECIFICATION;
    }

    @Override
    public HttpApiResource getApiDocumentation() {
        return RESOURCE_DOCUMENTATION;
    }

    @Override
    public HttpApiResource[] getApiOtherResources() {
        return new HttpApiResource[]{RESOURCE_SCHEMA};
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(HttpApiService.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"specification\": " +
                RESOURCE_SPECIFICATION.serializedJSON() +
                ", \"documentation\": " +
                RESOURCE_DOCUMENTATION.serializedJSON() +
                "}";
    }

    @Override
    public Reply getInconsistencies() {
        // get all the consistency constraints
        Reply reply = getConsistencyConstraints();
        if (!reply.isSuccess())
            return reply;
        Collection<XOWLConsistencyConstraint> constraints = ((ReplyResultCollection<XOWLConsistencyConstraint>) reply).getData();

        // query for inconsistencies
        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();
        reply = live.sparql("DESCRIBE ?i WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRIs.GRAPH_INFERENCE) +
                "> { ?i a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_INCONSISTENCY) +
                "> } }", null, null);
        if (!reply.isSuccess())
            return reply;
        Result sparqlResult = ((ReplyResult<Result>) reply).getData();
        if (sparqlResult.isFailure())
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, ((ResultFailure) sparqlResult).getMessage());
        Collection<Quad> quads = ((ResultQuads) sparqlResult).getQuads();
        Map<SubjectNode, Collection<Quad>> map = PlatformUtils.mapBySubject(quads);

        // create the inconsistencies
        Collection<XOWLInconsistency> inconsistencies = new ArrayList<>();
        for (Map.Entry<SubjectNode, Collection<Quad>> entry : map.entrySet()) {
            String constraintId = null;
            String message = null;
            Map<String, Node> antecedents = new HashMap<>();
            for (Quad quad : entry.getValue()) {
                if (IRI_CONCEPT_PRODUCED_BY.equals(((IRINode) quad.getProperty()).getIRIValue())) {
                    constraintId = ((IRINode) quad.getObject()).getIRIValue();
                } else if (IRI_CONCEPT_MESSAGE.equals(((IRINode) quad.getProperty()).getIRIValue())) {
                    message = ((LiteralNode) quad.getObject()).getLexicalValue();
                } else if (((IRINode) quad.getProperty()).getIRIValue().startsWith(IRI_CONCEPT_ANTECEDENT)) {
                    String name = ((IRINode) quad.getProperty()).getIRIValue().substring(IRI_CONCEPT_ANTECEDENT.length());
                    antecedents.put(name, quad.getObject());
                }
            }
            XOWLConsistencyConstraint constraint = null;
            for (XOWLConsistencyConstraint potential : constraints) {
                if (potential.getIdentifier().equals(constraintId)) {
                    constraint = potential;
                    break;
                }
            }
            if (constraint != null)
                inconsistencies.add(new XOWLInconsistency(IRI_PREFIX_INCONSISTENCY + UUID.randomUUID().toString(), message, constraint, antecedents));
        }
        return new ReplyResultCollection<>(inconsistencies);
    }

    @Override
    public Reply getReasoningRules() {
        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();

        // query the database for reasoning rules metadata
        Reply reply = live.sparql("SELECT DISTINCT ?r ?n WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_GRAPH_METADATA) +
                "> { ?r a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_REASONING_RULE) +
                "> . ?r <" +
                TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n } }", null, null);
        if (!reply.isSuccess())
            return reply;
        Result sparqlResult = ((ReplyResult<Result>) reply).getData();
        if (sparqlResult.isFailure())
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, ((ResultFailure) sparqlResult).getMessage());
        ResultSolutions solutions = (ResultSolutions) sparqlResult;

        // tries to map to rules in the database
        Collection<XOWLReasoningRule> result = new ArrayList<>();
        for (RDFPatternSolution solution : solutions.getSolutions()) {
            String ruleId = ((IRINode) solution.get("r")).getIRIValue();
            String ruleName = ((LiteralNode) solution.get("n")).getLexicalValue();
            reply = live.getRule(ruleId);
            if (!reply.isSuccess())
                return reply;
            result.add(new XOWLReasoningRule(((ReplyResult<XOWLRule>) reply).getData(), ruleName));
        }
        return new ReplyResultCollection<>(result);
    }

    @Override
    public Reply getReasoningRule(String identifier) {
        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();

        // query the database for reasoning rules metadata
        Reply reply = live.sparql("SELECT DISTINCT ?n WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_GRAPH_METADATA) +
                "> { <" +
                TextUtils.escapeAbsoluteURIW3C(identifier) +
                "> a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_REASONING_RULE) +
                "> . <" +
                TextUtils.escapeAbsoluteURIW3C(identifier) +
                "> <" +
                TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n } }", null, null);
        if (!reply.isSuccess())
            return reply;
        Result sparqlResult = ((ReplyResult<Result>) reply).getData();
        if (sparqlResult.isFailure())
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, ((ResultFailure) sparqlResult).getMessage());
        ResultSolutions solutions = (ResultSolutions) sparqlResult;
        if (solutions.getSolutions().size() == 0)
            return ReplyNotFound.instance();

        // tries to map to a rule in the database
        RDFPatternSolution solution = solutions.getSolutions().iterator().next();
        String ruleName = ((LiteralNode) solution.get("n")).getLexicalValue();
        reply = live.getRule(identifier);
        if (!reply.isSuccess())
            return reply;
        XOWLReasoningRule rule = new XOWLReasoningRule(((ReplyResult<XOWLRule>) reply).getData(), ruleName);
        return new ReplyResult<>(rule);
    }

    @Override
    public Reply createReasoningRule(String name, String definition) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_CREATE_REASONING_RULE);
        if (!reply.isSuccess())
            return reply;

        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();

        // create the new rule in the database
        reply = live.addRule(definition, false);
        if (!reply.isSuccess())
            return reply;
        XOWLRule original = ((ReplyResult<XOWLRule>) reply).getData();

        // insert the metadata
        reply = live.sparql("INSERT DATA { GRAPH <" + TextUtils.escapeAbsoluteURIW3C(IRI_GRAPH_METADATA) + "> {" +
                "<" + TextUtils.escapeAbsoluteURIW3C(original.getIdentifier()) + "> <" + TextUtils.escapeAbsoluteURIW3C(Vocabulary.rdfType) + "> <" + TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_REASONING_RULE) + "> ." +
                "<" + TextUtils.escapeAbsoluteURIW3C(original.getIdentifier()) + "> <" + TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) + "> \"" + TextUtils.escapeStringW3C(name) + "\" ." +
                "} }", null, null);
        if (!reply.isSuccess())
            return reply;
        XOWLReasoningRule rule = new XOWLReasoningRule(original, name);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ReasoningRuleCreatedEvent(rule, this));
        return new ReplyResult<>(rule);
    }

    @Override
    public Reply addReasoningRule(ReasoningRule rule) {
        return createReasoningRule(rule.getName(), rule.getDefinition());
    }

    @Override
    public Reply activateReasoningRule(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_ACTIVATE_REASONING_RULE);
        if (!reply.isSuccess())
            return reply;

        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();
        return live.activateRule(identifier);
    }

    @Override
    public Reply activateReasoningRule(ReasoningRule rule) {
        return activateReasoningRule(rule.getIdentifier());
    }

    @Override
    public Reply deactivateReasoningRule(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_DEACTIVATE_REASONING_RULE);
        if (!reply.isSuccess())
            return reply;

        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();
        return live.deactivateRule(identifier);
    }

    @Override
    public Reply deactivateReasoningRule(ReasoningRule rule) {
        return deactivateReasoningRule(rule.getIdentifier());
    }

    @Override
    public Reply deleteReasoningRule(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_DELETE_REASONING_RULE);
        if (!reply.isSuccess())
            return reply;

        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();

        reply = live.sparql("DELETE WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_GRAPH_METADATA) +
                "> { <" +
                TextUtils.escapeAbsoluteURIW3C(identifier) +
                "> ?p ?o } }", null, null);
        if (!reply.isSuccess())
            return reply;

        return live.removeRule(identifier);
    }

    @Override
    public Reply deleteReasoningRule(ReasoningRule rule) {
        return deleteReasoningRule(rule.getIdentifier());
    }

    @Override
    public Reply getConsistencyConstraints() {
        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();

        // query the database for consistency constraint metadata
        Reply reply = live.sparql("SELECT DISTINCT ?r ?n WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_GRAPH_METADATA) +
                "> { ?r a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_CONSISTENCY_CONSTRAINT) +
                "> . ?r <" +
                TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n } }", null, null);
        if (!reply.isSuccess())
            return reply;
        Result sparqlResult = ((ReplyResult<Result>) reply).getData();
        if (sparqlResult.isFailure())
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, ((ResultFailure) sparqlResult).getMessage());
        ResultSolutions solutions = (ResultSolutions) sparqlResult;

        // tries to map to rules in the database
        Collection<XOWLConsistencyConstraint> result = new ArrayList<>();
        for (RDFPatternSolution solution : solutions.getSolutions()) {
            String constraintId = ((IRINode) solution.get("r")).getIRIValue();
            String constraintName = ((LiteralNode) solution.get("n")).getLexicalValue();
            reply = live.getRule(constraintId);
            if (!reply.isSuccess())
                return reply;
            result.add(new XOWLConsistencyConstraint(((ReplyResult<XOWLRule>) reply).getData(), constraintName));
        }
        return new ReplyResultCollection<>(result);
    }

    @Override
    public Reply getConsistencyConstraint(String identifier) {
        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();

        // query the database for consistency constraints metadata
        Reply reply = live.sparql("SELECT DISTINCT ?n WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_GRAPH_METADATA) +
                "> { <" +
                TextUtils.escapeAbsoluteURIW3C(identifier) +
                "> a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_CONSISTENCY_CONSTRAINT) +
                "> . <" +
                TextUtils.escapeAbsoluteURIW3C(identifier) +
                "> <" +
                TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n } }", null, null);
        if (!reply.isSuccess())
            return reply;
        Result sparqlResult = ((ReplyResult<Result>) reply).getData();
        if (sparqlResult.isFailure())
            return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED, ((ResultFailure) sparqlResult).getMessage());
        ResultSolutions solutions = (ResultSolutions) sparqlResult;
        if (solutions.getSolutions().size() == 0)
            return ReplyNotFound.instance();

        // tries to map to a rule in the database
        RDFPatternSolution solution = solutions.getSolutions().iterator().next();
        String ruleName = ((LiteralNode) solution.get("n")).getLexicalValue();
        reply = live.getRule(identifier);
        if (!reply.isSuccess())
            return reply;
        XOWLConsistencyConstraint rule = new XOWLConsistencyConstraint(((ReplyResult<XOWLRule>) reply).getData(), ruleName);
        return new ReplyResult<>(rule);
    }

    @Override
    public Reply createConsistencyConstraint(String name, String message, String prefixes, String antecedents, String guard) {
        // find the antecedents in the specified conditions
        String constraintIRI = IRI_PREFIX_CONSISTENCY_CONSTRAINT + "#" + SHA1.hashSHA1(name);
        String definition = prefixes + "\nRULE <" + TextUtils.escapeAbsoluteURIW3C(constraintIRI) + "> {\n" + antecedents + "\n} => {}";
        BufferedLogger logger = new BufferedLogger();
        xRDFLoader loader = new xRDFLoader();
        RDFLoaderResult rdfResult = loader.loadRDF(logger, new StringReader(definition), IRI_GRAPH_METADATA, IRI_GRAPH_METADATA);
        if (!logger.getErrorMessages().isEmpty())
            return new ReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString());
        if (rdfResult == null || rdfResult.getRules().isEmpty())
            return new ReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString());
        Collection<VariableNode> variables = rdfResult.getRules().get(0).getAntecedentVariables();

        if (guard != null)
            guard = guard.trim();
        // build the full definition of the constraint
        StringBuilder builder = new StringBuilder(prefixes);
        builder.append(IOUtils.LINE_SEPARATOR);
        builder.append("RULE DISTINCT <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(constraintIRI));
        builder.append("> {");
        builder.append(IOUtils.LINE_SEPARATOR);
        builder.append(antecedents);
        builder.append(IOUtils.LINE_SEPARATOR);
        builder.append("}");
        if (guard != null && !guard.isEmpty()) {
            builder.append(IOUtils.LINE_SEPARATOR);
            builder.append("WITH ");
            builder.append(guard);
            builder.append(IOUtils.LINE_SEPARATOR);
        } else {
            builder.append(" ");
        }
        builder.append("=> {");
        builder.append(IOUtils.LINE_SEPARATOR);
        builder.append("    ?e <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(Vocabulary.rdfType));
        builder.append("> <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_INCONSISTENCY));
        builder.append("> .");
        builder.append(IOUtils.LINE_SEPARATOR);
        builder.append("    ?e <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_MESSAGE));
        builder.append("> \"");
        builder.append(TextUtils.escapeStringW3C(message));
        builder.append("\" .");
        builder.append(IOUtils.LINE_SEPARATOR);
        builder.append("    ?e <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_PRODUCED_BY));
        builder.append("> <");
        builder.append(TextUtils.escapeAbsoluteURIW3C(constraintIRI));
        builder.append("> .");
        builder.append(IOUtils.LINE_SEPARATOR);
        for (VariableNode variable : variables) {
            builder.append("    ?e <");
            builder.append(TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_ANTECEDENT + variable.getName()));
            builder.append("> ?");
            builder.append(variable.getName());
            builder.append(" .");
            builder.append(IOUtils.LINE_SEPARATOR);
        }
        builder.append("}");
        return createConsistencyConstraint(name, builder.toString());
    }

    /**
     * Creates a consistency constraint
     *
     * @param name       The constraint's name
     * @param definition The constraint's definition
     * @return The operation result
     */
    private Reply createConsistencyConstraint(String name, String definition) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_CREATE_CONSISTENCY_CONSTRAINT);
        if (!reply.isSuccess())
            return reply;

        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();

        // create the new rule in the database
        reply = live.addRule(definition, false);
        if (!reply.isSuccess())
            return reply;
        XOWLRule original = ((ReplyResult<XOWLRule>) reply).getData();

        // insert the metadata
        reply = live.sparql("INSERT DATA { GRAPH <" + TextUtils.escapeAbsoluteURIW3C(IRI_GRAPH_METADATA) + "> {" +
                "<" + TextUtils.escapeAbsoluteURIW3C(original.getIdentifier()) + "> <" + TextUtils.escapeAbsoluteURIW3C(Vocabulary.rdfType) + "> <" + TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_CONSISTENCY_CONSTRAINT) + "> ." +
                "<" + TextUtils.escapeAbsoluteURIW3C(original.getIdentifier()) + "> <" + TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) + "> \"" + TextUtils.escapeStringW3C(name) + "\" ." +
                "} }", null, null);
        if (!reply.isSuccess())
            return reply;
        XOWLConsistencyConstraint constraint = new XOWLConsistencyConstraint(original, name);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new ConsistencyConstraintCreatedEvent(constraint, this));
        return new ReplyResult<>(constraint);
    }

    @Override
    public Reply addConsistencyConstraint(ConsistencyConstraint constraint) {
        return createConsistencyConstraint(constraint.getName(), constraint.getDefinition());
    }

    @Override
    public Reply activateConsistencyConstraint(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_ACTIVATE_CONSISTENCY_CONSTRAINT);
        if (!reply.isSuccess())
            return reply;

        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();
        return live.activateRule(identifier);
    }

    @Override
    public Reply activateConsistencyConstraint(ConsistencyConstraint constraint) {
        return activateConsistencyConstraint(constraint.getIdentifier());
    }

    @Override
    public Reply deactivateConsistencyConstraint(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_DEACTIVATE_CONSISTENCY_CONSTRAINT);
        if (!reply.isSuccess())
            return reply;

        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();
        return live.deactivateRule(identifier);
    }

    @Override
    public Reply deactivateConsistencyConstraint(ConsistencyConstraint constraint) {
        return deactivateConsistencyConstraint(constraint.getIdentifier());
    }

    @Override
    public Reply deleteConsistencyConstraint(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ACTION_DELETE_CONSISTENCY_CONSTRAINT);
        if (!reply.isSuccess())
            return reply;

        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return ReplyServiceUnavailable.instance();
        TripleStore live = storageService.getLiveStore();

        reply = live.sparql("DELETE WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_GRAPH_METADATA) +
                "> { <" +
                TextUtils.escapeAbsoluteURIW3C(identifier) +
                "> ?p ?o } }", null, null);
        if (!reply.isSuccess())
            return reply;

        return live.removeRule(identifier);
    }

    @Override
    public Reply deleteConsistencyConstraint(ConsistencyConstraint constraint) {
        return deleteConsistencyConstraint(constraint.getIdentifier());
    }

    /**
     * Gets the number of inconsistencies
     *
     * @return The number of inconsistencies
     */
    private int getInconsistenciesCount() {
        StorageService storageService = Register.getComponent(StorageService.class);
        if (storageService == null)
            return -1;
        TripleStore live = storageService.getLiveStore();
        Reply reply = live.sparql("SELECT (COUNT(?i) AS ?c) WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(IRIs.GRAPH_INFERENCE) +
                "> { ?i a <" +
                TextUtils.escapeAbsoluteURIW3C(IRI_CONCEPT_INCONSISTENCY) +
                "> } }", null, null);
        if (!reply.isSuccess())
            return -1;
        Result sparqlResult = ((ReplyResult<Result>) reply).getData();
        if (sparqlResult.isFailure())
            return -1;
        RDFPatternSolution solution = ((ResultSolutions) sparqlResult).getSolutions().iterator().next();
        return Integer.parseInt(((LiteralNode) solution.get("c")).getLexicalValue());
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
