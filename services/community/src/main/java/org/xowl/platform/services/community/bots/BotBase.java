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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.api.ReplyApiError;
import org.xowl.infra.utils.api.ReplyResultCollection;
import org.xowl.infra.utils.api.ReplySuccess;
import org.xowl.infra.utils.concurrent.SafeRunnable;
import org.xowl.infra.utils.logging.DispatchLogger;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ReplyServiceUnavailable;
import org.xowl.platform.kernel.events.Event;
import org.xowl.platform.kernel.events.EventConsumer;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.platform.PlatformLogBuffer;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecurityService;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base implementation of a bot
 *
 * @author Laurent Wouters
 */
public class BotBase implements Bot, EventConsumer {
    /**
     * The maximum number of messages to keep for this bot
     */
    private static final int MESSAGES_BOUND = 32;
    /**
     * The maximum number of queued events
     */
    private static final int QUEUE_LENGTH = 128;
    /**
     * The time to wait for events, in ms
     */
    private static final long WAIT_TIME = 500;

    /**
     * The identifier of the bot
     */
    protected final String identifier;
    /**
     * The name of the bot
     */
    protected final String name;
    /**
     * The type of this bot
     */
    protected final String botType;
    /**
     * The associated security user
     */
    protected final PlatformUser securityUser;
    /**
     * Whether this bot should be woken up when the platform starts
     */
    protected final boolean wakeupOnStartup;
    /**
     * The status of the bot
     */
    private BotStatus status;
    /**
     * The messages of this bot
     */
    private final PlatformLogBuffer logBuffer;

    /*
     * Bot's implementation data
     */

    /**
     * Flag whether the dispatcher thread must stop
     */
    private final AtomicBoolean mustStop;
    /**
     * The queue of events received by this bot
     */
    private final BlockingQueue<Event> queue;
    /**
     * The execution thread for this bot
     */
    private Thread thread;

