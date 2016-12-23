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

import java.util.*;

/**
 * Implements the descriptor of a static marketplace
 *
 * @author Laurent Wouters
 */
public class MarketplaceDescriptor {
    /**
     * Represents a piece of content on the marketplace
     */
    public static class Content {
        /**
         * The identifier of this content
         */
        public final String identifier;
        /**
         * The file (relative to this descriptor) that contains the descriptor of this content
         */
        public final String fileDescriptor;
        /**
         * The file (relative to this descriptor) that contains the content
         */
        public final String fileContent;

        /**
         * Initializes this structure
         *
         * @param root The root for the description
         */
        public Content(ASTNode root) {
            String identifier = "";
            String fileDescriptor = "";
            String fileContent = "";
            for (ASTNode member : root.getChildren()) {
                String head = TextUtils.unescape(member.getChildren().get(0).getValue());
                head = head.substring(1, head.length() - 1);
                if ("identifier".equals(head)) {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    identifier = value.substring(1, value.length() - 1);
                } else if ("fileDescriptor".equals(head)) {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    fileDescriptor = value.substring(1, value.length() - 1);
                } else if ("fileContent".equals(head)) {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    fileContent = value.substring(1, value.length() - 1);
                }
            }
            this.identifier = identifier;
            this.fileDescriptor = fileDescriptor;
            this.fileContent = fileContent;
        }
    }

    /**
     * The categories in the marketplace
     */
    private final Collection<Category> categories;
    /**
     * All the addons in the marketplace
     */
    private final Collection<Content> addons;
    /**
     * The identifiers of the addons in each category
     */
    private final Map<String, Collection<Content>> addonsByCategory;

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
    public Collection<Content> getAddons() {
        return Collections.unmodifiableCollection(addons);
    }

    /**
     * Gets the addons in a category
     *
     * @param category The identifier of a category
     * @return The associated addons
     */
    public Collection<Content> getAddonsInCategory(String category) {
        Collection<Content> result = addonsByCategory.get(category);
        if (result == null)
            return Collections.emptyList();
        return Collections.unmodifiableCollection(result);
    }

    /**
     * Initializes an empty descriptor
     */
    public MarketplaceDescriptor() {
        this.categories = new ArrayList<>();
        this.addons = new ArrayList<>();
        this.addonsByCategory = new HashMap<>();
    }

    /**
     * Initializes this descriptor
     *
     * @param root The root node of the descriptor
     */
    public MarketplaceDescriptor(ASTNode root) {
        this.categories = new ArrayList<>();
        this.addons = new ArrayList<>();
        this.addonsByCategory = new HashMap<>();
        for (ASTNode member : root.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("categories".equals(head)) {
                for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                    categories.add(new Category(member2));
                }
            } else if ("addons".equals(head)) {
                for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                    addons.add(new Content(member2));
                }
            }
        }
    }
}
