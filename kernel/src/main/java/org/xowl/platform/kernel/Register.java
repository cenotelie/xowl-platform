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

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.xowl.infra.utils.logging.Logging;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents the access to the register of components
 *
 * @author Laurent Wouters
 */
public class Register {
    /**
     * Gets the first component for the specified type of components
     *
     * @param componentType A type of components as the Java class that must be implemented
     * @param <T>           The type of component
     * @return The component, or null if there is none
     */
    public static <T> T getComponent(Class<T> componentType) {
        BundleContext context = FrameworkUtil.getBundle(componentType).getBundleContext();
        ServiceReference reference = context.getServiceReference(componentType);
        if (reference == null)
            return null;
        T result = (T) context.getService(reference);
        context.ungetService(reference);
        return result;
    }

    /**
     * Gets the all the components for the specified type of components
     *
     * @param componentType A type of components as the Java class that must be implemented
     * @param <T>           The type of component
     * @return The components
     */
    public static <T> Collection<T> getComponents(Class<T> componentType) {
        Collection<T> result = new ArrayList<>();
        BundleContext context = FrameworkUtil.getBundle(componentType).getBundleContext();
        try {
            Collection references = context.getServiceReferences(componentType, null);
            for (Object obj : references) {
                ServiceReference reference = (ServiceReference) obj;
                if (reference == null)
                    continue;
                result.add((T) context.getService(reference));
                context.ungetService(reference);
            }
            return result;
        } catch (InvalidSyntaxException exception) {
            // cannot happen
            return result;
        }
    }

    /**
     * Gets the first component for the specified type of components that matches the specified parameter
     *
     * @param componentType A type of components as the Java class that must be implemented
     * @param paramName     The name of the parameter to match
     * @param paramValue    The value of the parameter to match
     * @param <T>           The type of component
     * @return The component, or null if there is none
     */
    public static <T> T getComponent(Class<T> componentType, String paramName, String paramValue) {
        T result = null;
        BundleContext context = FrameworkUtil.getBundle(componentType).getBundleContext();
        try {
            Collection references = context.getServiceReferences(componentType, "(" + paramName + "=" + paramValue + ")");
            for (Object obj : references) {
                ServiceReference reference = (ServiceReference) obj;
                if (reference == null)
                    continue;
                if (result == null)
                    result = (T) context.getService(reference);
                context.ungetService(reference);
            }
        } catch (InvalidSyntaxException exception) {
            Logging.getDefault().error(exception);
        }
        return result;
    }

    /**
     * Waits for a component to become available
     *
     * @param componentType The type of component to wait for
     * @param waiter        The listener
     * @param context       The bundle context for the listener
     * @param <T>           The type of component
     */
    public static <T> void waitFor(Class<T> componentType, RegisterWaiter<T> waiter, BundleContext context) {
        ServiceReference reference = context.getServiceReference(componentType);
        if (reference != null) {
            T component = (T) context.getService(reference);
            context.ungetService(reference);
            waiter.onAvailable(context, component);
        } else {
            context.addServiceListener(new RegisterListener<T>(context, componentType, waiter));
        }
    }
}
