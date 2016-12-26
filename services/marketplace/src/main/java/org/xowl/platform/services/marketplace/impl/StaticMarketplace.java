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

import org.xowl.platform.services.marketplace.Marketplace;

import java.util.Collection;

/**
 * Basic implementation of a static marketplace
 */
abstract class StaticMarketplace implements Marketplace {
    /**
     * The descriptor file for a file-system marketplace
     */
    protected static final String MARKETPLACE_DESCRIPTOR = "marketplace.json";

    /**
     * Gets whether the specified content string contains the terms to look for
     *
     * @param content A string
     * @param terms   The list of terms to look for in the content
     * @return Whether the string contains all the terms
     */
    protected boolean stringMatches(String content, Collection<String> terms) {
        for (String term : terms) {
            if (!content.contains(term))
                return false;
        }
        return true;
    }
}
