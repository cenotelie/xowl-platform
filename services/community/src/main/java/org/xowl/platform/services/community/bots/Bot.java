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
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.platform.kernel.platform.PlatformLogMessage;
import org.xowl.platform.kernel.platform.PlatformUser;

import java.util.Collection;

/**
 * Represents an automated user (bot) on the platform
 * Each bot is identified by a unique identifier that is also the identifier of the security Platform User used to run the bot.
 *
 * @author Laurent Wouters
 */
public interface Bot extends Identifiable, Serializable {
    /**
     * Gets the security user that is used to run this bot
     *
     * @return The associated security user
     */
    PlatformUser getSecurity();

    /**
     * Gets whether this bot should be woken up when the platform starts
     *
     * @return Whether this bot should be woken up when the platform starts
     */
    boolean getWakeupOnStartup();

    /**
     * Gets the bot's status
     *
     * @return The bot's status
     */
    BotStatus getStatus();

    /**
     * Gets the last messages from this bot
     *
     * @return The last messages from this bot
     */
    Collection<PlatformLogMessage> getMessages();

    /**
     * Wakes this bot up (when asleep)
     *
     * @return The protocol reply
     */
    XSPReply wakeup();

    /**
     * Puts this bot to sleep (when awaken)
     *
     * @return The protocol reply
     */
    XSPReply sleep();
}
