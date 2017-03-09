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

package org.xowl.platform.kernel.bots;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecurityService;

/**
 * Base implementation of a bot
 *
 * @author Laurent Wouters
 */
public abstract class BotBase implements Bot {
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
    protected BotStatus status;

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
        if (securityService != null)
            this.platformUser = securityService.getRealm().getUser(identifier);
        else
            this.platformUser = null;
        this.status = BotStatus.Asleep;
    }

    /**
     * Initializes this bot
     *
     * @param definition The JSON definition
     */
    public BotBase(ASTNode definition) {
        String identifier = null;
        String name = null;
        boolean wakeupOnStartup = false;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("wakeupOnStartup".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                wakeupOnStartup = Boolean.parseBoolean(value);
            }
        }
        PlatformUser platformUser = null;
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService != null)
            platformUser = securityService.getRealm().getUser(identifier);
        this.identifier = identifier;
        this.name = name;
        this.wakeupOnStartup = wakeupOnStartup;
        this.platformUser = platformUser;
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
        synchronized (this) {
            if (status != BotStatus.Asleep)
                return new XSPReplyApiError(ERROR_INVALID_STATUS, "Bot is not asleep: " + status);
            status = BotStatus.WakingUp;
        }
        onWakeup();
        status = BotStatus.Awaken;
        return XSPReplySuccess.instance();
    }

    @Override
    public XSPReply sleep() {
        synchronized (this) {
            if (status != BotStatus.Awaken && status != BotStatus.Working)
                return new XSPReplyApiError(ERROR_INVALID_STATUS, "Bot is not awake: " + status);
            status = BotStatus.GoingToSleep;
        }
        onGoingToSleep();
        status = BotStatus.Asleep;
        return XSPReplySuccess.instance();
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

    /**
     * Reacts to the bot being woken up
     */
    protected abstract void onWakeup();

    /**
     * Reacts to the bot going to sleep
     */
    protected abstract void onGoingToSleep();
}
