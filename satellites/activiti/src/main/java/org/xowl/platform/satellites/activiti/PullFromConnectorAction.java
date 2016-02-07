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

package org.xowl.platform.satellites.activiti;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.xowl.platform.satellites.base.RemoteJob;
import org.xowl.platform.satellites.base.RemotePlatform;

/**
 * An Activiti delegate for pulling an artifact from a connector
 *
 * @author Laurent Wouters
 */
public class PullFromConnectorAction implements JavaDelegate {
    /**
     * The identifier of the connector to pull from
     */
    private Expression connectorId;

    /**
     * Sets the expression for the identifier of the connector to pull from
     *
     * @param expression The expression for the identifier of the connector to pull from
     */
    public void setConnectorId(Expression expression) {
        this.connectorId = expression;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String endpoint = (String) delegateExecution.getVariable("platformEndpoint");
        String login = (String) delegateExecution.getVariable("platformLogin");
        String password = (String) delegateExecution.getVariable("platformPassword");
        String connector = (String) connectorId.getValue(delegateExecution);

        RemotePlatform platform = new RemotePlatform(endpoint, login, password);
        RemoteJob job = platform.pullFromConnector(connector);
        if (job == null)
            throw new BpmnError("Failed to access the platform");
        job = platform.waitFor(job);
        if (job == null)
            throw new BpmnError("Failed to access the platform");
    }
}
