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

package org.xowl.platform.services.evaluation.impl;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.evaluation.*;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * The implementation of the evaluation service for the xOWL platform
 *
 * @author Laurent Wouters
 */
public class XOWLEvaluationService implements EvaluationService, HttpApiService {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLEvaluationService.class, "/org/xowl/platform/services/evaluation/api_service_evaluation.raml", "Evaluation Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLEvaluationService.class, "/org/xowl/platform/services/evaluation/api_service_evaluation.html", "Evaluation Service - Documentation", HttpApiResource.MIME_HTML);
    /**
     * The resource for the API's schema
     */
    private static final HttpApiResource RESOURCE_SCHEMA = new HttpApiResourceBase(XOWLEvaluationService.class, "/org/xowl/platform/services/evaluation/schema_platform_evaluation.json", "Evaluation Service - Schema", HttpConstants.MIME_JSON);


    /**
     * The URI for the API services
     */
    private final String apiUri;

    /**
     * Initializes this service
     */
    public XOWLEvaluationService() {
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/services/evaluation";
    }

    @Override
    public String getIdentifier() {
        return XOWLEvaluationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Evaluation Service";
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
        if (request.getUri().equals(apiUri + "/evaluableTypes")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return onGetEvaluableTypes();
        }
        if (request.getUri().equals(apiUri + "/evaluables")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return onGetEvaluables(request);
        }
        if (request.getUri().equals(apiUri + "/criterionTypes")) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            return onGetCriterionTypes(request);
        }
        if (request.getUri().equals(apiUri + "/evaluations")) {
            switch (request.getMethod()) {
                case HttpConstants.METHOD_GET:
                    return onGetEvaluations();
                case HttpConstants.METHOD_PUT:
                    return onPutEvaluation(request);
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        }
        if (request.getUri().startsWith(apiUri + "/evaluations")) {
            String rest = request.getUri().substring(apiUri.length() + "/evaluations".length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String evalId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                return onGetEvaluation(evalId);
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
    public Collection<EvaluableType> getEvaluableTypes() {
        return Register.getComponents(EvaluableType.class);
    }

    @Override
    public Collection<CriterionType> getCriterionTypes() {
        return Register.getComponents(CriterionType.class);
    }

    @Override
    public EvaluableType getEvaluableType(String typeId) {
        for (EvaluableType evaluableType : Register.getComponents(EvaluableType.class)) {
            if (evaluableType.getIdentifier().equals(typeId))
                return evaluableType;
        }
        return null;
    }

    @Override
    public CriterionType getCriterionType(String typeId) {
        for (CriterionType criterionType : Register.getComponents(CriterionType.class)) {
            if (criterionType.getIdentifier().equals(typeId))
                return criterionType;
        }
        return null;
    }

    @Override
    public Collection<CriterionType> getCriterionTypes(EvaluableType evaluableType) {
        Collection<CriterionType> result = new ArrayList<>();
        for (CriterionType criterionType : Register.getComponents(CriterionType.class)) {
            if (criterionType.supports(evaluableType))
                result.add(criterionType);
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public XSPReply getEvaluations() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_EVALUATIONS);
        if (!reply.isSuccess())
            return reply;
        return XOWLEvaluation.retrieveAll();
    }

    @Override
    public XSPReply getEvaluation(String evalId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_EVALUATIONS);
        if (!reply.isSuccess())
            return reply;
        return XOWLEvaluation.retrieve(this, evalId);
    }

    @Override
    public XSPReply newEvaluation(String name, EvaluableType evaluableType, Collection<Evaluable> evaluables, Collection<Criterion> criteria) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_NEW_EVALUATION);
        if (!reply.isSuccess())
            return reply;
        XOWLEvaluation evaluation = new XOWLEvaluation(null, name, evaluableType, evaluables, criteria);
        reply = evaluation.store();
        if (!reply.isSuccess())
            return reply;
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new EvaluationCreatedEvent(evaluation, this));
        return new XSPReplyResult<>(evaluation);
    }

    /**
     * Responds to a request for the evaluable types
     *
     * @return The response
     */
    private HttpResponse onGetEvaluableTypes() {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (EvaluableType evaluableType : getEvaluableTypes()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(evaluableType.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for the evaluables of a given type
     *
     * @param request The request to handle
     * @return The response
     */
    private HttpResponse onGetEvaluables(HttpApiRequest request) {
        String type = request.getParameter("type");
        if (type == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'type'"), null);
        EvaluableType evaluableType = getEvaluableType(type);
        if (evaluableType == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        XSPReply reply = evaluableType.getElements();
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Evaluable evaluable : ((XSPReplyResultCollection<Evaluable>) reply).getData()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(evaluable.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for the criterion types applicable to an evaluable type
     *
     * @param request The request to handle
     * @return The response
     */
    private HttpResponse onGetCriterionTypes(HttpApiRequest request) {
        String type = request.getParameter("for");
        if (type == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_EXPECTED_QUERY_PARAMETERS, "'for'"), null);
        EvaluableType evaluableType = getEvaluableType(type);
        if (evaluableType == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (CriterionType criterionType : getCriterionTypes(evaluableType)) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(criterionType.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for the current evaluations
     *
     * @return The response
     */
    private HttpResponse onGetEvaluations() {
        XSPReply reply = getEvaluations();
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (EvaluationReference evaluation : ((XSPReplyResultCollection<EvaluationReference>) reply).getData()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(evaluation.serializedJSON());
        }
        builder.append("]");
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
    }

    /**
     * Responds to a request for a particular evaluation
     *
     * @param evaluationIdentifier The identifier of the requested evlauation
     * @return The response
     */
    private HttpResponse onGetEvaluation(String evaluationIdentifier) {
        XSPReply reply = getEvaluation(evaluationIdentifier);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, ((XSPReplyResult<Evaluation>) reply).getData().serializedJSON());
    }

    /**
     * Responds to the request to launch a new evaluation
     *
     * @param request The request to handle
     * @return The response
     */
    private HttpResponse onPutEvaluation(HttpApiRequest request) {
        byte[] content = request.getContent();
        if (content == null || content.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);

        BufferedLogger logger = new BufferedLogger();
        ASTNode root = JsonLoader.parseJson(logger, new String(content, IOUtils.CHARSET));
        if (root == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = securityService.checkAction(ACTION_NEW_EVALUATION);
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        XOWLEvaluation evaluation = new XOWLEvaluation(root, this);
        reply = evaluation.store();
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new EvaluationCreatedEvent(evaluation, this));
        XOWLEvaluationReference reference = new XOWLEvaluationReference(evaluation.getIdentifier(), evaluation.getName());
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, reference.serializedJSON());
    }
}
