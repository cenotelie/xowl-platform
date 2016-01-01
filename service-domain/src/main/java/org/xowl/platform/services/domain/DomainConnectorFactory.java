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

package org.xowl.platform.services.domain;

import org.xowl.platform.kernel.Service;

import java.util.Collection;
import java.util.Map;

/**
 * A factory that can create new connectors for the platform
 *
 * @author Laurent Wouters
 */
public interface DomainConnectorFactory extends Service {
    /**
     * Gets the domains supported by this factory
     *
     * @return The supported domains
     */
    Collection<DomainDescription> getDomains();

    /**
     * Instantiates a new connector for a domain
     *
     * @param description The domain's description
     * @param identifier  The new connector's unique identifier
     * @param name        The new connector's name
     * @param uris        The new connector's API uris, if any
     * @param parameters  The parameters for the new connector, if any
     * @return The new connector
     */
    DomainConnectorService newConnector(DomainDescription description, String identifier, String name, String[] uris, Map<DomainDescriptionParam, Object> parameters);
}
