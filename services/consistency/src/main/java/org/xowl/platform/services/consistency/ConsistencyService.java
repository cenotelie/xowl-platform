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

package org.xowl.platform.services.consistency;

import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.collections.Couple;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricBase;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.services.consistency.impl.XOWLConsistencyService;

/**
 * Represents a service that manages the consistency on the platform
 *
 * @author Laurent Wouters
 */
public interface ConsistencyService extends SecuredService, MeasurableService {
    /**
     * The inconsistency count metric
     */
    Metric METRIC_INCONSISTENCY_COUNT = new MetricBase(XOWLConsistencyService.class.getCanonicalName() + ".InconsistencyCount",
            "Consistency Service - Inconsistency count",
            "inconsistencies",
            1000000000,
            new Couple<>(Metric.HINT_IS_NUMERIC, "true"),
            new Couple<>(Metric.HINT_MIN_VALUE, "0"));

    /**
     * Service action to create a reasoning rule
     */
    SecuredAction ACTION_CREATE_REASONING_RULE = new SecuredAction(ConsistencyService.class.getCanonicalName() + ".CreateReasoningRule", "Consistency Service - Create Reasoning Rule");
    /**
     * Service action to delete a reasoning rule
     */
    SecuredAction ACTION_DELETE_REASONING_RULE = new SecuredAction(ConsistencyService.class.getCanonicalName() + ".DeleteReasoningRule", "Consistency Service - Delete Reasoning Rule");
    /**
     * Service action to activate a reasoning rule
     */
    SecuredAction ACTION_ACTIVATE_REASONING_RULE = new SecuredAction(ConsistencyService.class.getCanonicalName() + ".ActivateReasoningRule", "Consistency Service - Activate Reasoning Rule");
    /**
     * Service action to de-activate a reasoning rule
     */
    SecuredAction ACTION_DEACTIVATE_REASONING_RULE = new SecuredAction(ConsistencyService.class.getCanonicalName() + ".DeactivateReasoningRule", "Consistency Service - Deactivate Reasoning Rule");
    /**
     * Service action to create a consistency constraint
     */
    SecuredAction ACTION_CREATE_CONSISTENCY_CONSTRAINT = new SecuredAction(ConsistencyService.class.getCanonicalName() + ".CreateConsistencyConstraint", "Consistency Service - Create Consistency Constraint");
    /**
     * Service action to delete a consistency constraint
     */
    SecuredAction ACTION_DELETE_CONSISTENCY_CONSTRAINT = new SecuredAction(ConsistencyService.class.getCanonicalName() + ".DeleteConsistencyConstraint", "Consistency Service - Delete Consistency Constraint");
    /**
     * Service action to activate a consistency constraint
     */
    SecuredAction ACTION_ACTIVATE_CONSISTENCY_CONSTRAINT = new SecuredAction(ConsistencyService.class.getCanonicalName() + ".ActivateConsistencyConstraint", "Consistency Service - Activate Consistency Constraint");
    /**
     * Service action to de-activate a consistency constraint
     */
    SecuredAction ACTION_DEACTIVATE_CONSISTENCY_CONSTRAINT = new SecuredAction(ConsistencyService.class.getCanonicalName() + ".DeactivateConsistencyConstraint", "Consistency Service - Deactivate Consistency Constraint");

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_CREATE_REASONING_RULE,
            ACTION_DELETE_REASONING_RULE,
            ACTION_ACTIVATE_REASONING_RULE,
            ACTION_DEACTIVATE_REASONING_RULE,
            ACTION_CREATE_CONSISTENCY_CONSTRAINT,
            ACTION_DELETE_CONSISTENCY_CONSTRAINT,
            ACTION_ACTIVATE_CONSISTENCY_CONSTRAINT,
            ACTION_DEACTIVATE_CONSISTENCY_CONSTRAINT
    };

    /**
     * Gets the current inconsistencies on the platform
     *
     * @return The current inconsistencies
     */
    Reply getInconsistencies();

    /**
     * Gets all the reasoning rules
     *
     * @return The reasoning rules
     */
    Reply getReasoningRules();

    /**
     * Gets the reasoning rule for the specified identifier
     *
     * @param identifier The identifier of a rule
     * @return The operation's result
     */
    Reply getReasoningRule(String identifier);

    /**
     * Creates a new reasoning rule
     *
     * @param name       The rule's name
     * @param definition The rule's definition
     * @return The operation's result
     */
    Reply createReasoningRule(String name, String definition);

    /**
     * Adds the specified reasoning rule
     *
     * @param rule The reasoning rule to add
     * @return The operation's result
     */
    Reply addReasoningRule(ReasoningRule rule);

    /**
     * Activates a reasoning rule
     *
     * @param identifier The identifier of a reasoning rule
     * @return The operation's result
     */
    Reply activateReasoningRule(String identifier);

    /**
     * Activates a reasoning rule
     *
     * @param rule The reasoning rule to activate
     * @return The operation's result
     */
    Reply activateReasoningRule(ReasoningRule rule);

    /**
     * Deactivates a reasoning rule
     *
     * @param identifier The identifier of a reasoning rule
     * @return The operation's result
     */
    Reply deactivateReasoningRule(String identifier);

    /**
     * Deactivates a reasoning rule
     *
     * @param rule The reasoning rule to deactivate
     * @return The operation's result
     */
    Reply deactivateReasoningRule(ReasoningRule rule);

    /**
     * Deletes a reasoning rule
     *
     * @param identifier The identifier of a reasoning rule
     * @return The operation's result
     */
    Reply deleteReasoningRule(String identifier);

    /**
     * Deletes a reasoning rule
     *
     * @param rule The reasoning rule to delete
     * @return The operation's result
     */
    Reply deleteReasoningRule(ReasoningRule rule);

    /**
     * Gets all the consistency constraints
     *
     * @return The consistency constraints
     */
    Reply getConsistencyConstraints();

    /**
     * Gets the consistency constraint for the specified identifier
     *
     * @param identifier The identifier of a constraint
     * @return The operation's result
     */
    Reply getConsistencyConstraint(String identifier);

    /**
     * Creates a new consistency constraint
     *
     * @param name        The constraint's name
     * @param message     The constraint's user message for inconsistencies
     * @param prefixes    The prefixes for short URIs
     * @param antecedents The constraint's antecedents for matching
     * @param guard       The constraint's guard (if any)
     * @return The operation's result
     */
    Reply createConsistencyConstraint(String name, String message, String prefixes, String antecedents, String guard);

    /**
     * Adds the specified consistency constraint
     *
     * @param constraint The consistency constraint to add
     * @return The operation's result
     */
    Reply addConsistencyConstraint(ConsistencyConstraint constraint);

    /**
     * Activates a consistency constraint
     *
     * @param identifier The identifier of a consistency constraint
     * @return The operation's result
     */
    Reply activateConsistencyConstraint(String identifier);

    /**
     * Activates a consistency constraint
     *
     * @param constraint The consistency constraint to activate
     * @return The operation's result
     */
    Reply activateConsistencyConstraint(ConsistencyConstraint constraint);

    /**
     * Deactivates a consistency constraint
     *
     * @param identifier The identifier of a consistency constraint
     * @return The operation's result
     */
    Reply deactivateConsistencyConstraint(String identifier);

    /**
     * Deactivates a consistency constraint
     *
     * @param constraint The consistency constraint to deactivate
     * @return The operation's result
     */
    Reply deactivateConsistencyConstraint(ConsistencyConstraint constraint);

    /**
     * Deletes a consistency constraint
     *
     * @param identifier The identifier of a consistency constraint
     * @return The operation's result
     */
    Reply deleteConsistencyConstraint(String identifier);

    /**
     * Deletes a consistency constraint
     *
     * @param constraint The consistency constraint to delete
     * @return The operation's result
     */
    Reply deleteConsistencyConstraint(ConsistencyConstraint constraint);
}
