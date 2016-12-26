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

package org.xowl.platform.kernel.platform;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.product.LicensePlain;
import org.xowl.infra.utils.product.Product;
import org.xowl.infra.utils.product.VersionInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Describes an addon for the platform
 *
 * @author Laurent Wouters
 */
public class Addon extends Product {
    /**
     * The description of the pricing policy for the addon
     */
    private final String pricing;
    /**
     * The collection of the bundles that compose this addon
     */
    private final Collection<AddonBundle> bundles;
    /**
     * The collection of tags for this addon
     */
    private final Collection<String> tags;
    /**
     * Whether the addon is currently installed on the platform
     */
    private boolean isInstalled;

    /**
     * Gets the description of the pricing policy for the addon
     *
     * @return The description of the pricing policy for the addon
     */
    public String getPricing() {
        return pricing;
    }

    /**
     * Gets the collection of the bundles that compose this addon
     *
     * @return The collection of the bundles that compose this addon
     */
    public Collection<AddonBundle> getBundles() {
        return Collections.unmodifiableCollection(bundles);
    }

    /**
     * Gets whether the addon is currently installed on the platform
     *
     * @return Whether the addon is currently installed on the platform
     */
    public boolean isInstalled() {
        return isInstalled;
    }

    /**
     * Gets the tags for this addon
     *
     * @return The tags for this addon
     */
    public Collection<String> getTags() {
        return Collections.unmodifiableCollection(tags);
    }

    /**
     * Sets this addon as installed
     */
    public void setInstalled() {
        isInstalled = true;
    }

    /**
     * Initializes this addon description
     *
     * @param root The root of the JSON node describing the addon
     */
    public Addon(ASTNode root) {
        String pricing = "";
        bundles = new ArrayList<>();
        tags = new ArrayList<>();
        isInstalled = false;
        for (ASTNode member : root.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("description".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                description = value.substring(1, value.length() - 1);
            } else if ("version".equals(head)) {
                String versionNumber = "";
                String versionScmTag = "";
                String versionBuildTag = "";
                String versionBuildUser = "";
                String versionBuildTimestamp = "";
                for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                    head = TextUtils.unescape(member2.getChildren().get(0).getValue());
                    head = head.substring(1, head.length() - 1);
                    if ("number".equals(head)) {
                        String value = TextUtils.unescape(member2.getChildren().get(1).getValue());
                        versionNumber = value.substring(1, value.length() - 1);
                    } else if ("scmTag".equals(head)) {
                        String value = TextUtils.unescape(member2.getChildren().get(1).getValue());
                        versionScmTag = value.substring(1, value.length() - 1);
                    } else if ("buildUser".equals(head)) {
                        String value = TextUtils.unescape(member2.getChildren().get(1).getValue());
                        versionBuildTag = value.substring(1, value.length() - 1);
                    } else if ("buildTag".equals(head)) {
                        String value = TextUtils.unescape(member2.getChildren().get(1).getValue());
                        versionBuildUser = value.substring(1, value.length() - 1);
                    } else if ("buildTimestamp".equals(head)) {
                        String value = TextUtils.unescape(member2.getChildren().get(1).getValue());
                        versionBuildTimestamp = value.substring(1, value.length() - 1);
                    }
                }
                version = new VersionInfo(versionNumber, versionScmTag, versionBuildUser, versionBuildTag, versionBuildTimestamp);
            } else if ("copyright".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                copyright = value.substring(1, value.length() - 1);
            } else if ("iconName".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                iconName = value.substring(1, value.length() - 1);
            } else if ("iconContent".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                iconContent = value.substring(1, value.length() - 1);
            } else if ("vendor".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                vendor = value.substring(1, value.length() - 1);
            } else if ("vendorLink".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                vendorLink = value.substring(1, value.length() - 1);
            } else if ("link".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                link = value.substring(1, value.length() - 1);
            } else if ("license".equals(head)) {
                String licenseName = "";
                String licenseFullText = "";
                for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                    head = TextUtils.unescape(member2.getChildren().get(0).getValue());
                    head = head.substring(1, head.length() - 1);
                    if ("name".equals(head)) {
                        String value = TextUtils.unescape(member2.getChildren().get(1).getValue());
                        licenseName = value.substring(1, value.length() - 1);
                    } else if ("fullText".equals(head)) {
                        String value = TextUtils.unescape(member2.getChildren().get(1).getValue());
                        licenseFullText = value.substring(1, value.length() - 1);
                    }
                }
                license = new LicensePlain(licenseName, licenseFullText);
            } else if ("pricing".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                pricing = value.substring(1, value.length() - 1);
            } else if ("link".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                link = value.substring(1, value.length() - 1);
            } else if ("bundles".equals(head)) {
                for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                    bundles.add(new AddonBundle(member2));
                }
            } else if ("tags".equals(head)) {
                for (ASTNode member2 : member.getChildren().get(1).getChildren()) {
                    String value = TextUtils.unescape(member2.getValue());
                    tags.add(value.substring(1, value.length() - 1));
                }
            }
        }
        this.pricing = pricing;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(Addon.class.getCanonicalName()));
        builder.append("\"");
        serializedJSONBase(builder);
        builder.append(", \"pricing\": \"");
        builder.append(TextUtils.escapeStringJSON(pricing));
        builder.append("\", \"bundles\": [");
        boolean first = true;
        for (AddonBundle bundle : bundles) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(bundle.serializedJSON());
        }
        builder.append("], \"tags\": [");
        first = true;
        for (String tag : tags) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(tag));
            builder.append("\"");
        }
        builder.append("], \"isInstalled\": \"");
        builder.append(isInstalled);
        builder.append("\"}");
        return builder.toString();
    }
}
