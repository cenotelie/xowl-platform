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

package org.xowl.platform.kernel;

import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.kernel.platform.PlatformShutdownEvent;
import org.xowl.platform.kernel.platform.PlatformStartupEvent;
import org.xowl.platform.kernel.platform.PlatformUserRoot;
import org.xowl.platform.kernel.security.SecurityService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the management of the platform's lifecycle
 *
 * @author Laurent Wouters
 */
public class PlatformLifecycle implements FrameworkListener {
    /**
     * The singleton instance
     */
    private static final PlatformLifecycle INSTANCE = new PlatformLifecycle();

    /**
     * Gets the instance of this structure
     *
     * @return The singleton instance
     */
    public static PlatformLifecycle getInstance() {
        return INSTANCE;
    }

    /**
     * Whether the startup sequence has been executed
     */
    private final AtomicBoolean hasStarted;
    /**
     * Whether the shutdown sequence has been executed
     */
    private final AtomicBoolean hasShutdown;
    /**
     * The register listeners that have been activated so far
     */
    private final Collection<RegisterListener> activatedListeners;

    /**
     * Initializes this structure
     */
    private PlatformLifecycle() {
        this.hasStarted = new AtomicBoolean(false);
        this.hasShutdown = new AtomicBoolean(false);
        this.activatedListeners = new ArrayList<>();
    }

    @Override
    public void frameworkEvent(FrameworkEvent frameworkEvent) {
        if (frameworkEvent.getType() == FrameworkEvent.STARTED) {
            onPlatformStartup();
        }
    }

    /**
     * When a component have been waited for and is now available
     *
     * @param listener The corresponding listener
     */
    void onComponentAvailable(RegisterListener listener) {
        activatedListeners.add(listener);
    }

    /**
     * Executes the startup sequence of the platform
     */
    public void onPlatformStartup() {
        if (!hasStarted.compareAndSet(false, true))
            return;

        // finishes delayed activations
        while (!activatedListeners.isEmpty()) {
            Collection<RegisterListener> buffer = new ArrayList<>(activatedListeners);
            activatedListeners.clear();
            for (RegisterListener listener : buffer)
                listener.broadcast();
        }

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
