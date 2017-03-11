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

import org.xowl.infra.utils.RichString;
import org.xowl.infra.utils.http.HttpConnection;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.services.community.bots.BotBase;
import org.xowl.platform.services.community.bots.BotSpecification;

/**
 * Implements a bot that transmits statistics about the platform
 *
 * @author Laurent Wouters
 */
public class XOWLTelemetryBot extends BotBase {
    /**
     * The number of seconds to wait between two connections
     */
    private final long interval;
    /**
     * The connection to the remote gathering service
     */
    private final HttpConnection connection;
    /**
     * The timestamp the last time the bot sent data
     */
    private long lastTimestamp;

    /**
     * Initializes this bot
     *
     * @param specification The specification for this bot
     */
    public XOWLTelemetryBot(BotSpecification specification) {
        super(specification);
        this.interval = Long.parseLong(specification.getValueFor("interval").toString());
        this.connection = new HttpConnection(specification.getValueFor("target").toString());
        this.lastTimestamp = Long.MIN_VALUE;
    }

    /**
     * Gets whether this bot has work to do
     *
     * @return Whether this bot has work to do
     */
    protected boolean hasWorkToDo() {
        long now = System.nanoTime();
        return (now >= lastTimestamp + (interval * 1000000000));
    }

    /**
     * Executes some work
     *
     * @return true if the bot must stop, false otherwise
     */
    protected boolean doWork() {
        Logging.get().info(new RichString(this, " should send data now."));
        lastTimestamp = System.nanoTime();
        return false;
    }
}
