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

package org.xowl.platform.services.collaboration.impl;

import org.xowl.platform.services.collaboration.CollaborationNetworkService;
import org.xowl.platform.services.collaboration.CollaborationNetworkServiceProvider;

/**
 * The default provider of implementations of the collaboration network service
 *
 * @author Laurent Wouters
 */
public class XOWLNetworkProvider implements CollaborationNetworkServiceProvider {
    @Override
    public String getIdentifier() {
        return XOWLNetworkProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - Collaboration Network Service Provider";
    }

    @Override
    public CollaborationNetworkService instantiate(String identifier) {
        if (StandaloneNetworkService.class.getCanonicalName().equals(identifier))
            return new StandaloneNetworkService();
        if (MasterNetworkService.class.getCanonicalName().equals(identifier))
            return new MasterNetworkService();
        if (SlaveNetworkService.class.getCanonicalName().equals(identifier))
            return new SlaveNetworkService();
        return null;
    }
}
