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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.loaders.JsonLoader;
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
import org.xowl.platform.services.community.profiles.Badge;
import org.xowl.platform.services.community.profiles.ProfileService;
import org.xowl.platform.services.community.profiles.ProfileServiceProvider;
import org.xowl.platform.services.community.profiles.PublicProfile;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Objects;

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
     * The URI for the API services for the profiles
     */
    private final String apiUriProfiles;
    /**
     * The URI for the API services for the badges
     */
    private final String apiUriBadges;
    /**
     * The configured implementation for this service
     */
    private ProfileService implementation;

    /**
     * Initializes this service
     */
    public XOWLProfileService() {
        this.apiUriProfiles = PlatformHttp.getUriPrefixApi() + "/services/community/profiles";
        this.apiUriBadges = PlatformHttp.getUriPrefixApi() + "/services/community/badges";
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
        XSPReply reply = securityService.checkAction(ACTION_UPDATE_PROFILE, profile);
        if (!reply.isSuccess())
            return reply;
        return getImplementation().updatePublicProfile(profile);
    }

    @Override
    public Collection<Badge> getBadges() {
        return getImplementation().getBadges();
    }

    @Override
    public Badge getBadge(String badgeId) {
        return getImplementation().getBadge(badgeId);
    }

    @Override
    public XSPReply awardBadge(String userId, String badgeId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_AWARD_BADGE);
        if (!reply.isSuccess())
            return reply;
        return getImplementation().awardBadge(userId, badgeId);
    }

    @Override
    public XSPReply rescindBadge(String userId, String badgeId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_RESCIND_BADGE);
        if (!reply.isSuccess())
            return reply;
        return getImplementation().rescindBadge(userId, badgeId);
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return (request.getUri().startsWith(apiUriProfiles) || request.getUri().startsWith(apiUriBadges))
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (request.getUri().startsWith(apiUriBadges))
            return handleBadges(request);
        if (request.getUri().startsWith(apiUriProfiles))
            return handleProfiles(request);
        return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Handles requests for badges
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleBadges(HttpApiRequest request) {
        if (request.getUri().equals(apiUriBadges)) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (Badge badge : getBadges()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(badge.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        }

        String rest = request.getUri().substring(apiUriBadges.length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String badgeId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
        if (index >= 0)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
        Badge badge = getBadge(badgeId);
        if (badge == null)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, badge.serializedJSON());
    }

    /**
     * Handles requests for profiles
     *
     * @param request The request
     * @return The response
     */
    private HttpResponse handleProfiles(HttpApiRequest request) {
        if (request.getUri().equals(apiUriProfiles))
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        String rest = request.getUri().substring(apiUriProfiles.length() + 1);
        if (rest.isEmpty())
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        int index = rest.indexOf("/");
        String profileId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
        if (index < 0)
            return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
        rest = rest.substring(index);
        if (rest.equals("/public")) {
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
                ASTNode root = JsonLoader.parseJson(logger, content);
                if (root == null)
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, logger.getErrorsAsString()), null);
                PublicProfile profile = new PublicProfile(root, null);
                if (!Objects.equals(profile.getIdentifier(), profileId))
                    return XSPReplyUtils.toHttpResponse(new XSPReplyApiError(ERROR_CONTENT_PARSING_FAILED, "Profile identifier in content does not match URI"), null);
                return XSPReplyUtils.toHttpResponse(updatePublicProfile(profile), null);
            }
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: GET, PUT");
        }
        if (rest.startsWith("/public/badges/")) {
            rest = rest.substring("/public/badges/".length());
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            String badgeId = URIUtils.decodeComponent(rest);
            if (HttpConstants.METHOD_PUT.equals(request.getMethod()))
                return XSPReplyUtils.toHttpResponse(awardBadge(profileId, badgeId), null);
            if (HttpConstants.METHOD_DELETE.equals(request.getMethod()))
                return XSPReplyUtils.toHttpResponse(rescindBadge(profileId, badgeId), null);
            return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected methods: PUT, DELETE");
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
