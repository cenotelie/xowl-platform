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

/**
 * Represents the status of a bot
 *
 * @author Laurent Wouters
 */
public enum BotStatus {
    /**
     * The bot is invalid, or the request that lead to this result is invalid
     */
    Invalid,
    /**
     * The bot is asleep (not running)
     */
    Asleep,
    /**
     * The bot is currently waking up
     */
    WakingUp,
    /**
     * The bot is awaken, but not currently doing something (may be waiting for inputs)
     */
    Awaken,
    /**
     * The bot is awaken and doing some work (i.e. running and consuming CPU cycles)
     */
    Working,
    /**
     * The bot is currently going to sleep
     */
    GoingToSleep
}
