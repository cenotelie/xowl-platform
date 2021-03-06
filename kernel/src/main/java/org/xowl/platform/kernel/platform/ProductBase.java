/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.product.LicensePlain;
import fr.cenotelie.commons.utils.product.Product;
import fr.cenotelie.commons.utils.product.VersionInfo;
import fr.cenotelie.hime.redist.ASTNode;

/**
 * Extends the base description of a product
 *
 * @author Laurent Wouters
 */
public class ProductBase extends Product {
    /**
     * Initializes this addon description
     *
     * @param root The root of the JSON node describing the addon
     */
    public ProductBase(ASTNode root) {
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
                String versionBuildUser = "";
                String versionBuildTag = "";
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
                        versionBuildUser = value.substring(1, value.length() - 1);
                    } else if ("buildTag".equals(head)) {
                        String value = TextUtils.unescape(member2.getChildren().get(1).getValue());
                        versionBuildTag = value.substring(1, value.length() - 1);
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
            }
        }
    }
}
