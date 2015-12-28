/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.lts.impl;

import org.xowl.platform.services.lts.TripleStore;
import org.xowl.store.sparql.Result;
import org.xowl.store.sparql.ResultFailure;
import org.xowl.store.storage.remote.HTTPConnection;
import org.xowl.store.xsp.XSPReply;
import org.xowl.store.xsp.XSPReplyNetworkError;

/**
 * Represents a remote xOWL store for this platform
 *
 * @author Laurent Wouters
 */
abstract class RemoteXOWLStore implements TripleStore {
    /**
     * Gets the connection for this store
     *
     * @return The connection for this store
     */
    protected abstract HTTPConnection getConnection();

    @Override
    public Result sparql(String query) {
        HTTPConnection connection = getConnection();
        if (connection == null)
            return new ResultFailure("The connection to the remote host is not configured");
        return connection.sparql(query);
    }

    @Override
    public XSPReply execute(String command) {
        HTTPConnection connection = getConnection();
        if (connection == null)
            return new XSPReplyNetworkError("The connection to the remote host is not configured");
        return connection.xsp(command);
    }
}
