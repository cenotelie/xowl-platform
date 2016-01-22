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

package org.xowl.platform.services.config.impl;

import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.services.config.ConfigurationService;
import org.xowl.platform.utils.Utils;
import org.xowl.utils.config.Configuration;
import org.xowl.utils.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * An implementation of the configuration service that relies on the file system
 *
 * @author Laurent Wouters
 */
public class FSConfigurationService implements ConfigurationService {
    /**
     * The supported extension for configuration files
     */
    private static final String CONFIG_EXT = ".ini";

    /**
     * The root folder for the configuration
     */
    private final File root;

    /**
     * Initializes this service
     */
    public FSConfigurationService() {
        root = new File(System.getProperty(Env.CONF_DIR));
    }

    @Override
    public String getIdentifier() {
        return FSConfigurationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Configuration Service";
    }

    @Override
    public Configuration getConfigFor(Identifiable entity) {
        Configuration configuration = new Configuration();
        File file = new File(root, entity.getIdentifier() + CONFIG_EXT);
        if (file.exists() && file.canRead()) {
            try (FileInputStream stream = new FileInputStream(file)) {
                configuration.load(stream, Utils.DEFAULT_CHARSET);
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
        }
        return configuration;
    }

    @Override
    public File resolve(String file) {
        return new File(root, file);
    }
}