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

package org.xowl.platform.services.security.internal;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.utils.config.Section;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.remote.DeserializerForOSGi;
import org.xowl.platform.kernel.remote.RemotePlatformAccess;
import org.xowl.platform.kernel.remote.RemotePlatformAccessManager;
import org.xowl.platform.kernel.remote.RemotePlatformAccessProvider;
import org.xowl.platform.kernel.security.SecurityService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * An authentication realm that uses a remote xOWL platform as a user base, but still manages its own set of groups and roles
 *
 * @author Laurent Wouters
 */
class XOWLSubordinateRealm extends XOWLInternalRealm implements RemotePlatformAccessProvider {
    /**
     * The access manager to use
     */
    private final RemotePlatformAccessManager accessManager;

    /**
     * Initialize this realm
     *
     * @param configuration The configuration for the realm
     */
    public XOWLSubordinateRealm(Section configuration) {
        super(configuration);
        this.accessManager = new RemotePlatformAccessManager(configuration.get("master"), new DeserializerForOSGi());
    }

    @Override
    public String getIdentifier() {
        return XOWLSubordinateRealm.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Subordinate Realm";
    }

    @Override
    public String getEndpoint() {
        return accessManager.getEndpoint();
    }

    @Override
    public RemotePlatformAccess getAccess(String userId) {
        return accessManager.getAccess(userId);
    }

    @Override
    public PlatformUser authenticate(String login, String password) {
        XSPReply reply = getAccess(login).login(login, password);
        if (!reply.isSuccess())
            return null;
        PlatformUser result = ((XSPReplyResult<PlatformUser>) reply).getData();
        return getUser(result.getIdentifier(), result.getName());
    }

    @Override
    public Collection<PlatformUser> getUsers() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return Collections.emptyList();
        PlatformUser currentUser = securityService.getCurrentUser();

        XSPReply reply = getAccess(currentUser.getIdentifier()).getPlatformUsers();
        if (!reply.isSuccess())
            return Collections.emptyList();
        Collection<PlatformUser> result = new ArrayList<>();
        for (PlatformUser remoterUser : ((XSPReplyResultCollection<PlatformUser>) reply).getData()) {
            result.add(getUser(remoterUser.getIdentifier(), remoterUser.getName()));
        }
        return result;
    }

    @Override
    public PlatformUser getUser(String identifier) {
        PlatformUser result = cacheUsers.get(identifier);
        if (result != null)
            return result;
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();

        XSPReply reply = getAccess(currentUser == null ? identifier : currentUser.getIdentifier()).getPlatformUser(identifier);
        if (!reply.isSuccess())
            return null;
        result = ((XSPReplyResult<PlatformUser>) reply).getData();
        return getUser(result.getIdentifier(), result.getName());
    }

    @Override
    public XSPReply createUser(String identifier, String name, String key) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getAccess(currentUser.getIdentifier()).createPlatformUser(identifier, name, key);
    }

    @Override
    public XSPReply renameUser(String identifier, String name) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getAccess(currentUser.getIdentifier()).renamePlatformUser(identifier, name);
    }

    @Override
    public XSPReply deleteUser(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getAccess(currentUser.getIdentifier()).deletePlatformUser(identifier);
    }

    @Override
    public XSPReply changeUserKey(String identifier, String oldKey, String newKey) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getAccess(currentUser.getIdentifier()).changePlatformUserPassword(identifier, oldKey, newKey);
    }

    @Override
    public XSPReply resetUserKey(String identifier, String newKey) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getAccess(currentUser.getIdentifier()).resetPlatformUserPassword(identifier, newKey);
    }
}