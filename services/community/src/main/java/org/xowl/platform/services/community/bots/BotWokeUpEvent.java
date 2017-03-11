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

import org.xowl.infra.utils.RichString;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a bot just work up
 *
 * @author Laurent Wouters
 */
public class BotWokeUpEvent extends EventBase {
    /**
     * The bot that work up
     */
    private final Bot bot;

    /**
     * Gets the bot that work up
     *
     * @return The bot that work up
     */
    public Bot getBot() {
        return bot;
    }

    /**
     * Initializes this event
     *
     * @param bot The bot that work up
     */
    public BotWokeUpEvent(Bot bot) {
        super(
                new RichString(bot, " just work up"),
                BotWokeUpEvent.class.getCanonicalName(),
                Register.getComponent(BotManagementService.class));
        this.bot = bot;
    }
}
