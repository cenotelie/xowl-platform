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
     * @param <S>           The type of component
     * @return The component, or null if there is none
     */
    public static <S extends Registrable> S getComponent(Class<S> componentType) {
        BundleContext context = FrameworkUtil.getBundle(componentType).getBundleContext();
        ServiceReference reference = context.getServiceReference(componentType);
        if (reference == null)
            return null;
        S result = (S) context.getService(reference);
        context.ungetService(reference);
        return result;
    }

    /**
     * Gets the all the components for the specified type of components
     *
     * @param componentType A type of components as the Java class that must be implemented
     * @param <S>           The type of component
     * @return The components
     */
    public static <S extends Registrable> Collection<S> getComponents(Class<S> componentType) {
        Collection<S> result = new ArrayList<>();
        BundleContext context = FrameworkUtil.getBundle(componentType).getBundleContext();
        try {
            Collection references = context.getServiceReferences(componentType, null);
            for (Object obj : references) {
                ServiceReference reference = (ServiceReference) obj;
                if (reference == null)
                    continue;
                result.add((S) context.getService(reference));
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
     * @param <S>           The type of component
     * @return The component, or null if there is none
     */
    public static <S extends Registrable> S getComponent(Class<S> componentType, String paramName, String paramValue) {
        S result = null;
        BundleContext context = FrameworkUtil.getBundle(componentType).getBundleContext();
        try {
            Collection references = context.getServiceReferences(componentType, "(" + paramName + "=" + paramValue + ")");
            for (Object obj : references) {
                ServiceReference reference = (ServiceReference) obj;
                if (reference == null)
                    continue;
                if (result == null)
                    result = (S) context.getService(reference);
                context.ungetService(reference);
            }
        } catch (InvalidSyntaxException exception) {
            Logging.getDefault().error(exception);
        }
        return result;
    }
}
