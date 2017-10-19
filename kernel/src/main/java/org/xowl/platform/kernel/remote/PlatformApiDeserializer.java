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

import fr.cenotelie.commons.utils.api.ApiDeserializer;
import fr.cenotelie.commons.utils.api.ApiFactory;
import fr.cenotelie.hime.redist.ASTNode;

import java.util.Collection;

/**
 * Represents a factory of platform objects
 *
 * @author Laurent Wouters
 */
public abstract class PlatformApiDeserializer extends ApiDeserializer {
    /**
     * The inner factory for this serializer
     */
    private static class Factory implements ApiFactory {
        /**
         * The parent deserializer
         */
        private PlatformApiDeserializer deserializer;

        @Override
        public Object newObject(String type, ASTNode definition) {
            for (PlatformApiFactory factory : deserializer.getParts()) {
                Object result = factory.newObject(deserializer, type, definition);
                if (result != null)
                    return result;
            }
            return null;
        }
    }

    /**
     * Initializes this deserializer
     */
    public PlatformApiDeserializer() {
        super(new Factory());
        ((Factory) getFactory()).deserializer = this;
    }

    /**
     * Gets the factory parts
     *
     * @return The factory parts
     */
    protected abstract Collection<PlatformApiFactory> getParts();
}
