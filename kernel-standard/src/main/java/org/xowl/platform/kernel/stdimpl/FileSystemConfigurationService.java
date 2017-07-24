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

package org.xowl.platform.kernel.stdimpl;

import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.PlatformUtils;

import java.io.File;
import java.io.IOException;

/**
 * An implementation of the configuration service that relies on the file system
 *
 * @author Laurent Wouters
 */
public class FileSystemConfigurationService implements ConfigurationService {
    /**
     * From the distribution's root, the path to the configuration
     */
    private static final String CONFIG_DIR = "config";
    /**
     * The supported extension for configuration files
     */
    private static final String CONFIG_EXT = ".ini";

    /**
     * The directory for the configuration
     */
    private final File directory;

    /**
     * Initializes this service
     */
    public FileSystemConfigurationService() {
        this.directory = PlatformUtils.resolve(CONFIG_DIR);
    }

    @Override
    public String getIdentifier() {
        return FileSystemConfigurationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Configuration Service";
    }

    @Override
    public Configuration getConfigFor(Identifiable entity) {
        return getConfigFor(entity.getIdentifier());
    }

    @Override
    public Configuration getConfigFor(String entityId) {
        Configuration configuration = new Configuration();
        File file = new File(directory, entityId + CONFIG_EXT);
        if (file.exists() && file.canRead()) {
            try {
                configuration.load(file);
            } catch (IOException exception) {
                Logging.get().error(exception);
            }
        }
        return configuration;
    }
}