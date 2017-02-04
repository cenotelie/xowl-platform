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
import org.xowl.platform.kernel.remote.Deserializer;
import org.xowl.platform.kernel.remote.DeserializerForOSGi;
import org.xowl.platform.kernel.remote.RemotePlatformAccess;
import org.xowl.platform.kernel.security.SecurityService;

import java.util.*;

/**
 * An authentication realm that uses a remote xOWL platform as a user base, but still manages its own set of groups and roles
 *
 * @author Laurent Wouters
 */
public class XOWLSubordinateRealm extends XOWLInternalRealm {
    /**
     * The deserializer to use
     */
    private final Deserializer deserializer;
    /**
     * The API endpoint for the master platform
     */
    private final String masterEndpoint;
    /**
     * The user connections to the master platform
     */
    private final Map<String, RemotePlatformAccess> connections;

    /**
     * Initialize this realm
     *
     * @param configuration The configuration for the realm
     */
    public XOWLSubordinateRealm(Section configuration) {
        super(configuration);
        this.deserializer = new DeserializerForOSGi();
        this.masterEndpoint = configuration.get("master");
        this.connections = new HashMap<>();
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
    public PlatformUser authenticate(String login, String password) {
        XSPReply reply = getRemotePlatformFor(login).login(login, password);
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

        XSPReply reply = getRemotePlatformFor(currentUser.getIdentifier()).getPlatformUsers();
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
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();

        XSPReply reply = getRemotePlatformFor(currentUser.getIdentifier()).getPlatformUser(identifier);
        if (!reply.isSuccess())
            return null;
        PlatformUser result = ((XSPReplyResult<PlatformUser>) reply).getData();
        return getUser(result.getIdentifier(), result.getName());
    }

    @Override
    public XSPReply createUser(String identifier, String name, String key) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getRemotePlatformFor(currentUser.getIdentifier()).createPlatformUser(identifier, name, key);
    }

    @Override
    public XSPReply renameUser(String identifier, String name) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getRemotePlatformFor(currentUser.getIdentifier()).renamePlatformUser(identifier, name);
    }

    @Override
    public XSPReply deleteUser(String identifier) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getRemotePlatformFor(currentUser.getIdentifier()).deletePlatformUser(identifier);
    }

    @Override
    public XSPReply changeUserKey(String identifier, String oldKey, String newKey) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getRemotePlatformFor(currentUser.getIdentifier()).changePlatformUserPassword(identifier, oldKey, newKey);
    }

    @Override
    public XSPReply resetUserKey(String identifier, String newKey) {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return null;
        PlatformUser currentUser = securityService.getCurrentUser();
        return getRemotePlatformFor(currentUser.getIdentifier()).resetPlatformUserPassword(identifier, newKey);
    }

    /**
     * Gets the remote platform access for a user
     *
     * @param userId The identifier of the user
     * @return The remote platform access
     */
    private RemotePlatformAccess getRemotePlatformFor(String userId) {
        synchronized (connections) {
            RemotePlatformAccess connection = connections.get(userId);
            if (connection == null) {
                connection = new RemotePlatformAccess(masterEndpoint, deserializer);
                connections.put(userId, connection);
            }
            return connection;
        }
    }
}
