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

package org.xowl.platform.services.connection;

import org.xowl.infra.store.Serializable;
import org.xowl.platform.kernel.Identifiable;

/**
 * Represents a parameter for this domain
 *
 * @author Laurent Wouters
 */
public interface ConnectorDescriptionParam extends Identifiable, Serializable {
    /**
     * Type hint for a string parameter
     */
    String TYPE_HINT_STRING = "string";
    /**
     * Type hint for a password parameter
     */
    String TYPE_HINT_PASSWORD = "password";
    /**
     * Type hint for a number parameter
     */
    String TYPE_HINT_NUMBER = "number";
    /**
     * Type hint for an uri parameter
     */
    String TYPE_HINT_URI = "uri";

    /**
     * Gets whether the parameter is required
     *
     * @return Whether the parameter is required
     */
    boolean isRequired();

    /**
     * Gets the type hint for this parameter
     *
     * @return The type hint
     */
    String typeHint();
}
