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

package org.xowl.platform.kernel.artifacts;

import org.xowl.infra.store.ProxyObject;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;

import java.util.Collection;

/**
 * Represents the schema of a domain, or artifact
 *
 * @author Laurent Wouters
 */
public interface BusinessSchema extends Identifiable, Serializable {
    /**
     * Gets the classes in this schema
     *
     * @return The classes in this schema
     */
    Collection<ProxyObject> getClasses();

    /**
     * Gets the datatypes in this schema
     *
     * @return The datatypes in this schema
     */
    Collection<ProxyObject> getDatatypes();

    /**
     * Gets the object properties in this schema
     *
     * @return The object properties in this schema
     */
    Collection<ProxyObject> getObjectProperties();

    /**
     * Gets the data properties in this schema
     *
     * @return The data properties in this schema
     */
    Collection<ProxyObject> getDataProperties();

    /**
     * Gets the individuals in this schema
     *
     * @return The individuals in this schema
     */
    Collection<ProxyObject> getIndividuals();

    /**
     * Gets the entity for the specified URI
     *
     * @param uri The URI of an entity
     * @return The associated entity
     */
    ProxyObject getEntity(String uri);
}
