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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.platform.services.evaluation.*;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * The implementation of the evaluation service for the xOWL platform
 *
 * @author Laurent Wouters
 */
public class XOWLEvaluationService implements EvaluationService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/core/evaluation/evaluations",
            "services/core/evaluation/evaluation",
            "services/core/evaluation/evaluableTypes",
            "services/core/evaluation/evaluables",
            "services/core/evaluation/criterionTypes",
            "services/core/evaluation/service"
    };

    /**
     * The registered evaluable types
     */
    private final Map<String, EvaluableType> evaluableTypes;
    /**
     * The registered criterion types
     */
    private final Map<String, CriterionType> criterionTypes;

    /**
     * Initializes this service
     */
    public XOWLEvaluationService() {
        this.evaluableTypes = new HashMap<>();
        this.criterionTypes = new HashMap<>();
    }

    @Override
    public String getIdentifier() {
        return XOWLEvaluationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Evaluation Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (method.equals("GET")) {
            switch (uri) {
                case "services/core/evaluation/evaluableTypes":
                    return onGetEvaluableTypes();
                case "services/core/evaluation/evaluables":
                    return onGetEvaluables(parameters);
                case "services/core/evaluation/criterionTypes":
                    return onGetCriterionTypes(parameters);
                case "services/core/evaluation/evaluations":
                    return onGetEvaluations();
                case "services/core/evaluation/evaluation":
                    return onGetEvaluation(parameters);
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } else if (method.equals("POST")) {
            if (uri.equals("services/core/evaluation/service"))
                return onPostEvaluation(content);
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD);
    }

    @Override
    public void register(EvaluableType evaluableType) {
        evaluableTypes.put(evaluableType.getIdentifier(), evaluableType);
    }

    @Override
    public void register(CriterionType criterionType) {
        criterionTypes.put(criterionType.getIdentifier(), criterionType);
    }

    @Override
    public Collection<EvaluableType> getEvaluableTypes() {
        return Collections.unmodifiableCollection(evaluableTypes.values());
    }

    @Override
    public Collection<CriterionType> getCriterionTypes() {
        return Collections.unmodifiableCollection(criterionTypes.values());
    }

    @Override
    public EvaluableType getEvaluableType(String typeId) {
        return evaluableTypes.get(typeId);
    }

    @Override
    public CriterionType getCriterionType(String typeId) {
        return criterionTypes.get(typeId);
    }

    @Override
    public Collection<CriterionType> getCriterionTypes(EvaluableType evaluableType) {
        Collection<CriterionType> result = new ArrayList<>(criterionTypes.size());
        for (CriterionType criterionType : criterionTypes.values()) {
            if (criterionType.supports(evaluableType))
                result.add(criterionType);
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public XSPReply getEvaluations() {
        return XOWLEvaluation.retrieveAll();
    }

    @Override
    public XSPReply getEvaluation(String evalId) {
        return XOWLEvaluation.retrieve(this, evalId);
    }

    @Override
    public XSPReply newEvaluation(String name, EvaluableType evaluableType, Collection<Evaluable> evaluables, Collection<Criterion> criteria) {
        XOWLEvaluation evaluation = new XOWLEvaluation(null, name, evaluableType, evaluables, criteria);
        XSPReply reply = evaluation.store();
        if (!reply.isSuccess())
            return reply;
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
        for (EvaluableType evaluableType : evaluableTypes.values()) {
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
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onGetEvaluables(Map<String, String[]> parameters) {
        String[] types = parameters.get("type");
        if (types == null || types.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected 'type' parameter");
        EvaluableType evaluableType = evaluableTypes.get(types[0]);
        if (evaluableType == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return XSPReplyUtils.toHttpResponse(evaluableType.getElements(), null);
    }

    /**
     * Responds to a request for the criterion types applicable to an evaluable type
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onGetCriterionTypes(Map<String, String[]> parameters) {
        String[] types = parameters.get("for");
        if (types == null || types.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected 'for' parameter");
        EvaluableType evaluableType = evaluableTypes.get(types[0]);
        if (evaluableType == null)
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, "[]");
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
        return XSPReplyUtils.toHttpResponse(getEvaluations(), null);
    }

    /**
     * Responds to a request for a particular evaluation
     *
     * @param parameters The request parameters
     * @return The response
     */
    private HttpResponse onGetEvaluation(Map<String, String[]> parameters) {
        String[] ids = parameters.get("id");
        if (ids == null || ids.length == 0)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected 'id' parameter");
        return XSPReplyUtils.toHttpResponse(getEvaluation(ids[0]), null);
    }

    /**
     * Responds to the request to launch a new evaluation
     *
     * @param content The posted content
     * @return The response
     */
    private HttpResponse onPostEvaluation(byte[] content) {
        if (content == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, "Expected content");
        BufferedLogger logger = new BufferedLogger();
        ASTNode root = IOUtils.parseJSON(logger, new String(content, Files.CHARSET));
        if (root == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, logger.getErrorsAsString());
        XOWLEvaluation evaluation = new XOWLEvaluation(root, this);
        XSPReply reply = evaluation.store();
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        XOWLEvaluationReference reference = new XOWLEvaluationReference(evaluation.getIdentifier(), evaluation.getName());
        return XSPReplyUtils.toHttpResponse(new XSPReplyResult<>(reference), null);
    }
}
