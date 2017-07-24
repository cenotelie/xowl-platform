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
import java.util.Collections;

/**
 * Implements a factory that requires the manual registering of parts
 *
 * @author Laurent Wouters
 */
public class PlatformApiDeserializerAggregate extends PlatformApiDeserializer {
    /**
     * The registered parts
     */
    private final Collection<PlatformApiFactory> parts = new ArrayList<>();

    /**
     * Registers a factory
     *
     * @param factory The factory to register
     */
    public void register(PlatformApiFactory factory) {
        this.parts.add(factory);
    }

    @Override
    protected Collection<PlatformApiFactory> getParts() {
        return Collections.unmodifiableCollection(parts);
    }
}
