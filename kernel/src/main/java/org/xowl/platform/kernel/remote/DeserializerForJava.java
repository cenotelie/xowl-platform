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

package org.xowl.platform.kernel.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

/**
 * Implements a deserializer that relies on the java service discovery to discover the factories
 *
 * @author Laurent Wouters
 */
public class DeserializerForJava extends Deserializer {
    /**
     * The loader for the factories
     */
    private final ServiceLoader<DeserializerFactory> serviceLoader = ServiceLoader.load(DeserializerFactory.class);

    @Override
    protected Collection<DeserializerFactory> getFactories() {
        Collection<DeserializerFactory> result = new ArrayList<>();
        for (DeserializerFactory factory : serviceLoader)
            result.add(factory);
        return result;
    }
}
