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
import org.xowl.infra.store.AbstractRepository;
import org.xowl.infra.store.Repository;
import org.xowl.infra.store.rdf.Node;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultYesNo;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.security.Realm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
     * The path to the resources
     */
    private static final String RESOURCES = "/org/xowl/platform/services/security/internal/";
    /**
     * The graph for security entities
     */
    private static final String GRAPH = "http://xowl.org/platform/services/security#";

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
     * The stored procedures
     */
    private final Map<String, XOWLStoredProcedure> procedures;

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
        this.procedures = new HashMap<>();
        initializeDatabase();
    }

    /**
     * Initializes the database when empty
     */
    private void initializeDatabase() {
        // (re-)upload the rules
        XSPReply reply = database.upload(readResource("rules.rdft"), AbstractRepository.SYNTAX_RDFT);
        if (!reply.isSuccess()) {
            Logging.getDefault().error("Failed to initialize the security database");
            return;
        }

        // check for stored procedures
        reply = database.getStoredProcedures();
        if (!reply.isSuccess()) {
            Logging.getDefault().error("Failed to initialize the security database");
            return;
        }

        if (!((XSPReplyResultCollection<?>) reply).getData().isEmpty()) {
            // we already have the stored procedures, retrieve them
            getProcedure("procedure-add-admin");
            getProcedure("procedure-add-member");
            getProcedure("procedure-assign-role");
            getProcedure("procedure-check-role");
            getProcedure("procedure-create-group");
            getProcedure("procedure-create-role");
            getProcedure("procedure-create-user");
            getProcedure("procedure-get-groups");
            getProcedure("procedure-get-roles");
            getProcedure("procedure-get-users");
            getProcedure("procedure-imply-role");
        } else {
            // deploy the procedures
            deployProcedure("procedure-add-admin", "group", "admin");
            deployProcedure("procedure-add-member", "group", "user");
            deployProcedure("procedure-assign-role", "entity", "role");
            deployProcedure("procedure-check-role", "user", "role");
            deployProcedure("procedure-create-group", "group", "name", "admin");
            deployProcedure("procedure-create-role", "role", "name");
            deployProcedure("procedure-create-user", "user", "name");
            deployProcedure("procedure-get-groups");
            deployProcedure("procedure-get-roles");
            deployProcedure("procedure-get-users");
            deployProcedure("procedure-imply-role", "source", "target");
        }
    }

    /**
     * Retrieves a stored procedure
     *
     * @param name The name of the procedure
     */
    private void getProcedure(String name) {
        XOWLStoredProcedure procedure = ((XSPReplyResult<XOWLStoredProcedure>) database.getStoreProcedure(name)).getData();
        procedures.put(name, procedure);
    }

    /**
     * Deploy a stored procedure
     *
     * @param name       The name of the procedure
     * @param parameters The parameters for the procedure
     */
    private void deployProcedure(String name, String... parameters) {
        String content = readResource(name + ".sparql");
        XSPReply reply = database.addStoredProcedure(name, content, Arrays.asList(parameters));
        if (!reply.isSuccess()) {
            Logging.getDefault().error("Failed to deploy the procedure " + name);
            return;
        }
        XOWLStoredProcedure procedure = ((XSPReplyResult<XOWLStoredProcedure>) reply).getData();
        procedures.put(name, procedure);
    }

    /**
     * Reads a resource
     *
     * @param resource The resource to read
     * @return The content of the resource
     */
    private static String readResource(String resource) {
        InputStream stream = Repository.class.getResourceAsStream(RESOURCES + resource);
        try {
            return Files.read(stream, Files.CHARSET);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return null;
        }
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
        parameters.put("user", nodes.getIRINode(GRAPH + userId));
        parameters.put("role", nodes.getIRINode(roleId));
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-check-role"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
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
