/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.kernel.impl;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.xowl.platform.kernel.SecurityService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Implements the authorization for the platform
 *
 * @author Laurent Wouters
 */
public class XOWLSecurityAuthorizer implements Authorizer {
    @Override
    public boolean isPermitted(PrincipalCollection principalCollection, String permission) {
        return false;
    }

    @Override
    public boolean isPermitted(PrincipalCollection principalCollection, Permission permission) {
        return false;
    }

    @Override
    public boolean[] isPermitted(PrincipalCollection principalCollection, String... permissions) {
        return new boolean[permissions.length];
    }

    @Override
    public boolean[] isPermitted(PrincipalCollection principalCollection, List<Permission> permissions) {
        return new boolean[permissions.size()];
    }

    @Override
    public boolean isPermittedAll(PrincipalCollection principalCollection, String... permissions) {
        return false;
    }

    @Override
    public boolean isPermittedAll(PrincipalCollection principalCollection, Collection<Permission> permissions) {
        return false;
    }

    @Override
    public void checkPermission(PrincipalCollection principalCollection, String permissions) throws AuthorizationException {
        throw new AuthorizationException("Unsupported");
    }

    @Override
    public void checkPermission(PrincipalCollection principalCollection, Permission permissions) throws AuthorizationException {
        throw new AuthorizationException("Unsupported");
    }

    @Override
    public void checkPermissions(PrincipalCollection principalCollection, String... permissions) throws AuthorizationException {
        throw new AuthorizationException("Unsupported");
    }

    @Override
    public void checkPermissions(PrincipalCollection principalCollection, Collection<Permission> permissions) throws AuthorizationException {
        throw new AuthorizationException("Unsupported");
    }

    @Override
    public boolean hasRole(PrincipalCollection principalCollection, String role) {
        for (Object principal : principalCollection) {
            if (hasRole(principal.toString(), role))
                return true;
        }
        return false;
    }

    @Override
    public boolean[] hasRoles(PrincipalCollection principalCollection, List<String> roles) {
        boolean[] result = new boolean[roles.size()];
        for (int i = 0; i != roles.size(); i++) {
            for (Object principal : principalCollection) {
                if (hasRole(principal.toString(), roles.get(i))) {
                    result[i] = true;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasAllRoles(PrincipalCollection principalCollection, Collection<String> roles) {
        for (String role : roles) {
            boolean found = false;
            for (Object principal : principalCollection) {
                if (hasRole(principal.toString(), role)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                return false;
        }
        return true;
    }

    @Override
    public void checkRole(PrincipalCollection principalCollection, String role) throws AuthorizationException {
        if (!hasRole(principalCollection, role))
            throw new AuthorizationException("Unauthorized");
    }

    @Override
    public void checkRoles(PrincipalCollection principalCollection, Collection<String> roles) throws AuthorizationException {
        if (!hasAllRoles(principalCollection, roles))
            throw new AuthorizationException("Unauthorized");
    }

    @Override
    public void checkRoles(PrincipalCollection principalCollection, String... roles) throws AuthorizationException {
        if (!hasAllRoles(principalCollection, Arrays.asList(roles)))
            throw new AuthorizationException("Unauthorized");
    }

    /**
     * Gets whether a principal has a role
     *
     * @param principal The principal
     * @param role      The role to look for
     * @return Whether the principal has the role
     */
    private boolean hasRole(String principal, String role) {
        if (SecurityService.ROLE_ADMIN.equals(role))
            return "admin".equals(principal);
        // FIXME: properly implement this
        return true;
    }
}
