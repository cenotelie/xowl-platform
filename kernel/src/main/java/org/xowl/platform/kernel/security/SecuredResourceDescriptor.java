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
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.platform.PlatformUserRoot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents the security descriptor of a secured resource
 *
 * @author Laurent Wouters
 */
public class SecuredResourceDescriptor implements Identifiable, Serializable {
    /**
     * The resource's identifier
     */
    private final String identifier;
    /**
     * The resource's name
     */
    private final String name;
    /**
     * The resource's current owners
     */
    private final Collection<String> owners;
    /**
     * The sharing of the resource
     */
    private final Collection<SecuredResourceSharing> sharings;

    /**
     * Initializes this descriptor
     *
     * @param resource The associated secured resource
     */
    public SecuredResourceDescriptor(SecuredResource resource) {
        this.identifier = resource.getIdentifier();
        this.name = resource.getName();
        this.owners = new ArrayList<>();
        this.sharings = new ArrayList<>();
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
    public SecuredResourceDescriptor(ASTNode node) {
        String identifier = "";
        String name = "";
        this.owners = new ArrayList<>();
        this.sharings = new ArrayList<>();
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
                            sharings.add(sharing);
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
    private static SecuredResourceSharing loadSharing(ASTNode node) {
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

    /**
     * Gets the owners of this resource
     *
     * @return The owners of this resource
     */
    public Collection<String> getOwners() {
        return Collections.unmodifiableCollection(owners);
    }

    /**
     * Adds an owner of this resource
     *
     * @param user The new owner for this resource
     * @return The protocol reply
     */
    public XSPReply addOwner(PlatformUser user) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_MANAGE_RESOURCE_OWNERSHIP, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (owners) {
            if (owners.contains(user.getIdentifier()))
                return new XSPReplyApiError(SecuredResourceManager.ERROR_ALREADY_OWNER);
            owners.add(user.getIdentifier());
            reply = onOwnerChanged(user, true);
            if (!reply.isSuccess())
                owners.remove(user.getIdentifier());
            return reply;
        }
    }

    /**
     * Removes an owner of this resource
     *
     * @param user The previous owner for this resource
     * @return The protocol reply
     */
    public XSPReply removeOwner(PlatformUser user) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_MANAGE_RESOURCE_OWNERSHIP, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (owners) {
            if (owners.size() == 1)
                return new XSPReplyApiError(SecuredResourceManager.ERROR_LAST_OWNER);
            boolean removed = owners.remove(user.getIdentifier());
            if (!removed)
                return XSPReplyNotFound.instance();
            reply = onOwnerChanged(user, false);
            if (!reply.isSuccess())
                owners.add(user.getIdentifier());
            return reply;
        }
    }

    /**
     * Event when the owner has changed
     *
     * @param user  The changed owner
     * @param added Whether the owner was added
     * @return The to accept the change
     */
    protected XSPReply onOwnerChanged(PlatformUser user, boolean added) {
        return XSPReplySuccess.instance();
    }

    /**
     * Gets the specifications of how this resource is shared
     *
     * @return The specifications of how this resource is shared
     */
    public Collection<SecuredResourceSharing> getSharings() {
        return Collections.unmodifiableCollection(sharings);
    }

    /**
     * Adds a sharing for this resource
     *
     * @param sharing The sharing to add
     * @return The protocol reply
     */
    public XSPReply addSharing(SecuredResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_MANAGE_RESOURCE_SHARING, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (sharings) {
            sharings.add(sharing);
            reply = onSharingChanged(sharing, true);
            if (!reply.isSuccess())
                sharings.remove(sharing);
            return reply;
        }
    }

    /**
     * Remove a sharing for this resource
     *
     * @param sharing The sharing to remove
     * @return The protocol reply
     */
    public XSPReply removeSharing(SecuredResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecurityService.ACTION_MANAGE_RESOURCE_SHARING, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (sharings) {
            for (SecuredResourceSharing candidate : sharings) {
                if (candidate.equals(sharing)) {
                    sharings.remove(candidate);
                    reply = onSharingChanged(sharing, false);
                    if (!reply.isSuccess())
                        sharings.add(candidate);
                    return reply;
                }
            }
        }
        return XSPReplyNotFound.instance();
    }

    /**
     * Event when the sharing specification changed
     *
     * @param sharing The changed sharing
     * @param added   Whether the sharing was added
     * @return The to accept the change
     */
    protected XSPReply onSharingChanged(SecuredResourceSharing sharing, boolean added) {
        return XSPReplySuccess.instance();
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
        builder.append(TextUtils.escapeStringJSON(getIdentifier()));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(getName()));
        builder.append("\", \"owner\": [");
        boolean first = true;
        for (String owner : getOwners()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("\"");
            builder.append(TextUtils.escapeStringJSON(owner));
            builder.append("\"");
        }

        builder.append("], \"sharing\": [");
        first = true;
        for (SecuredResourceSharing sharing : getSharings()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(sharing.serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}
