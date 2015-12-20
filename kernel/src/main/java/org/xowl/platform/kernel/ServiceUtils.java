/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.kernel;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Utility APIs for services
 *
 * @author Laurent Wouters
 */
public class ServiceUtils {
    /**
     * Gets the first service for the specified service type (using Java service loader)
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S extends Service> S getJavaService(Class<S> serviceType) {
        Iterator<S> iterator = ServiceLoader.load(serviceType).iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Gets all the services for the specified service type (using Java service loader)
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param <S>         The type of service
     * @return The services
     */
    public static <S extends Service> Collection<S> getJavaServices(Class<S> serviceType) {
        Collection<S> result = new ArrayList<>();
        for (S service : ServiceLoader.load(serviceType))
            result.add(service);
        return result;
    }

    /**
     * Gets the service for the specified service type with a specific identifier (using Java service loader)
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param id          The identifier of the service to retrieve
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S extends Service> S getJavaService(Class<S> serviceType, String id) {
        if (id == null)
            return getJavaService(serviceType);
        for (S service : ServiceLoader.load(serviceType)) {
            if (id.equals(service.getIdentifier()))
                return service;
        }
        return null;
    }

    /**
     * Gets the first service for the specified service type (using OSGI framework)
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S extends Service> S getOSGIService(Class<S> serviceType) {
        BundleContext context = FrameworkUtil.getBundle(serviceType).getBundleContext();
        ServiceReference reference = context.getServiceReference(serviceType);
        if (reference == null)
            return null;
        S result = (S) context.getService(reference);
        context.ungetService(reference);
        return result;
    }

    /**
     * Gets the first service for the specified service type (using OSGI framework)
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S extends Service> Collection<S> getOSGIServices(Class<S> serviceType) {
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
     * Gets the service for the specified service type with a specific identifier (using OSGI framework)
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param id          The identifier of the service to retrieve
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S extends Service> S getOSGIService(Class<S> serviceType, String id) {
        BundleContext context = FrameworkUtil.getBundle(serviceType).getBundleContext();
        try {
            Collection references = context.getServiceReferences(serviceType, null);
            for (Object obj : references) {
                ServiceReference reference = (ServiceReference) obj;
                if (reference == null)
                    continue;
                S service = (S) context.getService(reference);
                if (id.equals(service.getIdentifier()))
                    return service;
            }
            return null;
        } catch (InvalidSyntaxException exception) {
            // cannot happen
            return null;
        }
    }
}
