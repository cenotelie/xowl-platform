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
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.platform.PlatformUserRoot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Base implementation of an secured resource
 *
 * @author Laurent Wouters
 */
public class SecuredResourceBase implements SecuredResource {
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
    private final Collection<String> owners;
    /**
     * The sharing of the resource
     */
    private final Collection<SecuredResourceSharing> sharings;

    /**
     * Initializes this resource
     *
     * @param identifier The resource's identifier
     * @param name       he resource's name
     */
    protected SecuredResourceBase(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
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
    public SecuredResourceBase(ASTNode node) {
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

    @Override
    public final Collection<String> getOwners() {
        return Collections.unmodifiableCollection(owners);
    }

    @Override
    public final XSPReply addOwner(PlatformUser user) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecuredResource.ACTION_MANAGE_OWNERSHIP, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (owners) {
            if (owners.contains(user.getIdentifier()))
                return new XSPReplyApiError(SecuredResource.ERROR_ALREADY_OWNER);
            owners.add(user.getIdentifier());
            onOwnedChanged(user, true);
        }
        return XSPReplySuccess.instance();
    }

    @Override
    public final XSPReply removeOwner(PlatformUser user) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecuredResource.ACTION_MANAGE_OWNERSHIP, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (owners) {
            if (owners.size() == 1)
                return new XSPReplyApiError(SecuredResource.ERROR_LAST_OWNER);
            boolean removed = owners.remove(user.getIdentifier());
            if (!removed)
                return XSPReplyNotFound.instance();
            onOwnedChanged(user, false);
        }
        return XSPReplyNotFound.instance();
    }

    /**
     * Event when the owner has changed
     *
     * @param user  The changed owner
     * @param added Whether the owner was added
     */
    protected void onOwnedChanged(PlatformUser user, boolean added) {
        // by default do nothing
    }

    @Override
    public final Collection<SecuredResourceSharing> getSharings() {
        return Collections.unmodifiableCollection(sharings);
    }

    @Override
    public final XSPReply addSharing(SecuredResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecuredResource.ACTION_MANAGE_SHARING, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (sharings) {
            sharings.add(sharing);
            onSharingChanged(sharing, true);
        }
        return XSPReplySuccess.instance();
    }

    @Override
    public final XSPReply removeSharing(SecuredResourceSharing sharing) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        XSPReply reply = securityService.checkAction(SecuredResource.ACTION_MANAGE_SHARING, this);
        if (!reply.isSuccess())
            return reply;
        synchronized (sharings) {
            for (SecuredResourceSharing candidate : sharings) {
                if (candidate.equals(sharing)) {
                    sharings.remove(candidate);
                    onSharingChanged(sharing, false);
                    return XSPReplySuccess.instance();
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
     */
    protected void onSharingChanged(SecuredResourceSharing sharing, boolean added) {
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
        return securityService.checkAction(SecuredResource.ACTION_ACCESS, this);
    }
}
