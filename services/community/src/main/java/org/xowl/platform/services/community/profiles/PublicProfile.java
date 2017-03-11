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

package org.xowl.platform.services.community.profiles;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.Base64;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.platform.PlatformUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents the profile of a user on the platform
 *
 * @author Laurent Wouters
 */
public class PublicProfile implements Identifiable, Serializable {
    /**
     * The identifier of this user
     */
    private final String identifier;
    /**
     * The email to reach this user
     */
    private final String email;
    /**
     * The MIME type for the avatar
     */
    private final String avatarMime;
    /**
     * The content for the avatar
     */
    private final byte[] avatarContent;
    /**
     * The user's affiliation, if any
     */
    private final String organization;
    /**
     * The user's occupation, if any
     */
    private final String occupation;
    /**
     * The badges associated to this profile
     */
    private final Collection<Badge> badges;

    /**
     * Initializes this user profile
     *
     * @param user The user to initializes the profile from
     */
    public PublicProfile(PlatformUser user) {
        this.identifier = user.getIdentifier();
        this.email = "";
        this.avatarMime = "";
        this.avatarContent = new byte[0];
        this.organization = "";
        this.occupation = "";
        this.badges = new ArrayList<>();
    }

    /**
     * Initializes this user profile
     *
     * @param definition The AST node for the serialized definition
     */
    public PublicProfile(ASTNode definition) {
        this.badges = new ArrayList<>();
        String identifier = "";
        String email = "";
        String avatarMime = "";
        byte[] avatarContent = null;
        String organization = "";
        String occupation = "";
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("email".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                email = value.substring(1, value.length() - 1);
            } else if ("avatarMime".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                avatarMime = value.substring(1, value.length() - 1);
            } else if ("avatarContent".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                String data = value.substring(1, value.length() - 1);
                avatarContent = Base64.decodeBase64(data);
            } else if ("organization".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                organization = value.substring(1, value.length() - 1);
            } else if ("occupation".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                occupation = value.substring(1, value.length() - 1);
            } else if ("badges".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren())
                    badges.add(new Badge(child));
            }
        }
        this.identifier = identifier;
        this.email = email;
        this.avatarMime = avatarMime;
        this.avatarContent = (avatarContent == null ? new byte[0] : avatarContent);
        this.organization = organization;
        this.occupation = occupation;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return identifier;
    }

    /**
     * Gets the email to reach this user
     *
     * @return The email to reach this user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the MIME type for the avatar
     *
     * @return The MIME type for the avatar
     */
    public String getAvatarMime() {
        return avatarMime;
    }

    /**
     * Gets the content for the avatar
     *
     * @return The content for the avatar
     */
    public byte[] getAvatarContent() {
        return avatarContent;
    }

    /**
     * Gets the user's affiliation, if any
     *
     * @return The user's affiliation, if any
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * Gets the user's occupation, if any
     *
     * @return The user's occupation, if any
     */
    public String getOccupation() {
        return occupation;
    }

    /**
     * Gets the badges associated to this profile
     *
     * @return The badges associated to this profile
     */
    public Collection<Badge> getBadges() {
        return Collections.unmodifiableCollection(badges);
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(PublicProfile.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"email\": \"");
        builder.append(TextUtils.escapeStringJSON(email));
        builder.append("\", \"avatarMime\": \"");
        builder.append(TextUtils.escapeStringJSON(avatarMime));
        builder.append("\", \"avatarContent\": \"");
        builder.append(Base64.encodeBase64(avatarContent));
        builder.append("\", \"organization\": \"");
        builder.append(TextUtils.escapeStringJSON(organization));
        builder.append("\", \"occupation\": \"");
        builder.append(TextUtils.escapeStringJSON(occupation));
        builder.append("\", \"badges\": [");
        boolean first = true;
        for (Badge badge : badges) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(badge.serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}
