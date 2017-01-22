/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.kernel.security;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.Registrable;
import org.xowl.platform.kernel.ServiceAction;

/**
 * Represents an authorization policy for the platform
 *
 * @author Laurent Wouters
 */
public interface SecurityPolicy extends Registrable {
    /**
     * The identifier of the property for the policy identifier
     */
    String PROPERTY_ID = "policyId";

    /**
     * Checks the authorization policy for the specified action
     *
     * @param securityService The current security service
     * @param action          The requested action
     * @return The protocol reply
     */
    XSPReply checkAction(SecurityService securityService, ServiceAction action);

    /**
     * Checks the authorization policy for the specified action
     *
     * @param securityService The current security service
     * @param action          The requested action
     * @param data            Custom data that may be required to make a decision
     * @return The protocol reply
     */
    XSPReply checkAction(SecurityService securityService, ServiceAction action, Object data);
}
