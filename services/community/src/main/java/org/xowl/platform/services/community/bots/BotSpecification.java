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
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The specification that can be used to create a bot
 *
 * @author Laurent Wouters
 */
public class BotSpecification implements Identifiable, Serializable {
    /**
     * The identifier for the bot
     */
    private final String identifier;
    /**
     * The name for the bot
     */
    private final String name;
    /**
     * The type of bot
     */
    private final String botType;
    /**
     * The security user to use to run the bot
     */
    private final String securityUser;
    /**
     * Whether this bot should be woken up when the platform starts
     */
    private final boolean wakeupOnStartup;
    /**
     * The parameters for the bot
     */
    private final Map<String, Object> parameters;

    /**
     * Initializes this structure
     *
     * @param identifier      The identifier for this connector
     * @param name            The name for this connector
     * @param botType         The type of bot
     * @param securityUser    The security user to use to run the bot
     * @param wakeupOnStartup Whether this bot should be woken up when the platform starts
     */
    public BotSpecification(String identifier, String name, String botType, String securityUser, boolean wakeupOnStartup) {
        this.identifier = identifier;
        this.name = name;
        this.botType = botType;
        this.securityUser = securityUser;
        this.wakeupOnStartup = wakeupOnStartup;
        this.parameters = new HashMap<>();
    }

    /**
     * Initializes this structure
     *
     * @param definition The serialized definition
     */
    public BotSpecification(ASTNode definition) {
        this.parameters = new HashMap<>();
        String identifier = "";
        String name = "";
        String botType = "";
        String securityUser = "";
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
            } else if ("botType".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                botType = value.substring(1, value.length() - 1);
            } else if ("securityUser".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                securityUser = value.substring(1, value.length() - 1);
            } else if ("wakeupOnStartup".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                wakeupOnStartup = Boolean.parseBoolean(value);
            } else {
                ASTNode valueNode = member.getChildren().get(1);
                if (valueNode.getValue() != null) {
                    String value = TextUtils.unescape(valueNode.getValue());
                    if (value.startsWith("\"") && value.endsWith("\""))
                        value = value.substring(1, value.length() - 1);
                    parameters.put(head, value);
                } else if (valueNode.getChildren().size() > 0) {
                    Collection<String> values = new ArrayList<>();
                    for (int i = 0; i != valueNode.getChildren().size(); i++) {
                        String value = TextUtils.unescape(valueNode.getChildren().get(i).getValue());
                        if (value.startsWith("\"") && value.endsWith("\""))
                            value = value.substring(1, value.length() - 1);
                        values.add(value);
                    }
                    parameters.put(head, values);
                }
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.botType = botType;
        this.securityUser = securityUser;
        this.wakeupOnStartup = wakeupOnStartup;
    }

    /**
     * Gets the type of bot
     *
     * @return The type of bot
     */
    public String getBotType() {
        return botType;
    }

    /**
     * Gets the security user to use to run the bot
     *
     * @return The security user to use to run the bot
     */
    public String getSecurityUser() {
        return securityUser;
    }

    /**
     * Gets whether this bot should be woken up when the platform starts
     *
     * @return Whether this bot should be woken up when the platform starts
     */
    public boolean getWakeupOnStartup() {
        return wakeupOnStartup;
    }

    /**
     * Gets the set parameters
     *
     * @return The set parameters
     */
    public Collection<String> getParameters() {
        return parameters.keySet();
    }

    /**
     * Gets the value associated to a parameter
     *
     * @param parameter A parameter
     * @return The associated value
     */
    public Object getValueFor(String parameter) {
        return parameters.get(parameter);
    }

    /**
     * Sets the value for a parameter
     *
     * @param parameter The parameter
     * @param value     The associated value
     */
    public void addParameter(String parameter, Object value) {
        parameters.put(parameter, value);
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
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(BotSpecification.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"botType\": \"");
        builder.append(TextUtils.escapeStringJSON(botType));
        builder.append("\", \"securityUser\": \"");
        builder.append(TextUtils.escapeStringJSON(securityUser));
        builder.append("\", \"wakeupOnStartup\": ");
        builder.append(Boolean.toString(wakeupOnStartup));
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            builder.append(", \"");
            builder.append(TextUtils.escapeStringJSON(entry.getKey()));
            builder.append("\": ");
            Json.serialize(builder, entry.getValue());
        }
        builder.append("}");
        return builder.toString();
    }
}
