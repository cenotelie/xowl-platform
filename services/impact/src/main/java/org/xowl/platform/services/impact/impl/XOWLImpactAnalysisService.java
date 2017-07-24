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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.api.ReplyApiError;
import org.xowl.infra.utils.api.ReplyResult;
import org.xowl.infra.utils.api.ReplyUtils;
import org.xowl.infra.store.loaders.JsonLoader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.impact.ImpactAnalysisService;
import org.xowl.platform.services.impact.ImpactAnalysisSetup;

import java.net.HttpURLConnection;

/**
 * Implements the impact analysis service
 *
 * @author Laurent Wouters
 */
public class XOWLImpactAnalysisService implements ImpactAnalysisService, HttpApiService {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLImpactAnalysisService.class, "/org/xowl/platform/services/impact/api_service_impact.raml", "Impact Analysis Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLImpactAnalysisService.class, "/org/xowl/platform/services/impact/api_service_impact.html", "Impact Analysis Service - Documentation", HttpApiResource.MIME_HTML);
    /**
     * The resource for the API's schema
     */
    private static final HttpApiResource RESOURCE_SCHEMA = new HttpApiResourceBase(XOWLImpactAnalysisService.class, "/org/xowl/platform/services/impact/schema_platform_impact.json", "Impact Analysis Service - Schema", HttpConstants.MIME_JSON);

    /**
     * The URI for the API services
     */
    private final String apiUri;

    /**
     * Initializes this service
     */
    public XOWLImpactAnalysisService() {
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/services/impact";
    }

    @Override
    public String getIdentifier() {
        return XOWLImpactAnalysisService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Impact Analysis Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public boolean requireAuth(HttpApiRequest request) {
        return true;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
        byte[] content = request.getContent();
        if (content == null || content.length == 0)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);

        BufferedLogger logger = new BufferedLogger();
        ASTNode root = JsonLoader.parseJson(logger, new String(content, IOUtils.CHARSET));
        if (root == null)
            return ReplyUtils.toHttpResponse(new ReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
        return ReplyUtils.toHttpResponse(perform(new XOWLImpactAnalysisSetup(root)), null);
    }

    @Override
    public HttpApiResource getApiSpecification() {
        return RESOURCE_SPECIFICATION;
    }

    @Override
    public HttpApiResource getApiDocumentation() {
        return RESOURCE_DOCUMENTATION;
    }

    @Override
    public HttpApiResource[] getApiOtherResources() {
        return new HttpApiResource[]{RESOURCE_SCHEMA};
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(HttpApiService.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"specification\": " +
                RESOURCE_SPECIFICATION.serializedJSON() +
                ", \"documentation\": " +
                RESOURCE_DOCUMENTATION.serializedJSON() +
                "}";
    }

    @Override
    public Reply perform(ImpactAnalysisSetup setup) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(ImpactAnalysisService.ACTION_PERFORM);
        if (!reply.isSuccess())
            return reply;
        JobExecutionService executionService = Register.getComponent(JobExecutionService.class);
        if (executionService == null)
            return ReplyServiceUnavailable.instance();
        XOWLImpactAnalysisJob job = new XOWLImpactAnalysisJob(setup);
        executionService.schedule(job);
        return new ReplyResult<>(job);
    }
}
