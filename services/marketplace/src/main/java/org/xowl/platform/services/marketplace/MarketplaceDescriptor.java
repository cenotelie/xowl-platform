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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements the descriptor of a static marketplace as produced by the Maven plugin:
 * org.xowl.toolkit.xowl-marketplace-builder-maven-plugin
 *
 * @author Laurent Wouters
 */
public class MarketplaceDescriptor {
    /**
     * Represents an addon in the marketplace descriptor
     */
    public static class Addon {
        /**
         * The identifier of this addon
         */
        public final String identifier;
        /**
         * The categories for this addon
         */
        public final String[] categories;

        /**
         * Initializes this structure
         *
         * @param root The root for the description
         */
        public Addon(ASTNode root) {
            String identifier = "";
            String[] categories = null;
            for (ASTNode member : root.getChildren()) {
                String head = TextUtils.unescape(member.getChildren().get(0).getValue());
                head = head.substring(1, head.length() - 1);
                if ("identifier".equals(head)) {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    identifier = value.substring(1, value.length() - 1);
                } else if ("categories".equals(head)) {
                    categories = new String[member.getChildren().get(1).getChildren().size()];
                    int i = 0;
                    for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                        String value = TextUtils.unescape(member2.getValue());
                        categories[i++] = value.substring(1, value.length() - 1);
                    }
                }
            }
            this.identifier = identifier;
            this.categories = categories == null ? new String[0] : categories;
        }
    }

    /**
     * The categories in the marketplace
     */
    private final Collection<Category> categories;
    /**
     * All the addons in the marketplace
     */
    private final Collection<Addon> addons;

    /**
     * Gets the categories
     *
     * @return The categories
     */
    public Collection<Category> getCategories() {
        return Collections.unmodifiableCollection(categories);
    }

    /**
     * Gets all addons
     *
     * @return All the addons
     */
    public Collection<Addon> getAddons() {
        return Collections.unmodifiableCollection(addons);
    }

    /**
     * Initializes an empty descriptor
     */
    public MarketplaceDescriptor() {
        this.categories = new ArrayList<>();
        this.addons = new ArrayList<>();
    }

    /**
     * Initializes this descriptor
     *
     * @param root The root node of the descriptor
     */
    public MarketplaceDescriptor(ASTNode root) {
        this.categories = new ArrayList<>();
        this.addons = new ArrayList<>();
        for (ASTNode member : root.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("categories".equals(head)) {
                for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                    categories.add(new Category(member2));
                }
            } else if ("addons".equals(head)) {
                for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                    addons.add(new Addon(member2));
                }
            }
        }
    }
}
