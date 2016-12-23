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

package org.xowl.platform.services.marketplace;

import org.xowl.platform.kernel.platform.Addon;

import java.util.Collection;

/**
 * Common interface for a marketplace
 *
 * @author Laurent Wouters
 */
public interface Marketplace {
    /**
     * Gets the available categories on this marketplace
     *
     * @return The available categories
     */
    Collection<Category> getCategories();

    /**
     * Lookups available addons on the marketplace
     *
     * @param identifier The identifier to look for, may be null
     * @param name       The name to look for, may be null
     * @param categoryId The category to look for, may be null
     * @return The collection of matching addons
     */
    Collection<Addon> lookupAddons(String identifier, String name, String categoryId);
}
