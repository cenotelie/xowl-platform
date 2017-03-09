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

package org.xowl.platform.kernel.impl;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.config.Section;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.bots.Bot;
import org.xowl.platform.kernel.bots.BotFactory;
import org.xowl.platform.kernel.bots.BotManagementService;
import org.xowl.platform.kernel.bots.BotSpecification;
import org.xowl.platform.kernel.events.Event;
import org.xowl.platform.kernel.events.EventConsumer;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.platform.PlatformStartupEvent;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecurityService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default implementation of the bot management service for the platform
 *
 * @author Laurent Wouters
 */
public class KernelBotManagementService implements BotManagementService, EventConsumer {
    /**
     * The loaded bots
     */
    private final Map<String, Bot> bots;

    /**
     * Initializes this service
     *
     * @param eventService The event service
     */
    public KernelBotManagementService(EventService eventService) {
        bots = new HashMap<>();
        eventService.subscribe(this, null, PlatformStartupEvent.TYPE);
    }

    @Override
    public String getIdentifier() {
        return KernelEventService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Bot Management Service";
    }

    @Override
    public SecuredAction[] getActions() {
        return ACTIONS;
    }

    @Override
    public XSPReply getBots() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_GET_BOTS);
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResultCollection<>(bots.values());
    }

    @Override
    public XSPReply wakeup(String botId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_WAKE_UP);
        if (!reply.isSuccess())
            return reply;

        Bot bot = bots.get(botId);
        if (bot == null)
            return XSPReplyNotFound.instance();
        return bot.wakeup();
    }

    @Override
    public XSPReply putToSleep(String botId) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(ACTION_SLEEP);
        if (!reply.isSuccess())
            return reply;

        Bot bot = bots.get(botId);
        if (bot == null)
            return XSPReplyNotFound.instance();
        return bot.sleep();
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType().equals(PlatformStartupEvent.TYPE)) {
            ConfigurationService configurationService = Register.getComponent(ConfigurationService.class);
            Configuration configuration = configurationService.getConfigFor(KernelBotManagementService.class.getCanonicalName());
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
        BotSpecification specification = new BotSpecification(id, name, botType, wakeupOnStartup);

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

    /**
     * When the platform is closing
     */
    public void close() {
        for (Bot bot : bots.values()) {
            bot.sleep();
        }
    }
}
