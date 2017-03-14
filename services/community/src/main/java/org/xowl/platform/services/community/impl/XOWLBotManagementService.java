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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.http.HttpConstants;
import org.xowl.infra.utils.http.HttpResponse;
import org.xowl.infra.utils.http.URIUtils;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.events.Event;
import org.xowl.platform.kernel.events.EventConsumer;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.platform.PlatformStartupEvent;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.webapi.HttpApiRequest;
import org.xowl.platform.kernel.webapi.HttpApiResource;
import org.xowl.platform.kernel.webapi.HttpApiResourceBase;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.community.bots.Bot;
import org.xowl.platform.services.community.bots.BotFactory;
import org.xowl.platform.services.community.bots.BotManagementService;
import org.xowl.platform.services.community.bots.BotSpecification;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default implementation of the bot management service for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLBotManagementService implements BotManagementService, HttpApiService, EventConsumer, Closeable {
    /**
     * The resource for the API's specification
     */
    private static final HttpApiResource RESOURCE_SPECIFICATION = new HttpApiResourceBase(XOWLBotManagementService.class, "/org/xowl/platform/services/community/api_service_bots.raml", "Bots Management Service - Specification", HttpApiResource.MIME_RAML);
    /**
     * The resource for the API's documentation
     */
    private static final HttpApiResource RESOURCE_DOCUMENTATION = new HttpApiResourceBase(XOWLBotManagementService.class, "/org/xowl/platform/services/community/api_service_bots.html", "Bots Management Service - Documentation", HttpApiResource.MIME_HTML);

    /**
     * The URI for the API services
     */
    private final String apiUri;
    /**
     * The loaded bots
     */
    private final Map<String, Bot> bots;

    /**
     * Initializes this service
     *
     * @param eventService The event service
     */
    public XOWLBotManagementService(EventService eventService) {
        this.apiUri = PlatformHttp.getUriPrefixApi() + "/services/community/bots";
        this.bots = new HashMap<>();
        eventService.subscribe(this, PlatformStartupEvent.TYPE);
    }

    @Override
    public String getIdentifier() {
        return BotManagementService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Bots Management Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public Collection<Bot> getBots() {
        return bots.values();
    }

    @Override
    public Bot getBot(String botId) {
        return bots.get(botId);
    }

    @Override
    public XSPReply getBotMessages(String botId) {
        Bot bot = bots.get(botId);
        if (bot == null)
            return XSPReplyNotFound.instance();
        return bot.getMessages();
    }

    @Override
    public XSPReply wakeup(String botId) {
        Bot bot = bots.get(botId);
        if (bot == null)
            return XSPReplyNotFound.instance();
        return bot.wakeup();
    }

    @Override
    public XSPReply putToSleep(String botId) {
        Bot bot = bots.get(botId);
        if (bot == null)
            return XSPReplyNotFound.instance();
        return bot.sleep();
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType().equals(PlatformStartupEvent.TYPE)) {
            ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
            Configuration configuration = configurationService.getConfigFor(BotManagementService.class.getCanonicalName());
            for (Section section : configuration.getSections()) {
                BotSpecification specification = loadBotSpecification(section);
                if (specification == null)
                    continue;
                for (BotFactory factory : Register.getComponents(BotFactory.class)) {
                    Bot bot = factory.newBot(specification);
                    if (bot != null) {
                        bots.put(bot.getIdentifier(), bot);
                        if (bot.getWakeupOnStartup())
                            bot.wakeup();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Loads a bot specification from a section
     *
     * @param section The configuration section
     * @return The specification, or null if the section is not a valid specification
     */
    private BotSpecification loadBotSpecification(Section section) {
        String id = section.getName();
        String name = section.get("name");
        String botType = section.get("type");
        if (id == null || name == null || botType == null)
            return null;
        String wakeup = section.get("wakeupOnStartup");
        boolean wakeupOnStartup = wakeup != null && "true".equalsIgnoreCase(wakeup);
        BotSpecification specification = new BotSpecification(id, name, botType, section.get("securityUser"), wakeupOnStartup);

        for (String property : section.getProperties()) {
            if (property.equals("name") || property.equals("type") || property.equals("wakeupOnStartup"))
                continue;
            List<String> values = section.getAll(property);
            if (values.size() == 1)
                specification.addParameter(property, values.get(0));
            else if (values.size() > 1)
                specification.addParameter(property, values.toArray());
        }
        return specification;
    }

    @Override
    public int canHandle(HttpApiRequest request) {
        return request.getUri().startsWith(apiUri)
                ? HttpApiService.PRIORITY_NORMAL
                : HttpApiService.CANNOT_HANDLE;
    }

    @Override
    public HttpResponse handle(SecurityService securityService, HttpApiRequest request) {
        if (request.getUri().equals(apiUri)) {
            if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
            boolean first = true;
            StringBuilder builder = new StringBuilder("[");
            for (Bot bot : getBots()) {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(bot.serializedJSON());
            }
            builder.append("]");
            return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, builder.toString());
        }

        if (request.getUri().startsWith(apiUri)) {
            String rest = request.getUri().substring(apiUri.length() + 1);
            if (rest.isEmpty())
                return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
            int index = rest.indexOf("/");
            String botId = URIUtils.decodeComponent(index > 0 ? rest.substring(0, index) : rest);
            if (index < 0) {
                if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                Bot bot = getBot(botId);
                if (bot == null)
                    return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND);
                return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, bot.serializedJSON());
            } else if (rest.substring(index).equals("/messages")) {
                if (!HttpConstants.METHOD_GET.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected GET method");
                return XSPReplyUtils.toHttpResponse(getBotMessages(botId), null);
            } else if (rest.substring(index).equals("/wakeup")) {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                return XSPReplyUtils.toHttpResponse(wakeup(botId), null);
            } else if (rest.substring(index).equals("/putToSleep")) {
                if (!HttpConstants.METHOD_POST.equals(request.getMethod()))
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, HttpConstants.MIME_TEXT_PLAIN, "Expected POST method");
                return XSPReplyUtils.toHttpResponse(putToSleep(botId), null);
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

    @Override
    public void close() {
        for (Bot bot : bots.values()) {
            bot.sleep();
        }
    }
}
