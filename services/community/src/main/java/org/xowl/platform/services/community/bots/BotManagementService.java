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

package org.xowl.platform.services.community.bots;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.utils.ApiError;
import org.xowl.platform.kernel.ClosableService;
import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.Collection;

/**
 * Service for the management of bots on the platform
 *
 * @author Laurent Wouters
 */
public interface BotManagementService extends SecuredService, ClosableService {
    /**
     * Service action to get the messages of a bot
     */
    SecuredAction ACTION_GET_MESSAGES = new SecuredAction(BotManagementService.class.getCanonicalName() + ".GetMessages", "Bots Management Service - Get Bot Messages", SecuredActionPolicyIsRunningBot.DESCRIPTOR);
    /**
     * Service action to wakeup bots
     */
    SecuredAction ACTION_WAKE_UP = new SecuredAction(BotManagementService.class.getCanonicalName() + ".Wakeup", "Bots Management Service - Wake up Bots", SecuredActionPolicyIsRunningBot.DESCRIPTOR);
    /**
     * Service action to put bots to sleep
     */
    SecuredAction ACTION_SLEEP = new SecuredAction(BotManagementService.class.getCanonicalName() + ".Sleep", "Bots Management Service - Put Bots to Sleep", SecuredActionPolicyIsRunningBot.DESCRIPTOR);
    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_GET_MESSAGES,
            ACTION_WAKE_UP,
            ACTION_SLEEP
    };

    /**
     * API error - The bot's status is invalid with respect to the requested operation
     */
    ApiError ERROR_INVALID_STATUS = new ApiError(0x00000121,
            "The bot's status is invalid with respect to the requested operation.",
            PlatformHttp.ERROR_HELP_PREFIX + "0x00000121.html");

    /**
     * Gets the list of the known bots
     *
     * @return The protocol reply
     */
    Collection<Bot> getBots();

    /**
     * Gets the details of a bot
     *
     * @param botId The identifier of the bot
     * @return The protocol reply
     */
    Bot getBot(String botId);

    /**
     * Gets the messages of a bot
     *
     * @param botId The identifier of the bot
     * @return The protocol reply
     */
    XSPReply getBotMessages(String botId);

    /**
     * Wakes a bot up
     *
     * @param botId The identifier of the bot to wake up
     * @return The protocol reply
     */
    XSPReply wakeup(String botId);

    /**
     * Puts a bot to sleep
     *
     * @param botId The identifier of the bot to put to sleep
     * @return The protocol reply
     */
    XSPReply putToSleep(String botId);
}
