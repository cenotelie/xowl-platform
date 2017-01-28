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

package org.xowl.platform.satellites.base.objects;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;

/**
 * Represents a connector on the remote platform
 *
 * @author Laurent Wouters
 */
public class RemoteConnector {
    /**
     * The connector's name
     */
    private final String name;
    /**
     * The connector's URI
     */
    private final String uri;

    /**
     * Initializes this connector
     *
     * @param name The connector's name
     * @param uri  The connector's URI
     */
    public RemoteConnector(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    /**
     * Initializes this connector
     *
     * @param node The definition
     */
    public RemoteConnector(ASTNode node) {
        String name = null;
        String uri = null;
        for (ASTNode member : node.getChildren()) {
            String memberName = TextUtils.unescape(member.getChildren().get(0).getValue());
            memberName = memberName.substring(1, memberName.length() - 1);
            if (memberName.equals("name")) {
                name = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = name.substring(1, name.length() - 1);
            } else if (memberName.equals("uris")) {
                if (member.getChildren().get(1).getChildren().isEmpty())
                    continue;
                ASTNode firstURI = member.getChildren().get(1).getChildren().get(0);
                uri = TextUtils.unescape(firstURI.getValue());
                uri = uri.substring(1, uri.length() - 1);
            }
        }
        this.name = name;
        this.uri = uri;
    }

    /**
     * Gets the connector's name
     *
     * @return The connector's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the connector's URI
     *
     * @return The connector's URI
     */
    public String getURI() {
        return uri;
    }
}
