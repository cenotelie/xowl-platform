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
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.artifacts.BusinessDirectoryService;
import org.xowl.platform.kernel.artifacts.FreeArtifactArchetype;
import org.xowl.platform.kernel.artifacts.SchemaArtifactArchetype;
import org.xowl.platform.kernel.artifacts.SchemaDomain;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.impl.*;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.PlatformStartupEvent;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.statistics.StatisticsService;

/**
 * Activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    /**
     * The platform management service
     */
    private XOWLPlatformManagementService managementService;
    /**
     * The job executor service
     */
    private XOWLJobExecutor serviceJobExecutor;
    /**
     * The event service
     */
    private XOWLEventService eventService;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        // register the logging service
        LoggingService loggingService = new XOWLLoggingService();
        Logging.setDefault(loggingService);
        bundleContext.registerService(LoggingService.class, loggingService, null);
        bundleContext.registerService(HttpAPIService.class, loggingService, null);

        // register the configuration service
        ConfigurationService configurationService = new FSConfigurationService();
        bundleContext.registerService(ConfigurationService.class, configurationService, null);

        // register the security service
        XOWLSecurityService securityService = new XOWLSecurityService(configurationService);
        bundleContext.registerService(SecurityService.class, securityService, null);
        bundleContext.registerService(HttpAPIService.class, securityService, null);

        // register the platform descriptor service
        managementService = new XOWLPlatformManagementService();
        bundleContext.registerService(PlatformManagementService.class, managementService, null);
        bundleContext.registerService(HttpAPIService.class, managementService, null);

        // register the statistics service
        StatisticsService statisticsService = new XOWLStatisticsService();
        bundleContext.registerService(StatisticsService.class, statisticsService, null);
        bundleContext.registerService(HttpAPIService.class, statisticsService, null);

        // register the job executor service
        serviceJobExecutor = new XOWLJobExecutor();
        bundleContext.registerService(JobExecutionService.class, serviceJobExecutor, null);
        bundleContext.registerService(HttpAPIService.class, serviceJobExecutor, null);
        statisticsService.registerProvider(serviceJobExecutor);

        // register the event service
        eventService = new XOWLEventService();
        bundleContext.registerService(EventService.class, eventService, null);
        statisticsService.registerProvider(eventService);

        // register the directory service
        XOWLBusinessDirectoryService directoryService = new XOWLBusinessDirectoryService();
        directoryService.register(KernelSchema.IMPL);
        directoryService.register(SchemaArtifactArchetype.INSTANCE);
        directoryService.register(FreeArtifactArchetype.INSTANCE);
        directoryService.register(SchemaDomain.INSTANCE);
        bundleContext.registerService(BusinessDirectoryService.class, directoryService, null);
        bundleContext.registerService(HttpAPIService.class, directoryService, null);

        eventService.onEvent(PlatformStartupEvent.INSTANCE);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (eventService != null)
            eventService.close();
        if (serviceJobExecutor != null)
            serviceJobExecutor.close();
    }
}
