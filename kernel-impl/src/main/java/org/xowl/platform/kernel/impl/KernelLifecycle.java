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

package org.xowl.platform.kernel.impl;

import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.xowl.platform.kernel.ManagedService;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.PlatformShutdownEvent;
import org.xowl.platform.kernel.platform.PlatformStartupEvent;
import org.xowl.platform.kernel.platform.PlatformUserRoot;
import org.xowl.platform.kernel.security.SecurityService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the management of the platform's lifecycle
 *
 * @author Laurent Wouters
 */
class KernelLifecycle implements FrameworkListener {
    /**
     * Whether the startup sequence has been executed
     */
    private final AtomicBoolean hasStarted;
    /**
     * Whether the shutdown sequence has been executed
     */
    private final AtomicBoolean hasShutdown;

    /**
     * Initializes this structure
     */
    public KernelLifecycle() {
        this.hasStarted = new AtomicBoolean(false);
        this.hasShutdown = new AtomicBoolean(false);
    }

    @Override
    public void frameworkEvent(FrameworkEvent frameworkEvent) {
        if (frameworkEvent.getType() == FrameworkEvent.STARTED) {
            onPlatformStartup();
        }
    }

    /**
     * Executes the startup sequence of the platform
     */
    public void onPlatformStartup() {
        if (!hasStarted.compareAndSet(false, true))
            return;

        // authenticate as root
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService != null)
            securityService.authenticate(PlatformUserRoot.INSTANCE);

        // execute sequence
        List<ManagedService> services = (List<ManagedService>) Register.getComponents(ManagedService.class);
        Collections.sort(services, ManagedService.COMPARATOR_STARTUP);
        for (ManagedService service : services) {
            service.onLifecycleStart();
        }

        // broadcast the startup event
        EventService eventService = Register.getComponent(EventService.class);
        PlatformManagementService platformManagementService = Register.getComponent(PlatformManagementService.class);
        if (eventService != null)
            eventService.onEvent(new PlatformStartupEvent(platformManagementService));
    }

    /**
     * Executes the shutdown sequence of the platform
     */
    public void onPlatformShutdown() {
        if (!hasShutdown.compareAndSet(false, true))
            return;

        // broadcast the shutdown event
        EventService eventService = Register.getComponent(EventService.class);
        PlatformManagementService platformManagementService = Register.getComponent(PlatformManagementService.class);
        if (eventService != null)
            eventService.onEvent(new PlatformShutdownEvent(platformManagementService));

        // authenticate as root
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService != null)
            securityService.authenticate(PlatformUserRoot.INSTANCE);

        // execute sequence
        List<ManagedService> services = (List<ManagedService>) Register.getComponents(ManagedService.class);
        Collections.sort(services, ManagedService.COMPARATOR_SHUTDOWN);
        for (ManagedService service : services) {
            service.onLifecycleStop();
        }
    }
}