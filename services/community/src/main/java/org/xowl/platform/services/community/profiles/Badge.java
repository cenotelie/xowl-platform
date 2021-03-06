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

import fr.cenotelie.commons.utils.*;
import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.hime.redist.ASTNode;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a badge for a user on the platform
 *
 * @author Laurent Wouters
 */
public class Badge implements Identifiable, Serializable {
    /**
     * The identifier of this badge
     */
    private final String identifier;
    /**
     * The name of this badge
     */
    private final String name;
    /**
     * The MIME type for the image
     */
    private final String imageMime;
    /**
     * The content of the image
     */
    private final byte[] imageContent;
    /**
     * The description of this badge
     */
    private final String description;

    /**
     * Creates a badge from a resource
     *
     * @param identifier  The identifier of this badge
     * @param name        The name of this badge
     * @param description The description of this badge
     * @param imageMime   The MIME type for the image
     * @param parentType  The parent type to access resources
     * @param resources   The path to the resource
     */
    public Badge(String identifier, String name, String description, String imageMime, Class parentType, String resources) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.imageMime = imageMime;
        byte[] buffer;
        try (InputStream stream = parentType.getResourceAsStream(resources)) {
            buffer = IOUtils.load(stream);
        } catch (IOException exception) {
            Logging.get().error(exception);
            buffer = new byte[0];
        }
        this.imageContent = buffer;
    }

    /**
     * Initializes this user profile
     *
     * @param definition The AST node for the serialized definition
     */
    public Badge(ASTNode definition) {
        String identifier = "";
        String name = "";
        String imageMime = "";
        byte[] imageContent = null;
        String description = "";
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("identifier".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                identifier = value.substring(1, value.length() - 1);
            } else if ("name".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                name = value.substring(1, value.length() - 1);
            } else if ("imageMime".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                imageMime = value.substring(1, value.length() - 1);
            } else if ("imageContent".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                String data = value.substring(1, value.length() - 1);
                imageContent = Base64.decodeBase64(data);
            } else if ("description".equals(head)) {
                String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                description = value.substring(1, value.length() - 1);
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.imageMime = imageMime;
        this.imageContent = (imageContent == null ? new byte[0] : imageContent);
        this.description = description;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the MIME type for the image
     *
     * @return The MIME type for the image
     */
    public String getImageMime() {
        return imageMime;
    }

    /**
     * Gets the content of the image
     *
     * @return The content of the image
     */
    public byte[] getImageContent() {
        return imageContent;
    }

    /**
     * Gets the description of this badge
     *
     * @return The description of this badge
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" + TextUtils.escapeStringJSON(PublicProfile.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"imageMime\": \"" +
                TextUtils.escapeStringJSON(imageMime) +
                "\", \"imageContent\": \"" +
                Base64.encodeBase64(imageContent) +
                "\", \"description\": \"" +
                TextUtils.escapeStringJSON(description) +
                "\"}";
    }
}
