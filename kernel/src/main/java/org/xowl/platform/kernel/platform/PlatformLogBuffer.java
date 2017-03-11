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

package org.xowl.platform.kernel.platform;

import org.xowl.infra.utils.logging.Logger;
import org.xowl.platform.kernel.events.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements a buffered of log messages for the platform
 *
 * @author Laurent Wouters
 */
public class PlatformLogBuffer implements Logger {
    /**
     * The size of the buffer to use
     */
    private final int bufferSize;
    /**
     * The buffer of the last messages
     */
    private final PlatformLogMessage[] messages;
    /**
     * The head of the buffer of messages
     */
    private final AtomicInteger head;

    /**
     * Initializes this buffer
     *
     * @param bufferSize The size of the buffer to use
     */
    public PlatformLogBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.messages = new PlatformLogMessage[bufferSize];
        this.head = new AtomicInteger(-1);
    }

    @Override
    public void debug(Object message) {
        if (message instanceof Event)
            message = ((Event) message).getDescription();
        onLogMessage("DEBUG", message);
    }

    @Override
    public void info(Object message) {
        if (message instanceof Event)
            message = ((Event) message).getDescription();
        onLogMessage("INFO", message);
    }

    @Override
    public void warning(Object message) {
        if (message instanceof Event)
            message = ((Event) message).getDescription();
        onLogMessage("WARNING", message);
    }

    @Override
    public void error(Object message) {
        if (message instanceof Event)
            message = ((Event) message).getDescription();
        onLogMessage("ERROR", message);
    }

    /**
     * When a message is received
     *
     * @param level   The log level for the message
     * @param content The message's content
     */
    private void onLogMessage(String level, Object content) {
        PlatformLogMessage message = new PlatformLogMessage(content, level);
        while (true) {
            int headValue = head.get();
            int insertion = headValue + 1;
            if (insertion >= bufferSize)
                insertion = 0;
            if (head.compareAndSet(headValue, insertion)) {
                messages[insertion] = message;
                break;
            }
        }
    }

    /**
     * Gets the current messages
     *
     * @return The current messages
     */
    public List<PlatformLogMessage> getMessages() {
        int current = head.get();
        if (current == -1)
            return Collections.emptyList();
        List<PlatformLogMessage> result = new ArrayList<>();
        for (int i = 0; i != bufferSize; i++) {
            if (messages[current] == null)
                return result;
            result.add(messages[current]);
            current--;
            if (current == -1)
                current = bufferSize - 1;
        }
        return result;
    }
}
