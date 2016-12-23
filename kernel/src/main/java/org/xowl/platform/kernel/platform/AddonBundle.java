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
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;

/**
 * Describes a bundle that is part of an addon
 *
 * @author Laurent Wouters
 */
public class AddonBundle implements Serializable {
    /**
     * The group id for the bundle
     */
    private final String groupId;
    /**
     * The artifact id for the bundle
     */
    private final String artifactId;
    /**
     * The version number for the bundle
     */
    private final String version;

    /**
     * Gets the group id for the bundle
     *
     * @return The group id for the bundle
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Gets the artifact id for the bundle
     *
     * @return The artifact id for the bundle
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Gets the version number for the bundle
     *
     * @return The version number for the bundle
     */
    public String getVersion() {
        return version;
    }

    /**
     * Initializes this description
     *
     * @param root The root for the description
     */
    public AddonBundle(ASTNode root) {
        String groupId = "";
        String artifactId = "";
        String version = "";
        for (ASTNode member : root.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("groupId".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                groupId = value.substring(1, value.length() - 1);
            } else if ("artifactId".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                artifactId = value.substring(1, value.length() - 1);
            } else if ("version".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                version = value.substring(1, value.length() - 1);
            }
        }
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public String serializedString() {
        return groupId + "." + artifactId + "-" + version;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                AddonBundle.class.getCanonicalName() +
                "\", \"groupId\": \"" +
                TextUtils.escapeStringJSON(groupId) +
                "\", \"artifactId\": \"" +
                TextUtils.escapeStringJSON(artifactId) +
                "\", \"version\": \"" +
                TextUtils.escapeStringJSON(version) +
                "\"}";
    }
}
