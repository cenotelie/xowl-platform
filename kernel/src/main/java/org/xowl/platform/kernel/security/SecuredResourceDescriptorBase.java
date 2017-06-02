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

package org.xowl.platform.kernel.security;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.platform.PlatformUserRoot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Base implementation of the security descriptor for a secured resource
 *
 * @author Laurent Wouters
 */
public class SecuredResourceDescriptorBase implements SecuredResourceDescriptor {
    /**
     * The resource's identifier
     */
    protected final String identifier;
    /**
     * The resource's name
     */
    protected final String name;
    /**
     * The resource's current owners
     */
    protected final Collection<String> owners;
    /**
     * The sharing of the resource
     */
    protected final Collection<SecuredResourceSharing> sharing;

    /**
     * Initializes this descriptor
     *
     * @param resource The associated secured resource
     */
    public SecuredResourceDescriptorBase(SecuredResource resource) {
        this.identifier = resource.getIdentifier();
        this.name = resource.getName();
        this.owners = new ArrayList<>();
        this.sharing = new ArrayList<>();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        PlatformUser currentUser = securityService == null ? null : securityService.getCurrentUser();
        if (currentUser == null)
            currentUser = PlatformUserRoot.INSTANCE;
        this.owners.add(currentUser.getIdentifier());
    }

    /**
     * Initializes this resource
     *
     * @param node The descriptor node to load from
     */
    public SecuredResourceDescriptorBase(ASTNode node) {
        String identifier = "";
        String name = "";
        this.owners = new ArrayList<>();
        this.sharing = new ArrayList<>();
        for (ASTNode pair : node.getChildren()) {
            String key = TextUtils.unescape(pair.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "identifier": {
                    String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
                    value = value.substring(1, value.length() - 1);
                    identifier = value;
                    break;
                }
                case "name": {
                    String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
                    value = value.substring(1, value.length() - 1);
                    name = value;
                    break;
                }
                case "owners": {
                    for (ASTNode child : pair.getChildren().get(1).getChildren()) {
                        String value = TextUtils.unescape(child.getValue());
                        value = value.substring(1, value.length() - 1);
                        owners.add(value);
                    }
                    break;
                }
                case "sharing": {
                    for (ASTNode child : pair.getChildren().get(1).getChildren()) {
                        SecuredResourceSharing sharing = loadSharing(child);
                        if (sharing != null)
                            this.sharing.add(sharing);
                    }
                    break;
                }
            }
        }
        this.identifier = identifier;
        this.name = name;
    }

    /**
     * Loads a sharing serialized in the specified node
     *
     * @param node A serialized definition
     * @return The sharing
     */
    public static SecuredResourceSharing loadSharing(ASTNode node) {
        String type = getObjectType(node);
        if (type == null)
            return null;
        if (SecuredResourceSharingWithEverybody.class.getCanonicalName().equals(type))
            return new SecuredResourceSharingWithEverybody(node);
        if (SecuredResourceSharingWithUser.class.getCanonicalName().equals(type))
            return new SecuredResourceSharingWithUser(node);
        if (SecuredResourceSharingWithGroup.class.getCanonicalName().equals(type))
            return new SecuredResourceSharingWithGroup(node);
        if (SecuredResourceSharingWithRole.class.getCanonicalName().equals(type))
            return new SecuredResourceSharingWithRole(node);
        return null;
    }

    /**
     * Gets the type of the serialized object
     *
     * @param node A serialized object
     * @return The object's type
     */
    private static String getObjectType(ASTNode node) {
        for (ASTNode pair : node.getChildren()) {
            String key = TextUtils.unescape(pair.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            switch (key) {
                case "type": {
                    String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
                    return value.substring(1, value.length() - 1);
                }
            }
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<String> getOwners() {
        return Collections.unmodifiableCollection(owners);
    }

    @Override
    public Collection<SecuredResourceSharing> getSharing() {
        return Collections.unmodifiableCollection(sharing);
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(SecuredResourceDescriptor.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(identifier));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"owner\": [");
        boolean first = true;
        for (String owner : owners) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(owner));
            builder.append("\"");
        }

        builder.append("], \"sharing\": [");
        first = true;
        for (SecuredResourceSharing sharing : this.sharing) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(sharing.serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}
