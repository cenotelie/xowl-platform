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

package org.xowl.platform.services.impact.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.services.impact.ImpactAnalysisService;
import org.xowl.platform.services.impact.ImpactAnalysisSetup;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Implements the impact analysis service
 *
 * @author Laurent Wouters
 */
public class XOWLImpactAnalysis implements ImpactAnalysisService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/core/impact"
    };

    @Override
    public String getIdentifier() {
        return XOWLImpactAnalysis.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Impact Analysis Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (!method.equals("POST"))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD);
        if (content == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        BufferedLogger logger = new BufferedLogger();
        ASTNode root = PlatformUtils.parseJSON(logger, new String(content, Files.CHARSET));
        if (root == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, HttpConstants.MIME_TEXT_PLAIN, logger.getErrorsAsString());
        return XSPReplyUtils.toHttpResponse(perform(new XOWLImpactAnalysisSetup(root)), null);
    }

    @Override
    public XSPReply perform(ImpactAnalysisSetup setup) {
        JobExecutionService executionService = ServiceUtils.getService(JobExecutionService.class);
        if (executionService == null)
            return XSPReplyServiceUnavailable.instance();
        XOWLImpactAnalysisJob job = new XOWLImpactAnalysisJob(setup);
        executionService.schedule(job);
        return new XSPReplyResult<>(job);
    }
}
