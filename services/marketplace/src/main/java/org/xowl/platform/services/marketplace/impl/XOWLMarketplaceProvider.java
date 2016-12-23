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

package org.xowl.platform.services.marketplace.impl;

import org.xowl.infra.utils.config.Section;
import org.xowl.platform.services.marketplace.Marketplace;
import org.xowl.platform.services.marketplace.MarketplaceProvider;

/**
 * Implements a marketplace provider for the standard platform
 *
 * @author Laurent Wouters
 */
public class XOWLMarketplaceProvider implements MarketplaceProvider {
    @Override
    public String getIdentifier() {
        return XOWLMarketplaceProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Marketplace Provider";
    }

    @Override
    public boolean supports(String type) {
        if (FSMarketplace.class.getCanonicalName().equals(type))
            return true;
        return false;
    }

    @Override
    public Marketplace newMarketplace(String type, Section configuration) {
        if (FSMarketplace.class.getCanonicalName().equals(type))
            return new FSMarketplace(configuration);
        return null;
    }
}
