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
import fr.cenotelie.hime.redist.ASTNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements a representation of a group from a remote platform
 *
 * @author Laurent Wouters
 */
public class PlatformGroupRemote extends PlatformGroupBase {
    /**
     * The administrators for this group
     */
    private final Collection<PlatformUser> admins;
    /**
     * The users for this group
     */
    private final Collection<PlatformUser> members;
    /**
     * The roles for this group
     */
    private final Collection<PlatformRole> roles;

    /**
     * Initializes this group
     *
     * @param definition The AST node for the serialized definition
     */
    public PlatformGroupRemote(ASTNode definition) {
        super(definition);
        this.members = new ArrayList<>();
        this.admins = new ArrayList<>();
        this.roles = new ArrayList<>();
        for (ASTNode member : definition.getChildren()) {
            String head = TextUtils.unescape(member.getChildren().get(0).getValue());
            head = head.substring(1, head.length() - 1);
            if ("members".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    this.members.add(new PlatformUserRemote(child));
                }
            } else if ("admins".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    this.admins.add(new PlatformUserRemote(child));
                }
            } else if ("roles".equals(head)) {
                for (ASTNode child : member.getChildren().get(1).getChildren()) {
                    this.roles.add(new PlatformRoleBase(child));
                }
            }
        }
    }

    @Override
    public Collection<PlatformUser> getUsers() {
        return Collections.unmodifiableCollection(members);
    }

    @Override
    public Collection<PlatformUser> getAdmins() {
        return Collections.unmodifiableCollection(admins);
    }

    @Override
    public Collection<PlatformRole> getRoles() {
        return Collections.unmodifiableCollection(roles);
    }
}
