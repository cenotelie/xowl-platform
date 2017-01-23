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
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.artifacts.*;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.impl.*;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.jobs.JobFactory;
import org.xowl.platform.kernel.platform.PlatformJobFactory;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.PlatformShutdownEvent;
import org.xowl.platform.kernel.security.Realm;
import org.xowl.platform.kernel.security.SecurityPolicy;
import org.xowl.platform.kernel.security.SecurityService;
import org.xowl.platform.kernel.statistics.MeasurableService;
import org.xowl.platform.kernel.statistics.StatisticsService;
import org.xowl.platform.kernel.webapi.HttpApiDiscoveryService;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    /**
     * The job executor service
     */
    private KernelJobExecutor serviceJobExecutor;
    /**
     * The event service
     */
    private KernelEventService eventService;
    /**
     * The platform management service
     */
    private KernelPlatformManagementService platformService;
    /**
     * The HTTP API discovery service
     */
    private KernelHttpApiDiscoveryService discoveryService;
    /**
     * The tracker of the HTTP API services
     */
    private ServiceTracker discoveryServiceTracker;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        // register security components
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put(Realm.PROPERTY_ID, XOWLSecurityNosecRealm.class.getCanonicalName());
        bundleContext.registerService(Realm.class, new XOWLSecurityNosecRealm(), dictionary);
        dictionary = new Hashtable<>();
        dictionary.put(SecurityPolicy.PROPERTY_ID, XOWLSecurityPolicyAuthenticated.class.getCanonicalName());
        bundleContext.registerService(SecurityPolicy.class, new XOWLSecurityPolicyAuthenticated(), dictionary);
        dictionary = new Hashtable<>();
        dictionary.put(SecurityPolicy.PROPERTY_ID, XOWLSecurityPolicyCustom.class.getCanonicalName());
        bundleContext.registerService(SecurityPolicy.class, new XOWLSecurityPolicyCustom(), dictionary);

        // register the logging service
        KernelLoggingService loggingService = new KernelLoggingService();
        Logging.setDefault(loggingService);
        bundleContext.registerService(Service.class, loggingService, null);
        bundleContext.registerService(LoggingService.class, loggingService, null);
        bundleContext.registerService(HttpApiService.class, loggingService, null);
        bundleContext.registerService(MeasurableService.class, loggingService, null);

        // register the configuration service
        ConfigurationService configurationService = new FileSystemConfigurationService();
        bundleContext.registerService(Service.class, configurationService, null);
        bundleContext.registerService(ConfigurationService.class, configurationService, null);

        // register the security service
        XOWLSecurityService securityService = new XOWLSecurityService(configurationService);
        bundleContext.registerService(Service.class, securityService, null);
        bundleContext.registerService(SecurityService.class, securityService, null);
        bundleContext.registerService(HttpApiService.class, securityService, null);

        // register the statistics service
        KernelStatisticsService statisticsService = new KernelStatisticsService();
        bundleContext.registerService(Service.class, statisticsService, null);
        bundleContext.registerService(StatisticsService.class, statisticsService, null);
        bundleContext.registerService(HttpApiService.class, statisticsService, null);

        // register the event service
        eventService = new KernelEventService();
        bundleContext.registerService(Service.class, eventService, null);
        bundleContext.registerService(EventService.class, eventService, null);
        bundleContext.registerService(MeasurableService.class, eventService, null);

        // register the job executor service
        serviceJobExecutor = new KernelJobExecutor(configurationService, eventService);
        bundleContext.registerService(Service.class, serviceJobExecutor, null);
        bundleContext.registerService(JobExecutionService.class, serviceJobExecutor, null);
        bundleContext.registerService(HttpApiService.class, serviceJobExecutor, null);
        bundleContext.registerService(MeasurableService.class, serviceJobExecutor, null);
        bundleContext.registerService(JobFactory.class, new PlatformJobFactory(), null);

        // register the directory service
        KernelBusinessDirectoryService directoryService = new KernelBusinessDirectoryService();
        bundleContext.registerService(BusinessSchema.class, KernelSchema.IMPL, null);
        bundleContext.registerService(BusinessDomain.class, SchemaDomain.INSTANCE, null);
        bundleContext.registerService(ArtifactArchetype.class, SchemaArtifactArchetype.INSTANCE, null);
        bundleContext.registerService(ArtifactArchetype.class, FreeArtifactArchetype.INSTANCE, null);
        bundleContext.registerService(Service.class, directoryService, null);
        bundleContext.registerService(BusinessDirectoryService.class, directoryService, null);
        bundleContext.registerService(HttpApiService.class, directoryService, null);

        // register the platform management service
        platformService = new KernelPlatformManagementService(configurationService, serviceJobExecutor);
        bundleContext.registerService(Service.class, platformService, null);
        bundleContext.registerService(PlatformManagementService.class, platformService, null);
        bundleContext.registerService(HttpApiService.class, platformService, null);
        bundleContext.registerService(MeasurableService.class, platformService, null);
        bundleContext.addFrameworkListener(platformService);

        // register the HTTP API discovery service
        discoveryService = new KernelHttpApiDiscoveryService();
        bundleContext.registerService(Service.class, discoveryService, null);
        bundleContext.registerService(HttpApiDiscoveryService.class, discoveryService, null);
        bundleContext.registerService(HttpApiService.class, discoveryService, null);
        discoveryServiceTracker = new ServiceTracker<HttpApiService, HttpApiService>(bundleContext, HttpApiService.class, null) {
            public void removedService(ServiceReference reference, HttpApiService apiService) {
                discoveryService.unregisterService(apiService);
            }

            public HttpApiService addingService(ServiceReference reference) {
                HttpApiService apiService = (HttpApiService) bundleContext.getService(reference);
                discoveryService.registerService(apiService);
                return apiService;
            }
        };
        discoveryServiceTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (eventService != null && platformService != null)
            eventService.onEvent(new PlatformShutdownEvent(platformService));
        if (eventService != null)
            eventService.close();
        if (serviceJobExecutor != null)
            serviceJobExecutor.close();
        discoveryServiceTracker.close();
    }
}
