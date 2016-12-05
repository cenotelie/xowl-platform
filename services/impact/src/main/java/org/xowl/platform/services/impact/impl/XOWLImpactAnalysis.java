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

package org.xowl.platform.services.impact.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.impact.ImpactAnalysisService;
import org.xowl.platform.services.impact.ImpactAnalysisSetup;

import java.net.HttpURLConnection;

/**
 * Implements the impact analysis service
 *
 * @author Laurent Wouters
 */
public class XOWLImpactAnalysis implements ImpactAnalysisService {
    /**
     * The URI for the API services
     */
    private static final String URI_API = HttpApiService.URI_API + "/services/impact";

    @Override
    public String getIdentifier() {
        return XOWLImpactAnalysis.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Impact Analysis Service";
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(URI_API)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(HttpApiRequest request) {
        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
        byte[] content = request.getContent();
        if (content == null || content.length == 0)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);

        BufferedLogger logger = new BufferedLogger();
        ASTNode root = JSONLDLoader.parseJSON(logger, new String(content, Files.CHARSET));
        if (root == null)
            return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
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
