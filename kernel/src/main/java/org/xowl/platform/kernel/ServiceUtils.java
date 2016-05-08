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
import org.xowl.infra.utils.logging.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility APIs for services
 *
 * @author Laurent Wouters
 */
public class ServiceUtils {
    /**
     * Gets the first service for the specified service type
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S extends Service> S getService(Class<S> serviceType) {
        BundleContext context = FrameworkUtil.getBundle(serviceType).getBundleContext();
        ServiceReference reference = context.getServiceReference(serviceType);
        if (reference == null)
            return null;
        S result = (S) context.getService(reference);
        context.ungetService(reference);
        return result;
    }

    /**
     * Gets the first service for the specified service type
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S extends Service> Collection<S> getServices(Class<S> serviceType) {
        Collection<S> result = new ArrayList<>();
        BundleContext context = FrameworkUtil.getBundle(serviceType).getBundleContext();
        try {
            Collection references = context.getServiceReferences(serviceType, null);
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
     * Gets the service for the specified service type with a a specific parameter
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param paramName   The name of the parameter to match
     * @param paramValue  The value of the parameter to match
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S extends Service> S getService(Class<S> serviceType, String paramName, String paramValue) {
        S result = null;
        BundleContext context = FrameworkUtil.getBundle(serviceType).getBundleContext();
        try {
            Collection references = context.getServiceReferences(serviceType, "(" + paramName + "=" + paramValue + ")");
            for (Object obj : references) {
                ServiceReference reference = (ServiceReference) obj;
                if (reference == null)
                    continue;
                if (result == null)
                    result = (S) context.getService(reference);
                context.ungetService(reference);
            }
        } catch (InvalidSyntaxException exception) {
            Logger.DEFAULT.error(exception);
        }
        return result;
    }
}
