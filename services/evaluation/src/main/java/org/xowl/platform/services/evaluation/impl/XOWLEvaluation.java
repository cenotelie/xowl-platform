/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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

package org.xowl.platform.services.evaluation.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.store.IOUtils;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.services.evaluation.*;

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
     * @param name          The evaluation's name
     * @param evaluableType The type of the evaluable elements in this evaluation
     * @param evaluables    The elements to evaluate
     * @param criteria      The selected criteria
     */
    public XOWLEvaluation(String name, EvaluableType evaluableType, Collection<Evaluable> evaluables, Collection<Criterion> criteria) {
        this.identifier = EVAL_URI + "/Evaluation#" + UUID.randomUUID();
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
            String memberName = IOUtils.unescape(member.getChildren().get(0).getValue());
            memberName = memberName.substring(1, memberName.length() - 1);
            switch (memberName) {
                case "identifier":
                    identifier = IOUtils.unescape(member.getChildren().get(1).getValue());
                    identifier = identifier.substring(1, identifier.length() - 1);
                    break;
                case "name":
                    name = IOUtils.unescape(member.getChildren().get(1).getValue());
                    name = name.substring(1, name.length() - 1);
                    break;
                case "evaluableType":
                    String id = IOUtils.unescape(member.getChildren().get(1).getValue());
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
        this.name = name;
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
            String paramName = IOUtils.unescape(member.getChildren().get(0).getValue());
            paramName = paramName.substring(1, paramName.length() - 1);
            String paramValue = IOUtils.unescape(member.getChildren().get(1).getValue());
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
            String memberName = IOUtils.unescape(member.getChildren().get(0).getValue());
            memberName = memberName.substring(1, memberName.length() - 1);
            switch (memberName) {
                case "typeId":
                    typeId = IOUtils.unescape(member.getChildren().get(1).getValue());
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
        Map<CriterionParam, String> params = new HashMap<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            CriterionParam param = null;
            for (CriterionParam p : criterionType.getParameters()) {
                if (p.getIdentifier().equals(entry.getKey())) {
                    param = p;
                    break;
                }
            }
            if (param != null)
                params.put(param, entry.getValue());
        }
        return criterionType.getCriterion(params);
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
        builder.append(IOUtils.escapeStringJSON(XOWLEvaluation.class.getCanonicalName()));
        builder.append("\", \"id\": \"");
        builder.append(IOUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(name));
        builder.append("\", \"evaluableType\": \"");
        builder.append(IOUtils.escapeStringJSON(evaluableType.getIdentifier()));
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
        builder.append("]}");
        return builder.toString();
    }
}
