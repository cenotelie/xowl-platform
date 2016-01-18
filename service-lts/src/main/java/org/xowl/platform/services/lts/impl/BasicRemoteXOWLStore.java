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

import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.config.ConfigurationService;
import org.xowl.store.storage.remote.HTTPConnection;
import org.xowl.utils.config.Configuration;

/**
 * Represents a remote store on a xOWL server that is expected to be configured through a configuration element with the specified form:
 * endpoint = https://example.com/api
 * login = login
 * password = password
 * configName = dbName
 *
 * @author Laurent Wouters
 */
class BasicRemoteXOWLStore extends RemoteXOWLStore {
    /**
     * The parent service
     */
    private final RemoteXOWLStoreService service;
    /**
     * The name in the configuration for this store
     */
    private final String configName;
    /**
     * The cached HTTP connection
     */
    private HTTPConnection connection;

    /**
     * Initializes this store
     *
     * @param service    The parent service
     * @param configName The name in the configuration for this store
     */
    public BasicRemoteXOWLStore(RemoteXOWLStoreService service, String configName) {
        this.service = service;
        this.configName = configName;
    }

    @Override
    protected String getDBName() {
        return configName;
    }

    @Override
    protected HTTPConnection getConnection() {
        if (connection == null) {
            ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
            if (configurationService == null)
                return null;
            Configuration configuration = configurationService.getConfigFor(service);
            if (configuration == null)
                return null;
            String endpoint = configuration.get("endpoint");
            if (endpoint == null)
                return null;
            connection = new HTTPConnection(
                    endpoint + "/db/" + configuration.get(configName),
                    configuration.get("login"),
                    configuration.get("password"));
        }
        return connection;
    }
}
