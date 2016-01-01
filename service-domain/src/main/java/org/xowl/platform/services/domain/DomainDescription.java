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

package org.xowl.platform.services.domain;

import org.xowl.platform.kernel.Identifiable;
import org.xowl.store.Serializable;

import java.util.Collection;

/**
 * Represents the description of a domain from which a domain connector can be instantiated
 *
 * @author Laurent Wouters
 */
public interface DomainDescription extends Identifiable, Serializable {

    /**
     * Gets the description of this domain
     *
     * @return The description
     */
    String getDescription();

    /**
     * Gets the parameters for the instantiation of a connector for this domain
     *
     * @return The parameters
     */
    Collection<DomainDescriptionParam> getParameters();
}
