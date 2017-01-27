/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.satellites.base;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.api.XOWLFactory;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.infra.utils.http.HttpConnection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The base API for accessing a remote platform
 *
 * @author Laurent Wouters
 */
public class RemotePlatform {
    /**
     * The connection to the platform
     */
    private final HttpConnection connection;
    /**
     * The factory of remote objects
     */
    private final Collection<RemoteFactory> factories;
    /**
     * The aggregated factory for
     */
    private final XOWLFactory aggregatedFactory;

    /**
     * Initializes this platform connection
     *
     * @param endpoint The API endpoint (https://something:port/api/)
     */
    public RemotePlatform(String endpoint) {
        this.connection = new HttpConnection(endpoint);
        this.factories = new ArrayList<>();
        this.aggregatedFactory = new XOWLFactory() {
            @Override
            public Object newObject(String type, ASTNode definition) {
                for (RemoteFactory factory : factories) {
                    Object result = factory.newObject(type, definition);
                    if (result != null)
                        return result;
                }
                return null;
            }
        };
    }

    /**
     * Registers a factory for remote objects
     *
     * @param factory The factory to add
     */
    public void addFactory(RemoteFactory factory) {
        factories.add(factory);
    }

    public XSPReply doRequest(String uriComplement, String method, byte[] body, String contentType, boolean compressed, String accept) {
        return XSPReplyUnsupported.instance();
    }
}
