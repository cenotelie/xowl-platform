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

package org.xowl.platform.kernel;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.infra.utils.logging.*;
import org.xowl.platform.kernel.artifacts.BusinessDirectoryService;
import org.xowl.platform.kernel.artifacts.FreeArtifactArchetype;
import org.xowl.platform.kernel.artifacts.SchemaArtifactArchetype;
import org.xowl.platform.kernel.artifacts.SchemaDomain;
import org.xowl.platform.kernel.impl.*;
import org.xowl.platform.kernel.platform.PlatformDescriptorService;
import org.xowl.platform.kernel.security.SecurityService;

import java.io.File;

/**
 * Activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Logger mainLogger = new DispatchLogger(new FileLogger(new File(System.getenv(Env.ROOT), "platform.log")), new ConsoleLogger());
        Logging.setDefault(mainLogger);
        mainLogger.info("=== Platform startup ===");
        PlatformDescriptorService platformDescriptorService = new XOWLPlatformDescriptorService();
        ConfigurationService configurationService = new FSConfigurationService();
        XOWLSecurityService securityService = new XOWLSecurityService(configurationService);
        XOWLBusinessDirectoryService directoryService = new XOWLBusinessDirectoryService();
        directoryService.register(KernelSchema.IMPL);
        directoryService.register(SchemaArtifactArchetype.INSTANCE);
        directoryService.register(FreeArtifactArchetype.INSTANCE);
        directoryService.register(SchemaDomain.INSTANCE);
        bundleContext.registerService(PlatformDescriptorService.class, platformDescriptorService, null);
        bundleContext.registerService(ConfigurationService.class, configurationService, null);
        bundleContext.registerService(SecurityService.class, securityService, null);
        bundleContext.registerService(BusinessDirectoryService.class, directoryService, null);
        bundleContext.registerService(HttpAPIService.class, platformDescriptorService, null);
        bundleContext.registerService(HttpAPIService.class, securityService, null);
        bundleContext.registerService(HttpAPIService.class, directoryService, null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Logging.getDefault().info("=== Platform shutdown ===");
    }
}
