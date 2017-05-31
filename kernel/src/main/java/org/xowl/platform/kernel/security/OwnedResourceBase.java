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
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.platform.PlatformUserRoot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Base implementation of an owned resource
 *
 * @author Laurent Wouters
 */
public class OwnedResourceBase implements OwnedResource {
    /**
     * The resource's identifier
     */
    protected final String identifier;
    /**
     * The resource's name
     */
    protected final String name;
    /**
     * The resource's current owner
     */
    private String owner;
    /**
     * The sharing of the resource
     */
    private final Collection<OwnedResourceSharing> sharings;

    /**
     * Initializes this resource
     *
     * @param identifier The resource's identifier
     * @param name       he resource's name
     */
    protected OwnedResourceBase(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
        this.sharings = new ArrayList<>();
        SecurityService securityService = Register.getComponent(SecurityService.class);
        PlatformUser currentUser = securityService == null ? null : securityService.getCurrentUser();
        this.owner = currentUser == null ? PlatformUserRoot.INSTANCE.getIdentifier() : currentUser.getIdentifier();
    }

    /**
     * Initializes this resource
     *
     * @param node The descriptor node to load from
     */
    public OwnedResourceBase(ASTNode node) {
        String identifier = "";
        String name = "";
        String owner = "";
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
                case "owner": {
                    String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
                    value = value.substring(1, value.length() - 1);
                    owner = value;
                    break;
                }
                case "sharing": {
                    for (ASTNode child : pair.getChildren().get(1).getChildren()) {
                        OwnedResourceSharing sharing = loadSharing(child);
                        if (sharing != null)
                            sharings.add(sharing);
                    }
                    break;
                }
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.owner = owner;
    }

    /**
     * Loads a sharing serialized in the specified node
     *
     * @param node A serialized definition
     * @return The sharing
     */
    private static OwnedResourceSharing loadSharing(ASTNode node) {
        String type = getObjectType(node);
        if (type == null)
            return null;
        if (OwnedResourceSharingWithEverybody.class.getCanonicalName().equals(type))
            return new OwnedResourceSharingWithEverybody(node);
        if (OwnedResourceSharingWithUser.class.getCanonicalName().equals(type))
            return new OwnedResourceSharingWithUser(node);
        if (OwnedResourceSharingWithGroup.class.getCanonicalName().equals(type))
            return new OwnedResourceSharingWithGroup(node);
        if (OwnedResourceSharingWithRole.class.getCanonicalName().equals(type))
            return new OwnedResourceSharingWithRole(node);
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
    public String getOwner() {
        return owner;
    }

    @Override
    public final synchronized XSPReply setOwner(PlatformUser user) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(OwnedResource.ACTION_CHANGE_OWNER, this);
        if (!reply.isSuccess())
            return reply;
        String oldOwned = this.owner;
        this.owner = user.getIdentifier();
        onOwnedChanged(oldOwned, user);
        return XSPReplySuccess.instance();
    }

    /**
     * Event when the owner has changed
     *
     * @param oldOwner The old owner
     * @param newOwned The new owner
     */
    protected void onOwnedChanged(String oldOwner, PlatformUser newOwned) {
        // by default do nothing
    }

    @Override
    public final Collection<OwnedResourceSharing> getSharings() {
        return Collections.unmodifiableCollection(sharings);
    }

    @Override
    public final XSPReply addSharing(OwnedResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(OwnedResource.ACTION_MANAGE_SHARING, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (sharings) {
            sharings.add(sharing);
            onSharingChanged();
        }
        return XSPReplySuccess.instance();
    }

    @Override
    public final XSPReply removeSharing(OwnedResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(OwnedResource.ACTION_MANAGE_SHARING, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (sharings) {
            for (OwnedResourceSharing candidate : sharings) {
                if (candidate.equals(sharing)) {
                    sharings.remove(candidate);
                    onSharingChanged();
                    return XSPReplySuccess.instance();
                }
            }
        }
        return XSPReplyNotFound.instance();
    }

    /**
     * Event when the sharing specification changed
     */
    protected void onSharingChanged() {
        // by default, do nothing
    }

    @Override
    public String serializedString() {
        return getIdentifier();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(this.getClass().getCanonicalName()));
        builder.append("\"");
        serializedJsonBase(builder);
        builder.append("}");
        return builder.toString();
    }

    /**
     * Serialized the base attributes of this resource
     *
     * @param builder The string builder to use
     */
    protected void serializedJsonBase(StringBuilder builder) {
        builder.append(", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(getIdentifier()));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(getName()));
        builder.append("\", \"owner\": \"");
        builder.append(TextUtils.escapeStringJSON(getOwner()));
        builder.append("\", \"sharing\": [");
        boolean first = true;
        for (OwnedResourceSharing sharing : getSharings()) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(sharing.serializedJSON());
        }
        builder.append("]");
    }

    /**
     * Checks whether the current user has access to this resource
     *
     * @return Whether the current user has access to this resource
     */
    public XSPReply checkAccess() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        return securityService.checkAction(OwnedResource.ACTION_ACCESS, this);
    }
}
