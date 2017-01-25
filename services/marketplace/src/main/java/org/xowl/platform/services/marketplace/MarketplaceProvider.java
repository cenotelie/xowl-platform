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

import org.xowl.infra.utils.config.Section;
import org.xowl.platform.kernel.Registrable;

/**
 * A provider of marketplace implementations
 *
 * @author Laurent Wouters
 */
public interface MarketplaceProvider extends Registrable {
    /**
     * Gets whether this provider supports marketplace of the specified type
     *
     * @param type A type of marketplace
     * @return Whether this provider supports the specified type
     */
    boolean supports(String type);

    /**
     * Creates a new marketplace for the specified type
     *
     * @param type          A type of marketplace
     * @param configuration The configuration section for the marketplace
     * @return The created marketplace
     */
    Marketplace newMarketplace(String type, Section configuration);
}
