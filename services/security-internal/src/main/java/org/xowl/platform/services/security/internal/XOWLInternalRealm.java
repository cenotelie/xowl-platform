/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
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

import org.xowl.infra.server.api.XOWLDatabase;
import org.xowl.infra.server.api.XOWLServer;
import org.xowl.infra.server.api.XOWLStoredProcedure;
import org.xowl.infra.server.api.XOWLUser;
import org.xowl.infra.server.api.base.BaseStoredProcedureContext;
import org.xowl.infra.server.base.ServerConfiguration;
import org.xowl.infra.server.embedded.EmbeddedServer;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.store.rdf.Node;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultYesNo;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.platform.PlatformUserRoleAdmin;
import org.xowl.platform.kernel.security.Realm;
import org.xowl.platform.kernel.security.SecurityService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An authentication realm that uses a xOWL server as a user base
 *
 * @author Laurent Wouters
 */
class XOWLInternalRealm implements Realm {
    /**
     * The graph for the user data
     */
    private static final String GRAPH = "http://xowl.org/platform/services/security/internal";
    /**
     * The prefix for a user name
     */
    private static final String SCHEMA_USER_PREFIX = "http://xowl.org/server/users#";
    /**
     * The name property of an entity
     */
    private static final String SCHEMA_NAME = "http://xowl.org/platform/services/security/internal#hasName";
    /**
     * The has role relation between a user/group and a role
     */
    private static final String SCHEMA_HASROLE = "http://xowl.org/platform/services/security/internal#hasRole";
    /**
     * The in group relation between a user and a group
     */
    private static final String SCHEMA_INGROUP = "http://xowl.org/platform/services/security/internal#inGroup";

    /**
     * The stored procedure for adding a role
     */
    private static final String PROCEDURE_ADD_ROLE = "http://xowl.org/platform/services/security/internal#addRole";
    /**
     * The stored procedure for removing a role
     */
    private static final String PROCEDURE_REMOVE_ROLE = "http://xowl.org/platform/services/security/internal#removeRole";
    /**
     * The stored procedure to check for a role
     */
    private static final String PROCEDURE_CHECK_ROLE = "http://xowl.org/platform/services/security/internal#checkRole";

    /**
     * A node manager for constant URIs
     */
    private final NodeManager nodes;
    /**
     * The embedded server for this realm
     */
    private final XOWLServer server;
    /**
     * The user database
     */
    private final XOWLDatabase database;
    /**
     * The procedure to add a role
     */
    private final XOWLStoredProcedure procedureAddRole;
    /**
     * The procedure to remove a role
     */
    private final XOWLStoredProcedure procedureRemoveRole;
    /**
     * The procedure to check for a role
     */
    private final XOWLStoredProcedure procedureCheckRole;

    /**
     * Initialize this realm
     */
    public XOWLInternalRealm() {
        XOWLServer server = null;
        XOWLDatabase database = null;
        try {
            ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
            Configuration configuration = configurationService.getConfigFor(this);
            String location = (new File(System.getenv(Env.ROOT), configuration.get("location"))).getAbsolutePath();
            ServerConfiguration serverConfiguration = new ServerConfiguration(location);
            server = new EmbeddedServer(serverConfiguration);
            database = ((XSPReplyResult<XOWLDatabase>) server.getDatabase(serverConfiguration.getAdminDBName())).getData();
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
        }
        this.nodes = new CachedNodes();
        this.server = server;
        this.database = database;
        initializeDatabase();
        this.procedureAddRole = ((XSPReplyResult<XOWLStoredProcedure>) database.getStoreProcedure(PROCEDURE_ADD_ROLE)).getData();
        this.procedureRemoveRole = ((XSPReplyResult<XOWLStoredProcedure>) database.getStoreProcedure(PROCEDURE_REMOVE_ROLE)).getData();
        this.procedureCheckRole = ((XSPReplyResult<XOWLStoredProcedure>) database.getStoreProcedure(PROCEDURE_CHECK_ROLE)).getData();
    }

    /**
     * Initializes the database when empty
     */
    private void initializeDatabase() {
        XSPReply reply = database.getStoredProcedures();
        if (!reply.isSuccess() || !((XSPReplyResultCollection<?>) reply).getData().isEmpty())
            return;
        database.sparql("INSERT DATA { GRAPH <" + GRAPH + "> { <" + SCHEMA_USER_PREFIX + "admin> <" + SCHEMA_HASROLE + "> <" + PlatformUserRoleAdmin.INSTANCE.getIdentifier() + "> } }", null, null);
        database.addStoredProcedure(PROCEDURE_ADD_ROLE, "INSERT DATA { GRAPH <" + GRAPH + "> { ?user <" + SCHEMA_HASROLE + "> ?role } }", Arrays.asList("user", "role"));
        database.addStoredProcedure(PROCEDURE_REMOVE_ROLE, "DELETE DATA { GRAPH <" + GRAPH + "> { ?user <" + SCHEMA_HASROLE + "> ?role } }", Arrays.asList("user", "role"));
        database.addStoredProcedure(PROCEDURE_CHECK_ROLE, "ASK { GRAPH ?g { ?user <" + SCHEMA_HASROLE + "> ?role } }", Arrays.asList("user", "role"));
    }

    @Override
    public String getIdentifier() {
        return XOWLInternalRealm.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Internal Realm";
    }

    @Override
    public XSPReply authenticate(String userId, char[] key) {
        XSPReply reply = server.login(userId, new String(key));
        if (!reply.isSuccess())
            return XSPReplyFailure.instance();
        return new XSPReplyResult<>(new XOWLInternalUser(((XSPReplyResult<XOWLUser>) reply).getData().getName()));
    }

    @Override
    public void onRequestEnd(String userId) {
        // do nothing
    }

    @Override
    public boolean checkHasRole(String userId, String roleId) {
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("user", nodes.getIRINode(SCHEMA_USER_PREFIX + userId));
        parameters.put("role", nodes.getIRINode(roleId));
        XSPReply reply = database.executeStoredProcedure(procedureCheckRole, new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return false;
        Result result = ((XSPReplyResult<Result>) reply).getData();
        return reply.isSuccess() && ((ResultYesNo) result).getValue();
    }

    /**
     * When the platform is stopping
     */
    public void onStop() {
        server.onShutdown();
    }
}
