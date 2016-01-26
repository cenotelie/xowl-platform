/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.store.URIUtils;
import org.xowl.infra.store.http.HttpConnection;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.utils.logging.Logger;
import org.xowl.platform.kernel.Job;
import org.xowl.platform.kernel.JobStatus;
import org.xowl.platform.kernel.PlatformUtils;

import java.net.HttpURLConnection;

/**
 * An Activiti delegate for pulling an artifact from a connector
 *
 * @author Laurent Wouters
 */
public class PullFromConnectorAction implements JavaDelegate {
    /**
     * The URI of the xOWL platform
     */
    private Expression platformUri;
    /**
     * The identifier of the connector to pull from
     */
    private Expression connectorId;

    /**
     * Sets the expression for the URI of the xOWL platform
     *
     * @param expression The expression for the URI of the xOWL platform
     */
    public void setPlatformUri(Expression expression) {
        this.platformUri = expression;
    }

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
        String uri = (String) platformUri.getValue(delegateExecution);
        String connector = (String) connectorId.getValue(delegateExecution);

        HttpConnection connection = new HttpConnection(uri, null, null);
        HttpResponse response = connection.request("/connectors?action=pull&id=" + URIUtils.encodeComponent(connector), "POST", null, null, HttpConstants.MIME_TEXT_PLAIN + ", " + HttpConstants.MIME_JSON);
        if (response == null)
            throw new BpmnError("Failed to connect to the federation platform");
        if (response.getCode() != HttpURLConnection.HTTP_OK)
            throw new BpmnError(response.getBodyAsString());
        ASTNode root = PlatformUtils.parseJSON(Logger.DEFAULT, response.getBodyAsString());
        if (root == null)
            throw new BpmnError("Failed to retrieve the job");
        Job job = new ForeignJob(root);
        while (job.getStatus() != JobStatus.Completed) {
            Thread.sleep(500);
            response = connection.request("/jobs?id=" + URIUtils.encodeComponent(job.getIdentifier()), "GET", null, null, HttpConstants.MIME_TEXT_PLAIN + ", " + HttpConstants.MIME_JSON);
            if (response == null)
                throw new BpmnError("Failed to connect to the federation platform");
            if (response.getCode() != HttpURLConnection.HTTP_OK)
                throw new BpmnError(response.getBodyAsString());
            root = PlatformUtils.parseJSON(Logger.DEFAULT, response.getBodyAsString());
            if (root == null)
                throw new BpmnError("Failed to retrieve the job");
            job = new ForeignJob(root);
        }
        XSPReply result = job.getResult();
        if (!result.isSuccess())
            throw new BpmnError(result.getMessage());
    }
}
