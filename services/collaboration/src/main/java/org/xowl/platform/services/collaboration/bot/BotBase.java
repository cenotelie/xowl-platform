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

package org.xowl.platform.services.collaboration.bot;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecurityService;

/**
 * Base implementation of a bot
 *
 * @author Laurent Wouters
 */
public class BotBase implements Bot {
    /**
     * The identifier of the bot
     */
    protected final String identifier;
    /**
     * The name of the bot
     */
    protected final String name;
    /**
     * The associated security user
     */
    protected final PlatformUser platformUser;
    /**
     * Whether this bot should be woken up when the platform starts
     */
    protected final boolean wakeupOnStartup;
    /**
     * The status of the bot
     */
    protected final BotStatus status;

    /**
     * Initializes this bot
     *
     * @param identifier      The identifier of the bot
     * @param name            The name of the bot
     * @param wakeupOnStartup Whether this bot should be woken up when the platform starts
     */
    public BotBase(String identifier, String name, boolean wakeupOnStartup) {
        this.identifier = identifier;
        this.name = name;
        this.wakeupOnStartup = wakeupOnStartup;
        SecurityService securityService = Register.getComponent(SecurityService.class);
        this.platformUser = securityService.getRealm().getUser(identifier);
        this.status = BotStatus.Asleep;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlatformUser getSecurity() {
        return platformUser;
    }

    @Override
    public boolean getWakeupOnStartup() {
        return wakeupOnStartup;
    }

    @Override
    public BotStatus getStatus() {
        return status;
    }

    @Override
    public XSPReply wakeup() {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply sleep() {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(Bot.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"wakeupOnStartup\": " +
                Boolean.toString(wakeupOnStartup) +
                ", \"status\": \"" +
                status.toString() +
                "\"}";
    }
}
