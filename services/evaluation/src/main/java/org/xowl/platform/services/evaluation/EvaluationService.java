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

package org.xowl.platform.services.evaluation;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.utils.ApiError;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.util.Collection;

/**
 * Implements the evaluation service for the xOWL platform.
 * The evaluation service provides an API for running evaluation criteria against evaluable elements.
 *
 * @author Laurent Wouters
 */
public interface EvaluationService extends SecuredService {
    /**
     * Service action to get the current evaluations
     */
    SecuredAction ACTION_GET_EVALUATIONS = new SecuredAction(EvaluationService.class.getCanonicalName() + ".GetEvaluations", "Evaluation Service - Get Evaluation(s)");
    /**
     * Service action to create a new evaluation
     */
    SecuredAction ACTION_NEW_EVALUATION = new SecuredAction(EvaluationService.class.getCanonicalName() + ".CreateEvaluation", "Evaluation Service - Create Evaluation");

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_GET_EVALUATIONS,
            ACTION_NEW_EVALUATION
    };

    /**
     * API error - The specified evaluable type is invalid (or unspecified)
     */
    ApiError ERROR_INVALID_EVALUABLE_TYPE = new ApiError(0x00050002,
            "The specified evaluable type is invalid (or unspecified).",
            HttpApiService.ERROR_HELP_PREFIX + "0x00050002.html");

    /**
     * Gets the registered evaluable types
     *
     * @return the registered evaluable types
     */
    Collection<EvaluableType> getEvaluableTypes();

    /**
     * Gets the registered criterion types
     *
     * @return The registered criterion types
     */
    Collection<CriterionType> getCriterionTypes();

    /**
     * Gets the evaluable type for the specified identifier
     *
     * @param typeId The identifier of an evaluable type
     * @return The resolve evaluable type, if any
     */
    EvaluableType getEvaluableType(String typeId);

    /**
     * Gets the criterion type for the specified identifier
     *
     * @param typeId The identifier of a criterion type
     * @return The resolve criterion type, if any
     */
    CriterionType getCriterionType(String typeId);

    /**
     * Gets the criterion types applicable to the specified type of evaluable elements
     *
     * @param evaluableType The evaluable type
     * @return The applicable criterion types
     */
    Collection<CriterionType> getCriterionTypes(EvaluableType evaluableType);

    /**
     * Gets the ongoing evaluations
     *
     * @return The ongoing evaluations
     */
    XSPReply getEvaluations();

    /**
     * Gets the evaluation for the specified identifier
     *
     * @param evalId The identifier of an evaluation
     * @return The corresponding evaluation
     */
    XSPReply getEvaluation(String evalId);

    /**
     * Launches a new evaluation
     *
     * @param name          Then evaluation's name
     * @param evaluableType The type of the evaluable elements
     * @param evaluables    The evaluable elements
     * @param criteria      The evaluation criteria
     * @return The operation's result
     */
    XSPReply newEvaluation(String name, EvaluableType evaluableType, Collection<Evaluable> evaluables, Collection<Criterion> criteria);
}
