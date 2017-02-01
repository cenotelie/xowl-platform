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

package org.xowl.platform.connectors.csv;

import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.services.connection.*;

import java.util.Map;

/**
 * Factory of CSV connectors
 *
 * @author Laurent Wouters
 */
public class CSVConnectorFactory implements ConnectorServiceFactory {
    /**
     * The singleton instance of the factory
     */
    public static final CSVConnectorFactory INSTANCE = new CSVConnectorFactory();

    @Override
    public String getIdentifier() {
        return CSVConnectorFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - CSV Connector Factory";
    }

    @Override
    public ConnectorService newConnector(ConnectorDescriptor description, ConnectorServiceData specification) {
        return new CSVConnector(specification);
    }
}