    /**
     * Initializes this bot
     *
     * @param specification The specification for the bot
     */
    public BotBase(BotSpecification specification) {
        this.identifier = specification.getIdentifier();
        this.name = specification.getName();
        this.botType = specification.getBotType();
        this.wakeupOnStartup = specification.getWakeupOnStartup();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService != null) {
            String userId = specification.getSecurityUser();
            if (userId == null || userId.isEmpty())
                userId = identifier;
            this.securityUser = securityService.getRealm().getUser(userId);
        } else
            this.securityUser = null;
        this.status = this.securityUser != null ? BotStatus.Asleep : BotStatus.Invalid;
        this.logBuffer = new PlatformLogBuffer(MESSAGES_BOUND);
        this.mustStop = new AtomicBoolean(false);
        this.queue = new ArrayBlockingQueue<>(QUEUE_LENGTH);
    }

    /**
     * Initializes this bot
     *
     * @param definition The JSON definition
     */
    public BotBase(ASTNode definition) {
        String identifier = null;
        String name = null;
        String botType = null;
        boolean wakeupOnStartup = false;
        String securityUser = null;
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("botType".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                botType = value.substring(1, value.length() - 1);
            } else if ("wakeupOnStartup".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                wakeupOnStartup = Boolean.parseBoolean(value);
            } else if ("securityUser".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                securityUser = value.substring(1, value.length() - 1);
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.botType = botType;
        this.wakeupOnStartup = wakeupOnStartup;
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService != null) {
            if (securityUser == null || securityUser.isEmpty())
                securityUser = identifier;
            this.securityUser = securityService.getRealm().getUser(securityUser);
        } else
            this.securityUser = null;
        this.status = this.securityUser != null ? BotStatus.Asleep : BotStatus.Invalid;
        this.logBuffer = new PlatformLogBuffer(MESSAGES_BOUND);
        this.mustStop = new AtomicBoolean(false);
        this.queue = new ArrayBlockingQueue<>(QUEUE_LENGTH);
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
        return securityUser;
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
    public Reply getMessages() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(BotManagementService.ACTION_GET_MESSAGES, this);
        if (!reply.isSuccess())
            return reply;

        return new ReplyResultCollection<>(logBuffer.getMessages());
    }

    @Override
    public Reply wakeup() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(BotManagementService.ACTION_WAKE_UP, this);
        if (!reply.isSuccess())
            return reply;

        synchronized (this) {
            if (status != BotStatus.Asleep)
                return new ReplyApiError(BotManagementService.ERROR_INVALID_STATUS, "Bot is not asleep: " + status);
            status = BotStatus.WakingUp;
        }
        mustStop.set(false);
        thread = new Thread(getRunnable(), Bot.class.getName() + " - " + identifier);
        thread.start();
        status = BotStatus.Awaken;
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new BotWokeUpEvent(this));
        return ReplySuccess.instance();
    }

    @Override
    public Reply sleep() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.checkAction(BotManagementService.ACTION_SLEEP, this);
        if (!reply.isSuccess())
            return reply;

        synchronized (this) {
            if (status != BotStatus.Awaken && status != BotStatus.Working)
                return new ReplyApiError(BotManagementService.ERROR_INVALID_STATUS, "Bot is not awake: " + status);
            status = BotStatus.GoingToSleep;
        }
        mustStop.set(true);
        try {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
                thread.join();
            }
            thread = null;
        } catch (InterruptedException exception) {
            Logging.get().error(exception);
        }
        status = BotStatus.Asleep;
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new BotHasGoneToSleepEvent(this));
        return ReplySuccess.instance();
    }

    @Override
    public void onEvent(Event event) {
        queue.offer(event);
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
                "\", \"botType\": \"" +
                TextUtils.escapeStringJSON(botType) +
                "\", \"wakeupOnStartup\": " +
                Boolean.toString(wakeupOnStartup) +
                ", \"securityUser\": \"" +
                (securityUser != null ? TextUtils.escapeStringJSON(securityUser.getIdentifier()) : "") +
                "\", \"status\": \"" +
                status.toString() +
                "\"}";
    }

    /**
     * Gets the runnable for the bot
     *
     * @return The runnable
     */
    private Runnable getRunnable() {
        return new SafeRunnable() {
            @Override
            public void doRun() {
                doBotRun();
            }

            @Override
            protected void onRunFailed(Throwable throwable) {
                onBotStop();
            }
        };
    }

    /**
     * Main method for the bot
     */
    private void doBotRun() {
        // register the logger for this thread
        Logging.set(new DispatchLogger(Logging.getDefault(), logBuffer));

        // authenticate the bot on this thread
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService != null && securityUser != null)
            securityService.authenticate(securityUser);

        while (!mustStop.get()) {
            if (hasWorkToDo()) {
                status = BotStatus.Working;
                if (doWork()) {
                    onBotStop();
                    return;
                } else {
                    status = BotStatus.Awaken;
                    continue;
                }
            }
            try {
                Event event = queue.poll(WAIT_TIME, TimeUnit.MILLISECONDS);
                if (event != null) {
                    status = BotStatus.Working;
                    if (reactTo(event)) {
                        onBotStop();
                        return;
                    } else {
                        status = BotStatus.Awaken;
                    }
                }
            } catch (InterruptedException exception) {
                onBotStop();
                return;
            }
        }
    }

    /**
     * When the bot is stopping for a reason other that an external request
     */
    private void onBotStop() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService != null)
            securityService.logout();
        status = BotStatus.Asleep;
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new BotHasGoneToSleepEvent(BotBase.this));
    }

    /**
     * Gets whether this bot has work to do
     *
     * @return Whether this bot has work to do
     */
    protected boolean hasWorkToDo() {
        return false;
    }

    /**
     * Executes some work
     *
     * @return true if the bot must stop, false otherwise
     */
    protected boolean doWork() {
        return false;
    }

    /**
     * Reacts to the received event
     *
     * @param event The event to react to
     * @return true if the bot must stop, false otherwise
     */
    protected boolean reactTo(Event event) {
        return false;
    }
}
