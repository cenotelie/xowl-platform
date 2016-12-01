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
import org.xowl.platform.kernel.HttpApiService;
import org.xowl.platform.kernel.Service;

/**
 * Represents a service that manages the consistency on the platform
 *
 * @author Laurent Wouters
 */
public interface ConsistencyService extends Service, HttpApiService {
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
