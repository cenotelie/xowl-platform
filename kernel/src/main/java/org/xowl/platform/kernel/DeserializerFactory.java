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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.api.XOWLFactory;

/**
 * Represents a factory for serialized platform objects
 *
 * @author Laurent Wouters
 */
public interface DeserializerFactory extends Registrable, XOWLFactory {
    /**
     * Creates a new object
     *
     * @param deserializer The parent deserializer
     * @param type         The object's type
     * @param definition   The definition
     * @return The new object
     */
    Object newObject(Deserializer deserializer, String type, ASTNode definition);
}
