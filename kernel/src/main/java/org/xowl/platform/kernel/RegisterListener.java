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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Implements an OSGi service tracker that wait for the availability of a component
 *
 * @param <T> The type of registrable
 * @author Laurent Wouters
 */
class RegisterListener<T> implements ServiceListener {
    /**
     * The bundle context for the listener
     */
    private final BundleContext context;
    /**
     * The type of component to wait for
     */
    private final Class<T> type;
    /**
     * The listener
     */
    private final RegisterWaiter<T> listener;
    /**
     * The component instance, when available
     */
    private T component;

    /**
     * Initializes this listener
     *
     * @param context  The bundle context for the listener
     * @param type     The type of component to wait for
     * @param listener The listener
     */
    public RegisterListener(BundleContext context, Class<T> type, RegisterWaiter<T> listener) {
        this.context = context;
        this.type = type;
        this.listener = listener;
    }

    @Override
    public void serviceChanged(ServiceEvent serviceEvent) {
        if (serviceEvent.getType() == ServiceEvent.REGISTERED) {
            ServiceReference reference = serviceEvent.getServiceReference();
            if (reference != null) {
                Object component = context.getService(reference);
                if (component != null && type.isInstance(component)) {
                    this.component = (T) component;
                    PlatformLifecycle.getInstance().onComponentAvailable(this);
                    context.removeServiceListener(this);
                }
            }
        }
    }

    /**
     * Broadcast to the listener
     */
    public void broadcast() {
        listener.onAvailable(context, component);
    }
}