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

package org.xowl.platform.satellites.activiti;

import org.xowl.infra.utils.config.Configuration;
import org.xowl.platform.satellites.base.RemotePlatform;

import java.io.IOException;

/**
 * Utility API
 *
 * @author Laurent Wouters
 */
public class ActionUtils {
    /**
     * The remote platform
     */
    private static RemotePlatform PLATFORM;

    /**
     * Gets the target platform
     *
     * @return The target platform
     */
    public static synchronized RemotePlatform getPlatform() {
        if (PLATFORM != null)
            return PLATFORM;
        Configuration configuration = new Configuration();
        try {
            configuration.load("/config/activiti.ini");
            String endpoint = configuration.get("endpoint");
            String login = configuration.get("login");
            String password = configuration.get("password");
            PLATFORM = new RemotePlatform(endpoint, login, password);
            return PLATFORM;
        } catch (IOException exception) {
            return null;
        }
    }
}
