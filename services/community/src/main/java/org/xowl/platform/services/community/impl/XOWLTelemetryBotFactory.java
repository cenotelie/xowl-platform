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

import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.services.community.bots.Bot;
import org.xowl.platform.services.community.bots.BotFactory;
import org.xowl.platform.services.community.bots.BotSpecification;

/**
 * Implements a factory of telemetry bots
 *
 * @author Laurent Wouters
 */
public class XOWLTelemetryBotFactory implements BotFactory {
    @Override
    public String getIdentifier() {
        return XOWLTelemetryBotFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Telemetry Bot Factory";
    }

    @Override
    public Bot newBot(BotSpecification specification) {
        if (XOWLTelemetryBot.class.getCanonicalName().equals(specification.getBotType()))
            return new XOWLTelemetryBot(specification);
        return null;
    }
}
