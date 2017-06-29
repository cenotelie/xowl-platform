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

package org.xowl.platform.kernel.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.artifacts.BusinessDirectoryService;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.PlatformRebootJob;
import org.xowl.platform.kernel.security.*;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.kernel.statistics.StatisticsService;
import org.xowl.platform.kernel.stdimpl.*;
import org.xowl.platform.kernel.webapi.HttpApiDiscoveryService;
import org.xowl.platform.kernel.webapi.HttpApiService;

/**
 * Activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        Register.waitFor(PlatformLocator.class, new RegisterWaiter<PlatformLocator>() {
            @Override
            public void onAvailable(BundleContext bundleContext, PlatformLocator component) {
                // register the configuration service
                ConfigurationService configurationService = new FileSystemConfigurationService();
                bundleContext.registerService(Service.class, configurationService, null);
                bundleContext.registerService(ConfigurationService.class, configurationService, null);

                // register the HTTP configuration
                PlatformHttp platformHttp = PlatformHttp.initialize(configurationService);
                bundleContext.registerService(PlatformHttp.class, platformHttp, null);

                // register the logging service
                KernelLoggingService loggingService = new KernelLoggingService();
                Logging.setDefault(loggingService);
                bundleContext.registerService(Service.class, loggingService, null);
                bundleContext.registerService(SecuredService.class, loggingService, null);
                bundleContext.registerService(HttpApiService.class, loggingService, null);
                bundleContext.registerService(MeasurableService.class, loggingService, null);
                bundleContext.registerService(LoggingService.class, loggingService, null);

                // register the security service
                KernelSecurityProvider securityProvider = new KernelSecurityProvider();
                bundleContext.registerService(SecurityRealmProvider.class, securityProvider, null);
                bundleContext.registerService(SecurityPolicyProvider.class, securityProvider, null);
                bundleContext.registerService(SecuredActionPolicyProvider.class, securityProvider, null);
                KernelSecurityService securityService = new KernelSecurityService(configurationService);
                bundleContext.registerService(Service.class, securityService, null);
                bundleContext.registerService(SecuredService.class, securityService, null);
                bundleContext.registerService(HttpApiService.class, securityService, null);
                bundleContext.registerService(SecurityService.class, securityService, null);

                // register the statistics service
                KernelStatisticsService statisticsService = new KernelStatisticsService();
                bundleContext.registerService(Service.class, statisticsService, null);
                bundleContext.registerService(HttpApiService.class, statisticsService, null);
                bundleContext.registerService(SecuredService.class, statisticsService, null);
                bundleContext.registerService(StatisticsService.class, statisticsService, null);

                // register the event service
                KernelEventService serviceEvents = new KernelEventService();
                bundleContext.registerService(Service.class, serviceEvents, null);
                bundleContext.registerService(MeasurableService.class, serviceEvents, null);
                bundleContext.registerService(ManagedService.class, serviceEvents, null);
                bundleContext.registerService(EventService.class, serviceEvents, null);

                // register the job executor service
                KernelJobExecutor serviceJobExecutor = new KernelJobExecutor(configurationService);
                bundleContext.registerService(Service.class, serviceJobExecutor, null);
                bundleContext.registerService(SecuredService.class, serviceJobExecutor, null);
                bundleContext.registerService(HttpApiService.class, serviceJobExecutor, null);
                bundleContext.registerService(MeasurableService.class, serviceJobExecutor, null);
                bundleContext.registerService(ManagedService.class, serviceJobExecutor, null);
                bundleContext.registerService(JobExecutionService.class, serviceJobExecutor, null);
                bundleContext.registerService(JobFactory.class, new KernelJobFactory(), null);

                // register the directory service
                KernelBusinessDirectoryService directoryService = new KernelBusinessDirectoryService();
                bundleContext.registerService(Service.class, directoryService, null);
                bundleContext.registerService(HttpApiService.class, directoryService, null);
                bundleContext.registerService(BusinessDirectoryService.class, directoryService, null);

                // register the platform management service
                KernelPlatformManagementService servicePlatform = new KernelPlatformManagementService(configurationService);
                bundleContext.registerService(Service.class, servicePlatform, null);
                bundleContext.registerService(SecuredService.class, servicePlatform, null);
                bundleContext.registerService(HttpApiService.class, servicePlatform, null);
                bundleContext.registerService(MeasurableService.class, servicePlatform, null);
                bundleContext.registerService(PlatformManagementService.class, servicePlatform, null);

                // register the HTTP API discovery service
                KernelHttpApiDiscoveryService discoveryService = new KernelHttpApiDiscoveryService();
                bundleContext.registerService(Service.class, discoveryService, null);
                bundleContext.registerService(HttpApiService.class, discoveryService, null);
                bundleContext.registerService(HttpApiDiscoveryService.class, discoveryService, null);

                boolean mustReboot = KernelPlatformManagementService.enforceHttpConfigFelix(platformHttp);
                if (mustReboot)
                    serviceJobExecutor.schedule(new PlatformRebootJob());
            }
        }, bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }
}
