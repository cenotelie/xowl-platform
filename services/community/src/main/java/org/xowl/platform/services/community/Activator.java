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

package org.xowl.platform.services.community;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.ClosableService;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.RegisterWaiter;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.remote.DeserializerFactory;
import org.xowl.platform.kernel.security.SecuredActionPolicyProvider;
import org.xowl.platform.kernel.security.SecuredService;
import org.xowl.platform.kernel.webapi.HttpApiService;
import org.xowl.platform.services.community.bots.BotFactory;
import org.xowl.platform.services.community.bots.BotManagementService;
import org.xowl.platform.services.community.impl.*;
import org.xowl.platform.services.community.profiles.BadgeProvider;
import org.xowl.platform.services.community.profiles.ProfileService;
import org.xowl.platform.services.community.profiles.ProfileServiceProvider;

/**
 * Activator for this bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        bundleContext.registerService(DeserializerFactory.class, new DeserializerFactoryForCommunity(), null);
        bundleContext.registerService(SecuredActionPolicyProvider.class, new XOWLCommunitySecurityProvider(), null);
        bundleContext.registerService(ProfileServiceProvider.class, new XOWLProfileServiceProvider(), null);
        bundleContext.registerService(BadgeProvider.class, new XOWLDefaultBadges(), null);
        bundleContext.registerService(BotFactory.class, new XOWLTelemetryBotFactory(), null);

        Register.waitFor(EventService.class, new RegisterWaiter<EventService>() {
            @Override
            public void onAvailable(BundleContext bundleContext, EventService component) {
                // register the bots management service
                XOWLBotManagementService serviceBots = new XOWLBotManagementService(component);
                bundleContext.registerService(Service.class, serviceBots, null);
                bundleContext.registerService(SecuredService.class, serviceBots, null);
                bundleContext.registerService(ClosableService.class, serviceBots, null);
                bundleContext.registerService(HttpApiService.class, serviceBots, null);
                bundleContext.registerService(BotManagementService.class, serviceBots, null);

                // register the profile service
                XOWLProfileService profileService = new XOWLProfileService();
                bundleContext.registerService(Service.class, profileService, null);
                bundleContext.registerService(SecuredService.class, profileService, null);
                bundleContext.registerService(HttpApiService.class, profileService, null);
                bundleContext.registerService(ProfileService.class, profileService, null);
            }
        }, bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }
}