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
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.*;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultFailure;
import org.xowl.infra.store.sparql.ResultSolutions;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactSimple;
import org.xowl.platform.services.evaluation.*;
import org.xowl.platform.services.lts.TripleStoreService;

import java.text.DateFormat;
import java.util.*;

/**
 * Implements an evaluation
 *
 * @author Laurent Wouters
 */
class XOWLEvaluation implements Evaluation {
    /**
     * The base URI for evaluation data
     */
    private static final String EVAL_URI = KernelSchema.URI_BASE + "/evaluation";
    /**
     * The URI for the concept of evaluation
     */
    private static final String EVALUATION = EVAL_URI + "/Evaluation";

    /**
     * The evaluation's identifier
     */
    private final String identifier;
    /**
     * The evaluation's name
     */
    private final String name;
    /**
     * The type of the evaluable elements in this evaluation
     */
    private final EvaluableType evaluableType;
    /**
     * The elements to evaluate
     */
    private final Collection<Evaluable> evaluables;
    /**
     * The selected criteria
     */
    private final Collection<Criterion> criteria;

    /**
     * Initializes an empty evaluation
     *
     * @param identifier    The evaluation's identifier
     * @param name          The evaluation's name
     * @param evaluableType The type of the evaluable elements in this evaluation
     * @param evaluables    The elements to evaluate
     * @param criteria      The selected criteria
     */
    public XOWLEvaluation(String identifier, String name, EvaluableType evaluableType, Collection<Evaluable> evaluables, Collection<Criterion> criteria) {
        this.identifier = identifier != null ? identifier : (EVALUATION + "#" + UUID.randomUUID());
        this.name = name;
        this.evaluableType = evaluableType;
        this.evaluables = new ArrayList<>(evaluables);
        this.criteria = new ArrayList<>(criteria);
    }

