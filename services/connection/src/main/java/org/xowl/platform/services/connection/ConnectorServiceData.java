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

package org.xowl.platform.services.connection;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.json.Json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data required for spawning a connector
 *
 * @author Laurent Wouters
 */
public class ConnectorServiceData implements Identifiable, Serializable {
    /**
     * The identifier for this connector
     */
    protected final String identifier;
    /**
     * The name for this connector
     */
    protected final String name;
    /**
     * The API URIs for this connector
     */
    protected final String[] uris;
    /**
     * The parameters for this connector
     */
    protected final Map<ConnectorDescriptorParam, Object> parameters;

    /**
     * Initializes this structure
     *
     * @param identifier The identifier for this connector
     * @param name       The name for this connector
     * @param uris       The API URIs for this connector
     */
    public ConnectorServiceData(String identifier, String name, String[] uris) {
        this.identifier = identifier;
        this.name = name;
        this.uris = uris;
        this.parameters = new HashMap<>();
    }

    /**
     * Initializes this structure
     *
     * @param descriptor The target connector descriptor
     * @param definition The serialized definition
     */
    public ConnectorServiceData(ConnectorDescriptor descriptor, ASTNode definition) {
        this.parameters = new HashMap<>();
        String identifier = "";
        String name = "";
        Collection<String> uris = new ArrayList<>(2);

        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("uris".equals(head)) {
                ASTNode valueNode = member.getChildren().get(1);
                if (valueNode.getValue() != null) {
                    String value = TextUtils.unescape(valueNode.getValue());
                    value = value.substring(1, value.length() - 1);
                    uris.add(value);
                } else if (valueNode.getChildren().size() > 0) {
                    for (ASTNode childNode : valueNode.getChildren()) {
                        String value = TextUtils.unescape(childNode.getValue());
                        value = value.substring(1, value.length() - 1);
                        uris.add(value);
                    }
                }
            } else {
                ConnectorDescriptorParam parameter = null;
                for (ConnectorDescriptorParam p : descriptor.getParameters()) {
                    if (p.getIdentifier().equals(head)) {
                        parameter = p;
                        break;
                    }
                }
                if (parameter != null) {
                    ASTNode valueNode = member.getChildren().get(1);
                    if (valueNode.getValue() != null) {
                        String value = TextUtils.unescape(valueNode.getValue());
                        if (value.startsWith("\"") && value.endsWith("\""))
                            value = value.substring(1, value.length() - 1);
                        parameters.put(parameter, value);
                    } else if (valueNode.getChildren().size() > 0) {
                        Collection<String> values = new ArrayList<>();
                        for (int i = 0; i != valueNode.getChildren().size(); i++) {
                            String value = TextUtils.unescape(valueNode.getChildren().get(i).getValue());
                            if (value.startsWith("\"") && value.endsWith("\""))
                                value = value.substring(1, value.length() - 1);
                            values.add(value);
                        }
                        parameters.put(parameter, values);
                    }
                }
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.uris = uris.toArray(new String[uris.size()]);
    }

    /**
     * Gets the uris for the connector
     *
     * @return The uris
     */
    public String[] getUris() {
        return uris;
    }

    /**
     * Gets the set parameters
     *
     * @return The set parameters
     */
    public Collection<ConnectorDescriptorParam> getParameters() {
        return parameters.keySet();
    }

    /**
     * Gets the value associated to a parameter
     *
     * @param parameter A parameter
     * @return The associated value
     */
    public Object getValueFor(ConnectorDescriptorParam parameter) {
        return parameters.get(parameter);
    }

    /**
     * Sets the value for a parameter
     *
     * @param parameter The parameter
     * @param value     The associated value
     */
    public void addParameter(ConnectorDescriptorParam parameter, Object value) {
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
        builder.append(TextUtils.escapeStringJSON(ConnectorServiceData.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"uris\": [");
        for (int i = 0; i != uris.length; i++) {
            if (i != 0)
                builder.append(", ");
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(uris[i]));
            builder.append("\"");
        }
        builder.append("]");
        for (Map.Entry<ConnectorDescriptorParam, Object> entry : parameters.entrySet()) {
            builder.append(", \"");
            builder.append(TextUtils.escapeStringJSON(entry.getKey().getIdentifier()));
            builder.append("\": ");
            Json.serialize(builder, entry.getValue());
        }
        builder.append("}");
        return builder.toString();
    }
}
