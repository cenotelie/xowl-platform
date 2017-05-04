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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements the descriptor of a static marketplace as produced by the Maven plugin:
 * org.xowl.toolkit.xowl-packaging-maven-plugin
 *
 * @author Laurent Wouters
 */
public class MarketplaceDescriptor {
    /**
     * All the addons in the marketplace
     */
    private final Collection<String> addons;

    /**
     * Gets all addons
     *
     * @return All the addons
     */
    public Collection<String> getAddons() {
        return Collections.unmodifiableCollection(addons);
    }

    /**
     * Initializes an empty descriptor
     */
    public MarketplaceDescriptor() {
        this.addons = new ArrayList<>();
    }

    /**
     * Initializes this descriptor
     *
     * @param root The root node of the descriptor
     */
    public MarketplaceDescriptor(ASTNode root) {
        this.addons = new ArrayList<>();
        for (ASTNode member : root.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("addons".equals(head)) {
                for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                    String value = TextUtils.unescape(member2.getValue());
                    addons.add(value.substring(1, value.length() - 1));
                }
            }
        }
    }
}
