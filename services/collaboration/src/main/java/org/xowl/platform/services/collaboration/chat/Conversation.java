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

package org.xowl.platform.services.collaboration.chat;

import fr.cenotelie.commons.utils.Identifiable;
import fr.cenotelie.commons.utils.Serializable;
import fr.cenotelie.commons.utils.TextUtils;
import org.xowl.platform.kernel.platform.PlatformUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Represents a conversation between users on the platform
 *
 * @author Laurent Wouters
 */
public class Conversation implements Identifiable, Serializable {
    /**
     * The unique identifier for this conversation
     */
    private final String identifier;
    /**
     * The name (subject) of this conversation
     */
    private final String name;
    /**
     * The users for this conversation
     */
    private final Collection<String> users;

    /**
     * Initializes this conversation
     *
     * @param name    The name (subject) of this conversation
     * @param creator The platform user that created this conversation
     */
    public Conversation(String name, PlatformUser creator) {
        this.identifier = UUID.randomUUID().toString();
        this.name = name;
        this.users = new ArrayList<>();
        this.users.add(creator.getIdentifier());
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the users for this conversation
     *
     * @return The users for this conversation
     */
    public Collection<String> getUsers() {
        return Collections.unmodifiableCollection(users);
    }

    /**
     * Adds a user to the conversation
     *
     * @param user The user to add
     * @return Whether the operation succeed
     */
    public boolean addUser(PlatformUser user) {
        synchronized (users) {
            if (users.contains(user.getIdentifier()))
                return false;
            users.add(user.getIdentifier());
            return true;
        }
    }

    /**
     * Removes a user from this conversation
     *
     * @param user The user to remove
     * @return Whether the operation succeed
     */
    public boolean removeUser(PlatformUser user) {
        synchronized (users) {
            return users.remove(user.getIdentifier());
        }
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(Conversation.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"users\": [");
        boolean first = true;
        for (String user : users) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(user));
            builder.append("\"");
        }
        builder.append("]}");
        return builder.toString();
    }
}
