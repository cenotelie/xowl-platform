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

package org.xowl.platform.services.community.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.community.profiles.ProfileService;
import org.xowl.platform.services.community.profiles.ProfileServiceProvider;
import org.xowl.platform.services.community.profiles.PublicProfile;

import java.net.HttpURLConnection;

/**
 * The implementation of the profile service used to delegate to a configured service
 *
 * @author Laurent Wouters
 */
public class XOWLProfileService implements ProfileService, HttpApiService {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLProfileService.class, "/org/xowl/platform/services/community/api_service_profiles.raml", "Profile Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLProfileService.class, "/org/xowl/platform/services/community/api_service_profiles.html", "Profile Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The URI for the API services
     */
    private final String apiUri;
    /**
     * The configured implementation for this service
     */
    private ProfileService implementation;

    /**
     * Initializes this service
     */
    public XOWLProfileService() {
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/services/community/profiles";
    }

    /**
     * Resolves the implementation of this service
     *
     * @return The configured implementation for this service
     */
    private synchronized ProfileService getImplementation() {
        if (implementation != null)
            return implementation;
        ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
        Configuration configuration = configurationService.getConfigFor(ProfileService.class.getCanonicalName());
        String identifier = configuration.get("implementation");
        Section section = configuration.getSection(identifier);
        for (ProfileServiceProvider provider : Register.getComponents(ProfileServiceProvider.class)) {
            implementation = provider.instantiate(identifier, section);
            if (implementation != null)
                break;
        }
        return implementation;
    }

    @Override
    public String getIdentifier() {
        return ProfileService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Profile Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public PublicProfile getPublicProfile(String identifier) {
        return getImplementation().getPublicProfile(identifier);
    }

    @Override
    public XSPReply updatePublicProfile(PublicProfile profile) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_UPDATE_PROFILE);
        if (!reply.isSuccess())
            return reply;
        return getImplementation().updatePublicProfile(profile);
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (request.getUri().equals(apiUri))
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);

        if (request.getUri().startsWith(apiUri)) {
            String rest = request.getUri().substring(apiUri.length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String profileId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0)
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            if (rest.substring(index).equals("/public")) {
                if (HttpConstants.METHOD_GET.equals(request.getMethod())) {
                    PublicProfile profile = getPublicProfile(profileId);
                    if (profile == null)
                        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                    return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, profile.serializedJSON());
                } else if (HttpConstants.METHOD_PUT.equals(request.getMethod())) {
                    String content = new String(request.getContent(), IOUtils.CHARSET);
                    if (content.isEmpty())
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_FAILED_TO_READ_CONTENT), null);
                    BufferedLogger logger = new BufferedLogger();
                    ASTNode root = JSONLDLoader.parseJSON(logger, content);
                    if (root == null)
                        return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
                    PublicProfile profile = new PublicProfile(root);
                    return XSPReplyUtils.toHttpResponse(updatePublicProfile(profile), null);
                }
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected method: GET, PUT");
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
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
        return null;
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
}