    /**
     * Initializes this evaluation from a JSON definition
     *
     * @param definition The JSON definition
     * @param service    The parent service
     */
    public XOWLEvaluation(ASTNode definition, XOWLEvaluationService service) {
        String identifier = null;
        String name = null;
        EvaluableType evaluableType = null;
        this.evaluables = new ArrayList<>();
        this.criteria = new ArrayList<>();
        for (ASTNode member : definition.getChildren()) {
            String memberName = TextUtils.unescape(member.getChildren().get(0).getValue());
            memberName = memberName.substring(1, memberName.length() - 1);
            switch (memberName) {
                case "identifier":
                    identifier = TextUtils.unescape(member.getChildren().get(1).getValue());
                    identifier = identifier.substring(1, identifier.length() - 1);
                    break;
                case "name":
                    name = TextUtils.unescape(member.getChildren().get(1).getValue());
                    name = name.substring(1, name.length() - 1);
                    break;
                case "evaluableType":
                    String id = TextUtils.unescape(member.getChildren().get(1).getValue());
                    id = id.substring(1, id.length() - 1);
                    evaluableType = service.getEvaluableType(id);
                    break;
                case "evaluables":
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        if (evaluableType != null)
                            evaluables.add(evaluableType.getElement(loadParameters(child)));
                    }
                    break;
                case "criteria":
                    for (ASTNode child : member.getChildren().get(1).getChildren()) {
                        Criterion criterion = loadCriterion(service, child);
                        if (criterion != null)
                            criteria.add(criterion);
                    }
                    break;
            }
        }
        this.identifier = identifier != null ? identifier : (EVAL_URI + "/Evaluation#" + UUID.randomUUID());
        this.name = name != null ? name : "Anonymous Evaluation";
        this.evaluableType = evaluableType;
    }

    /**
     * Loads a map of parameters form a JSON node
     *
     * @param node The JSON node
     * @return The parameters
     */
    private Map<String, String> loadParameters(ASTNode node) {
        Map<String, String> parameters = new HashMap<>();
        for (ASTNode member : node.getChildren()) {
            String paramName = TextUtils.unescape(member.getChildren().get(0).getValue());
            paramName = paramName.substring(1, paramName.length() - 1);
            String paramValue = TextUtils.unescape(member.getChildren().get(1).getValue());
            paramValue = paramValue.substring(1, paramValue.length() - 1);
            parameters.put(paramName, paramValue);
        }
        return parameters;
    }

    /**
     * Loads a criterion for a JSON node
     *
     * @param service    The parent service
     * @param definition The JSON definition
     * @return The criterion
     */
    private Criterion loadCriterion(XOWLEvaluationService service, ASTNode definition) {
        String typeId = null;
        Map<String, String> parameters = new HashMap<>();
        for (ASTNode member : definition.getChildren()) {
            String memberName = TextUtils.unescape(member.getChildren().get(0).getValue());
            memberName = memberName.substring(1, memberName.length() - 1);
            switch (memberName) {
                case "typeId":
                    typeId = TextUtils.unescape(member.getChildren().get(1).getValue());
                    typeId = typeId.substring(1, typeId.length() - 1);
                    break;
                case "parameters":
                    parameters = loadParameters(member.getChildren().get(1));
                    break;
            }
        }
        CriterionType criterionType = service.getCriterionType(typeId);
        if (criterionType == null)
            return null;
        return criterionType.getCriterion(parameters);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<Evaluable> getEvaluables() {
        return Collections.unmodifiableCollection(evaluables);
    }

    @Override
    public Collection<Criterion> getCriteria() {
        return Collections.unmodifiableCollection(criteria);
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(Evaluation.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"evaluableType\": \"");
        builder.append(TextUtils.escapeStringJSON(evaluableType.getIdentifier()));
        builder.append("\", \"evaluables\": [");
        boolean first = true;
        for (Evaluable evaluable : evaluables) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(evaluable.serializedJSON());
        }
        builder.append("], \"criteria\": [");
        first = true;
        for (Criterion criterion : criteria) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(criterion.serializedJSON());
        }
        builder.append("], \"results\": [");
        first = true;
        for (Evaluable evaluable : evaluables) {
            for (Criterion criterion : criteria) {
                CriterionResult result = criterion.getResultFor(evaluable);
                if (result == null)
                    continue;
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append("{\"evaluable\": \"");
                builder.append(TextUtils.escapeStringJSON(evaluable.getIdentifier()));
                builder.append("\", \"criterion\": \"");
                builder.append(TextUtils.escapeStringJSON(criterion.getIdentifier()));
                builder.append("\", \"data\": ");
                builder.append(result.serializedJSON());
                builder.append("}");
            }
        }
        builder.append("]}");
        return builder.toString();
    }

    /**
     * Stores this evaluation
     *
     * @return The operation's result
     */
    public XSPReply store() {
        if (evaluableType == null)
            return new XSPReplyFailure("Invalid evaluation: evaluable type not specified");
        NodeManager nodes = new CachedNodes();
        IRINode graphEval = nodes.getIRINode(identifier);
        IRINode registry = nodes.getIRINode(KernelSchema.GRAPH_ARTIFACTS);
        List<Quad> metadata = new ArrayList<>();
        metadata.add(new Quad(registry, graphEval, nodes.getIRINode(Vocabulary.rdfType), nodes.getIRINode(KernelSchema.ARTIFACT)));
        metadata.add(new Quad(registry, graphEval, nodes.getIRINode(Vocabulary.rdfType), nodes.getIRINode(EVALUATION)));
        metadata.add(new Quad(registry, graphEval, nodes.getIRINode(KernelSchema.NAME), nodes.getLiteralNode(name, Vocabulary.xsdString, null)));
        metadata.add(new Quad(registry, graphEval, nodes.getIRINode(KernelSchema.CREATED), nodes.getLiteralNode(DateFormat.getDateTimeInstance().format(new Date()), Vocabulary.xsdDateTime, null)));
        List<Quad> content = new ArrayList<>();
        content.add(new Quad(graphEval, graphEval, nodes.getIRINode(EVAL_URI + "/evaluableType"), nodes.getLiteralNode(evaluableType.getIdentifier(), Vocabulary.xsdString, null)));
        for (Evaluable evaluable : evaluables) {
            IRINode evaluableNode = nodes.getIRINode(EVAL_URI + "/Evaluable#" + UUID.randomUUID().toString());
            content.add(new Quad(graphEval, graphEval, nodes.getIRINode(EVAL_URI + "/hasEvaluable"), evaluableNode));
            for (Map.Entry<String, String> param : evaluable.getParameters().entrySet()) {
                IRINode paramNode = nodes.getIRINode(EVAL_URI + "/Parameter#" + UUID.randomUUID().toString());
                content.add(new Quad(graphEval, evaluableNode, nodes.getIRINode(EVAL_URI + "/hasParameter"), paramNode));
                content.add(new Quad(graphEval, paramNode, nodes.getIRINode(EVAL_URI + "/name"), nodes.getLiteralNode(param.getKey(), Vocabulary.xsdString, null)));
                content.add(new Quad(graphEval, paramNode, nodes.getIRINode(EVAL_URI + "/value"), nodes.getLiteralNode(param.getValue(), Vocabulary.xsdString, null)));
            }
        }
        for (Criterion criterion : criteria) {
            IRINode criterionNode = nodes.getIRINode(EVAL_URI + "/Criterion#" + UUID.randomUUID().toString());
            content.add(new Quad(graphEval, graphEval, nodes.getIRINode(EVAL_URI + "/hasCriterion"), criterionNode));
            content.add(new Quad(graphEval, criterionNode, nodes.getIRINode(Vocabulary.rdfType), nodes.getLiteralNode(criterion.getType().getIdentifier(), Vocabulary.xsdString, null)));
            for (Map.Entry<String, String> param : criterion.getParameters().entrySet()) {
                IRINode paramNode = nodes.getIRINode(EVAL_URI + "/Parameter#" + UUID.randomUUID().toString());
                content.add(new Quad(graphEval, criterionNode, nodes.getIRINode(EVAL_URI + "/hasParameter"), paramNode));
                content.add(new Quad(graphEval, paramNode, nodes.getIRINode(EVAL_URI + "/name"), nodes.getLiteralNode(param.getKey(), Vocabulary.xsdString, null)));
                content.add(new Quad(graphEval, paramNode, nodes.getIRINode(EVAL_URI + "/value"), nodes.getLiteralNode(param.getValue(), Vocabulary.xsdString, null)));
            }
        }
        Artifact artifact = new ArtifactSimple(metadata, content);
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        return lts.getServiceStore().store(artifact);
    }

    /**
     * Retrieves a list of reference to evaluations (not their content)
     *
     * @return The response
     */
    public static XSPReply retrieveAll() {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        Result sparqlResult = lts.getServiceStore().sparql("SELECT DISTINCT ?a ?n WHERE { GRAPH <" +
                TextUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS) +
                "> { ?a a <" +
                TextUtils.escapeAbsoluteURIW3C(EVALUATION) +
                "> . ?a <" +
                TextUtils.escapeAbsoluteURIW3C(KernelSchema.NAME) +
                "> ?n } }");
        if (!sparqlResult.isSuccess())
            return new XSPReplyFailure(((ResultFailure) sparqlResult).getMessage());
        Collection<EvaluationReference> result = new ArrayList<>();
        for (RDFPatternSolution solution : ((ResultSolutions) sparqlResult).getSolutions()) {
            result.add(new XOWLEvaluationReference(
                    ((IRINode) solution.get("a")).getIRIValue(),
                    ((LiteralNode) solution.get("n")).getLexicalValue()
            ));
        }
        return new XSPReplyResultCollection<>(result);
    }

    /**
     * Retrieves the data of an evaluation
     *
     * @param service    The parent service
     * @param identifier The identifier of the evaluation to retrieve
     * @return The result
     */
    public static XSPReply retrieve(XOWLEvaluationService service, String identifier) {
        TripleStoreService lts = ServiceUtils.getService(TripleStoreService.class);
        if (lts == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = lts.getServiceStore().retrieve(identifier);
        if (!reply.isSuccess())
            return reply;
        Artifact artifact = ((XSPReplyResult<Artifact>) reply).getData();
        Collection<Quad> content = artifact.getContent();
        if (content.isEmpty())
            return XSPReplyNotFound.instance();
        IRINode graphEval = (IRINode) content.iterator().next().getGraph();
        Map<SubjectNode, Collection<Quad>> data = PlatformUtils.mapBySubject(content);

        EvaluableType evaluableType = null;
        Collection<Evaluable> evaluables = new ArrayList<>();
        Collection<Criterion> criteria = new ArrayList<>();

        Collection<Quad> quads = data.get(graphEval);
        for (Quad quad : quads) {
            if (((IRINode) quad.getProperty()).getIRIValue().equals(EVAL_URI + "/evaluableType")) {
                String id = ((LiteralNode) quad.getObject()).getLexicalValue();
                evaluableType = service.getEvaluableType(id);
                break;
            }
        }
        if (evaluableType == null)
            return XSPReplyNotFound.instance();
        for (Quad quad : quads) {
            if (((IRINode) quad.getProperty()).getIRIValue().equals(EVAL_URI + "/hasEvaluable")) {
                IRINode evaluableNode = (IRINode) quad.getObject();
                Map<String, String> parameters = loadParameters(data, evaluableNode);
                evaluables.add(evaluableType.getElement(parameters));
            }
        }
        for (Quad quad : quads) {
            if (((IRINode) quad.getProperty()).getIRIValue().equals(EVAL_URI + "/hasCriterion")) {
                IRINode criterionNode = (IRINode) quad.getObject();
                Map<String, String> parameters = loadParameters(data, criterionNode);
                CriterionType criterionType = null;
                for (Quad q : data.get(criterionNode)) {
                    if (((IRINode) q.getProperty()).getIRIValue().endsWith(Vocabulary.rdfType)) {
                        String criterionTypeId = ((LiteralNode) q.getObject()).getLexicalValue();
                        criterionType = service.getCriterionType(criterionTypeId);
                        break;
                    }
                }
                if (criterionType == null)
                    continue;
                criteria.add(criterionType.getCriterion(parameters));
            }
        }

        XOWLEvaluation evaluation = new XOWLEvaluation(graphEval.getIRIValue(), artifact.getName(), evaluableType, evaluables, criteria);
        return new XSPReplyResult<>(evaluation);
    }

    /**
     * Loads the parameters for an element
     *
     * @param data    The current data map
     * @param element The element
     * @return The parameters
     */
    private static Map<String, String> loadParameters(Map<SubjectNode, Collection<Quad>> data, IRINode element) {
        Map<String, String> result = new HashMap<>();
        for (Quad quad : data.get(element)) {
            if (((IRINode) quad.getProperty()).getIRIValue().equals(EVAL_URI + "/hasParameter")) {
                IRINode paramNode = (IRINode) quad.getObject();
                String name = null;
                String value = null;
                for (Quad q : data.get(paramNode)) {
                    if (((IRINode) q.getProperty()).getIRIValue().equals(EVAL_URI + "/name"))
                        name = ((LiteralNode) q.getObject()).getLexicalValue();
                    else if (((IRINode) q.getProperty()).getIRIValue().equals(EVAL_URI + "/value"))
                        value = ((LiteralNode) q.getObject()).getLexicalValue();
                }
                result.put(name, value);
            }
        }
        return result;
    }
}
