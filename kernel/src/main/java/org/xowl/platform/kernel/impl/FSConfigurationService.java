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

package org.xowl.platform.kernel.impl;

import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.security.SecuredAction;

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
    public FSConfigurationService() {
        File root = new File(System.getProperty(Env.ROOT));
        directory = new File(root, CONFIG_DIR);
    }

    @Override
    public String getIdentifier() {
        return FSConfigurationService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - Configuration Service";
    }

    @Override
    public SecuredAction[] getActions() {
        // no action for the configuration service
        return SecuredService.ACTIONS_NONE;
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
            try (FileInputStream stream = new FileInputStream(file)) {
                configuration.load(stream, Files.CHARSET);
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
            }
        }
        return configuration;
    }
}
