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

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.hime.redist.ASTNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * The descriptor for a remote connector
 *
 * @author Laurent Wouters
 */
public class ConnectorDescriptorRemote extends ConnectorDescriptorBase {
    /**
     * The parameters for the descriptor
     */
    private final Collection<ConnectorDescriptorParam> parameters;

    /**
     * Initializes this description
     *
     * @param definition The AST node for the serialized definition
     */
    public ConnectorDescriptorRemote(ASTNode definition) {
        super(definition);
        this.parameters = new ArrayList<>();

        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            switch (head) {
                case "parameters": {
                    for (ASTNode child : member.getChildren().get(1).getChildren())
                        parameters.add(new ConnectorDescriptorParam(child));
                    break;
                }
            }
        }
    }

    @Override
    public Collection<ConnectorDescriptorParam> getParameters() {
        return Collections.unmodifiableCollection(parameters);
    }
}
