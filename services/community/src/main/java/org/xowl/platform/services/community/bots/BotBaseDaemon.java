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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.concurrent.SafeRunnable;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.security.SecurityService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base implementation of a bot that is backed by a thread
 *
 * @author Laurent Wouters
 */
public abstract class BotBaseDaemon extends BotBase {
    /**
     * The thread dispatching events
     */
    private final Thread thread;
    /**
     * Flag whether the dispatcher thread must stop
     */
    private final AtomicBoolean mustStop;

    /**
     * Initializes this bot
     *
     * @param specification The specification for the bot
     */
    public BotBaseDaemon(BotSpecification specification) {
        super(specification);
        this.mustStop = new AtomicBoolean(false);
        this.thread = new Thread(getRunnable(), Bot.class.getName() + " - " + identifier);
    }

    /**
     * Initializes this bot
     *
     * @param definition The JSON definition
     */
    public BotBaseDaemon(ASTNode definition) {
        super(definition);
        this.mustStop = new AtomicBoolean(false);
        this.thread = new Thread(getRunnable(), Bot.class.getName() + " - " + identifier);
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
                SecurityService securityService = Register.getComponent(SecurityService.class);
                if (securityService != null && platformUser != null)
                    securityService.authenticate(platformUser);
                while (!mustStop.get()) {
                    if (onRun()) {
                        if (!mustStop.get())
                            status = BotStatus.Asleep;
                        return;
                    }
                }
            }

            @Override
            protected void onRunFailed(Throwable throwable) {
                status = BotStatus.Asleep;
            }
        };
    }

    @Override
    protected void onWakeup() {
        mustStop.set(false);
        thread.start();
    }

    @Override
    protected void onGoingToSleep() {
        mustStop.set(true);
        try {
            if (thread.isAlive())
                thread.join();
        } catch (InterruptedException exception) {
            Logging.get().error(exception);
        }
    }

    /**
     * Executes this bot for a while
     *
     * @return true if the bot must stop, false otherwise
     */
    protected abstract boolean onRun();
}
