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

import org.xowl.platform.kernel.Registrable;

/**
 * A factory of bots that is used to produce bots from their serialized representation
 *
 * @author Laurent Wouters
 */
public interface BotFactory extends Registrable {
    /**
     * Creates a bot from the specified definition
     *
     * @param specification The specification for the bot
     * @return The created bot, or null if it cannot be created
     */
    Bot newBot(BotSpecification specification);
}
