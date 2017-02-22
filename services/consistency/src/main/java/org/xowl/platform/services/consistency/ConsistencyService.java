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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.utils.collections.Couple;
import org.xowl.infra.utils.metrics.Metric;
import org.xowl.infra.utils.metrics.MetricBase;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.services.consistency.impl.XOWLConsistencyService;
import org.xowl.platform.services.storage.StorageService;

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
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            StorageService.ACTION_CREATE_RULE,
            StorageService.ACTION_DELETE_RULE,
            StorageService.ACTION_ACTIVATE_RULE,
            StorageService.ACTION_DEACTIVATE_RULE
    };

    /**
     * Gets all the consistency rules
     *
     * @return The consistency rules
     */
    XSPReply getRules();

    /**
     * Gets the current inconsistencies on the platform
     *
     * @return The current inconsistencies
     */
    XSPReply getInconsistencies();

    /**
     * Gets the rule for the specified identifier
     *
     * @param identifier The identifier of a rule
     * @return The operation's result
     */
    XSPReply getRule(String identifier);

    /**
     * Creates a new consistency rule
     *
     * @param name       The rule's name
     * @param message    The rule's user message for inconsistencies
     * @param prefixes   The prefixes for short URIs
     * @param conditions The rule's conditions for matching
     * @return The operation's result
     */
    XSPReply createRule(String name, String message, String prefixes, String conditions);

    /**
     * Adds the specified consistency rule
     *
     * @param rule The consistency rule to add
     * @return The operation's result
     */
    XSPReply addRule(ConsistencyRule rule);

    /**
     * Activates a rule
     *
     * @param identifier The identifier of a rule
     * @return The operation's result
     */
    XSPReply activateRule(String identifier);

    /**
     * Activates a rule
     *
     * @param rule The rule to activate
     * @return The operation's result
     */
    XSPReply activateRule(ConsistencyRule rule);

    /**
     * Deactivates a rule
     *
     * @param identifier The identifier of a rule
     * @return The operation's result
     */
    XSPReply deactivateRule(String identifier);

    /**
     * Deactivates a rule
     *
     * @param rule The rule to deactivate
     * @return The operation's result
     */
    XSPReply deactivateRule(ConsistencyRule rule);

    /**
     * Deletes a rule
     *
     * @param identifier The identifier of a rule
     * @return The operation's result
     */
    XSPReply deleteRule(String identifier);

    /**
     * Deletes a rule
     *
     * @param rule The rule to delete
     * @return The operation's result
     */
    XSPReply deleteRule(ConsistencyRule rule);
}
