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
import org.xowl.infra.server.api.base.BaseStoredProcedureContext;
import org.xowl.infra.server.base.ServerConfiguration;
import org.xowl.infra.server.embedded.EmbeddedServer;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.AbstractRepository;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.LiteralNode;
import org.xowl.infra.store.rdf.Node;
import org.xowl.infra.store.rdf.RDFPatternSolution;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultSolutions;
import org.xowl.infra.store.sparql.ResultYesNo;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ConfigurationService;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.*;
import org.xowl.platform.kernel.security.Realm;
import org.xowl.platform.kernel.security.SecurityService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
     * The IRI prefix for users
     */
    public static final String USERS = "http://xowl.org/server/users#";
    /**
     * The IRI prefix for groups
     */
    public static final String GROUPS = "http://xowl.org/platform/security/groups#";
    /**
     * The IRI prefix for roles
     */
    public static final String ROLES = "http://xowl.org/platform/security/roles#";

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
     * The cache of users
     */
    private final Map<String, XOWLInternalUser> cacheUsers;
    /**
     * The cache of groups
     */
    private final Map<String, XOWLInternalGroup> cacheGroups;
    /**
     * The cache of roles
     */
    private final Map<String, PlatformRoleBase> cacheRoles;

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
        this.cacheUsers = new HashMap<>();
        this.cacheGroups = new HashMap<>();
        this.cacheRoles = new HashMap<>();
        initializeDatabase();
    }

    /**
     * Initializes the database when empty
     */
    private void initializeDatabase() {
        // (re-)upload the rules
        XSPReply reply = database.upload(AbstractRepository.SYNTAX_RDFT, readResource("rules.rdft"));
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
            getProcedure("procedure-get-entity-name");
            getProcedure("procedure-get-entity-roles");
            getProcedure("procedure-get-group-admins");
            getProcedure("procedure-get-group-members");
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
            deployProcedure("procedure-get-entity-name", "entity");
            deployProcedure("procedure-get-entity-roles", "entity");
            deployProcedure("procedure-get-group-admins", "group");
            deployProcedure("procedure-get-group-members", "group");
            deployProcedure("procedure-imply-role", "source", "target");
            // deploy admin user
            Map<String, Node> parameters = new HashMap<>();
            parameters.put("user", nodes.getIRINode(USERS + "admin"));
            parameters.put("name", nodes.getLiteralNode("Administrator", Vocabulary.xsdString, null));
            database.executeStoredProcedure(procedures.get("procedure-create-user"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            // deploy root user
            parameters = new HashMap<>();
            parameters.put("user", nodes.getIRINode(USERS + PlatformUserRoot.INSTANCE.getIdentifier()));
            parameters.put("name", nodes.getLiteralNode(PlatformUserRoot.INSTANCE.getName(), Vocabulary.xsdString, null));
            database.executeStoredProcedure(procedures.get("procedure-create-user"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            // deploy admin group
            parameters = new HashMap<>();
            parameters.put("group", nodes.getIRINode(GROUPS + "admin"));
            parameters.put("name", nodes.getLiteralNode("Administrators", Vocabulary.xsdString, null));
            parameters.put("admin", nodes.getIRINode(USERS + PlatformUserRoot.INSTANCE.getIdentifier()));
            database.executeStoredProcedure(procedures.get("procedure-create-group"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            // deploy admin role
            parameters = new HashMap<>();
            parameters.put("role", nodes.getIRINode(ROLES + PlatformRoleAdmin.INSTANCE.getIdentifier()));
            parameters.put("name", nodes.getLiteralNode(PlatformRoleAdmin.INSTANCE.getName(), Vocabulary.xsdString, null));
            database.executeStoredProcedure(procedures.get("procedure-create-role"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            // assign platform admin role to admin group
            parameters = new HashMap<>();
            parameters.put("entity", nodes.getIRINode(GROUPS + "admin"));
            parameters.put("role", nodes.getIRINode(ROLES + PlatformRoleAdmin.INSTANCE.getIdentifier()));
            database.executeStoredProcedure(procedures.get("procedure-assign-role"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            // add user admin as administrator of group admin
            parameters = new HashMap<>();
            parameters.put("group", nodes.getIRINode(GROUPS + "admin"));
            parameters.put("admin", nodes.getIRINode(USERS + "admin"));
            database.executeStoredProcedure(procedures.get("procedure-add-admin"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
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
        InputStream stream = XOWLInternalRealm.class.getResourceAsStream(RESOURCES + resource);
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
    public PlatformUser authenticate(String userId, char[] key) {
        XSPReply reply = server.login(userId, new String(key));
        if (!reply.isSuccess())
            return null;
        return getUser(userId);
    }

    @Override
    public void onRequestEnd(String userId) {
        // do nothing
    }

    @Override
    public boolean checkHasRole(String userId, String roleId) {
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("user", nodes.getIRINode(USERS + userId));
        parameters.put("role", nodes.getIRINode(ROLES + roleId));
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-check-role"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return false;
        Result result = ((XSPReplyResult<Result>) reply).getData();
        return reply.isSuccess() && ((ResultYesNo) result).getValue();
    }

    @Override
    public Collection<PlatformUser> getUsers() {
        Map<String, Node> parameters = new HashMap<>();
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-get-users"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return Collections.emptyList();
        ResultSolutions result = ((XSPReplyResult<ResultSolutions>) reply).getData();
        Collection<PlatformUser> users = new ArrayList<>(result.getSolutions().size());
        for (RDFPatternSolution solution : result.getSolutions()) {
            String id = ((IRINode) solution.get("user")).getIRIValue().substring(USERS.length());
            String name = ((LiteralNode) solution.get("name")).getLexicalValue();
            users.add(getUser(id, name));
        }
        return users;
    }

    @Override
    public Collection<PlatformGroup> getGroups() {
        Map<String, Node> parameters = new HashMap<>();
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-get-groups"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return Collections.emptyList();
        ResultSolutions result = ((XSPReplyResult<ResultSolutions>) reply).getData();
        Collection<PlatformGroup> groups = new ArrayList<>(result.getSolutions().size());
        for (RDFPatternSolution solution : result.getSolutions()) {
            String id = ((IRINode) solution.get("group")).getIRIValue().substring(GROUPS.length());
            String name = ((LiteralNode) solution.get("name")).getLexicalValue();
            groups.add(getGroup(id, name));
        }
        return groups;
    }

    @Override
    public Collection<PlatformRole> getRoles() {
        Map<String, Node> parameters = new HashMap<>();
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-get-roles"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return Collections.emptyList();
        ResultSolutions result = ((XSPReplyResult<ResultSolutions>) reply).getData();
        Collection<PlatformRole> roles = new ArrayList<>(result.getSolutions().size());
        for (RDFPatternSolution solution : result.getSolutions()) {
            String id = ((IRINode) solution.get("role")).getIRIValue().substring(ROLES.length());
            String name = ((LiteralNode) solution.get("name")).getLexicalValue();
            roles.add(getRole(id, name));
        }
        return roles;
    }

    @Override
    public PlatformUser getUser(String identifier) {
        PlatformUser user = cacheUsers.get(identifier);
        if (user != null)
            return user;
        String name = getEntityName(USERS + identifier);
        return getUser(identifier, name);
    }

    @Override
    public PlatformGroup getGroup(String identifier) {
        PlatformGroup group = cacheGroups.get(identifier);
        if (group != null)
            return group;
        String name = getEntityName(GROUPS + identifier);
        return getGroup(identifier, name);
    }

    @Override
    public PlatformRole getRole(String identifier) {
        PlatformRole role = cacheRoles.get(identifier);
        if (role != null)
            return role;
        String name = getEntityName(ROLES + identifier);
        return getRole(identifier, name);
    }

    @Override
    public XSPReply createUser(String identifier, String name, String key) {
        // check for current user with admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        PlatformUser currentUser = securityService.getCurrentUser();
        if (currentUser == null)
            return XSPReplyUnauthenticated.instance();
        if (!checkHasRole(currentUser.getIdentifier(), PlatformRoleAdmin.INSTANCE.getIdentifier()))
            return XSPReplyUnauthorized.instance();
        // check identifier format
        if (!identifier.matches("[_a-zA-Z0-9]+"))
            return new XSPReplyFailure("Identifier does not meet requirements ([_a-zA-Z0-9]+)");
        // create the user as an embedded server user
        XSPReply reply = server.createUser(identifier, key);
        if (!reply.isSuccess())
            return reply;
        // create the user data
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("user", nodes.getIRINode(USERS + identifier));
        parameters.put("name", nodes.getLiteralNode(name, Vocabulary.xsdString, null));
        reply = database.executeStoredProcedure(procedures.get("procedure-create-user"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(getUser(identifier, name));
    }

    @Override
    public XSPReply createGroup(String identifier, String name) {
        // check for current user with admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        PlatformUser currentUser = securityService.getCurrentUser();
        if (currentUser == null)
            return XSPReplyUnauthenticated.instance();
        if (!checkHasRole(currentUser.getIdentifier(), PlatformRoleAdmin.INSTANCE.getIdentifier()))
            return XSPReplyUnauthorized.instance();
        // check identifier format
        if (!identifier.matches("[_a-zA-Z0-9]+"))
            return new XSPReplyFailure("Identifier does not meet requirements ([_a-zA-Z0-9]+)");
        // create the group data
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("group", nodes.getIRINode(USERS + identifier));
        parameters.put("name", nodes.getLiteralNode(name, Vocabulary.xsdString, null));
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-create-group"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(getGroup(identifier, name));
    }

    @Override
    public XSPReply createRole(String identifier, String name) {
        // check for current user with admin role
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyServiceUnavailable.instance();
        PlatformUser currentUser = securityService.getCurrentUser();
        if (currentUser == null)
            return XSPReplyUnauthenticated.instance();
        if (!checkHasRole(currentUser.getIdentifier(), PlatformRoleAdmin.INSTANCE.getIdentifier()))
            return XSPReplyUnauthorized.instance();
        // check identifier format
        if (!identifier.matches("[_a-zA-Z0-9]+"))
            return new XSPReplyFailure("Identifier does not meet requirements ([_a-zA-Z0-9]+)");
        // create the group data
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("role", nodes.getIRINode(USERS + identifier));
        parameters.put("name", nodes.getLiteralNode(name, Vocabulary.xsdString, null));
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-create-role"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return new XSPReplyResult<>(getRole(identifier, name));
    }

    @Override
    public XSPReply renameUser(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply renameGroup(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply renameRole(String identifier, String name) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply deleteUser(String identifier) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply deleteGroup(String identifier) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply deleteRole(String identifier) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply changeUserKey(String identifier, String oldKey, String newKey) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply resetUserKey(String identifier, String newKey) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply addUserToGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply addAdminToGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply removeUserFromGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply removeAdminFromGroup(String user, String group) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply assignRoleToUser(String user, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply assignRoleToGroup(String group, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply unassignRoleToUser(String user, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply unassignRoleToGroup(String group, String role) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply addRoleImplication(String sourceRole, String targetRole) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply removeRoleImplication(String sourceRole, String targetRole) {
        return XSPReplyUnsupported.instance();
    }

    /**
     * When the platform is stopping
     */
    public void onStop() {
        server.onShutdown();
    }


    /**
     * Gets the name of an entity
     *
     * @param entity The IRI of an entity
     * @return The entity's name
     */
    private String getEntityName(String entity) {
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("entity", nodes.getIRINode(entity));
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-get-entity-name"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return null;
        ResultSolutions result = ((XSPReplyResult<ResultSolutions>) reply).getData();
        if (result.getSolutions().size() == 0)
            return null;
        RDFPatternSolution solution = result.getSolutions().iterator().next();
        return ((LiteralNode) solution.get("name")).getLexicalValue();
    }

    /**
     * Gets the roles for an entity
     *
     * @param entity The IRI of an entity
     * @return The roles
     */
    protected Collection<PlatformRole> getEntityRoles(String entity) {
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("entity", nodes.getIRINode(entity));
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-get-entity-roles"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return null;
        ResultSolutions result = ((XSPReplyResult<ResultSolutions>) reply).getData();
        Collection<PlatformRole> roles = new ArrayList<>(result.getSolutions().size());
        for (RDFPatternSolution solution : result.getSolutions()) {
            String roleId = ((IRINode) solution.get("role")).getIRIValue().substring(ROLES.length());
            String roleName = ((LiteralNode) solution.get("roleName")).getLexicalValue();
            roles.add(getRole(roleId, roleName));
        }
        return roles;
    }

    /**
     * Gets the administrators of a group
     *
     * @param groupIRI The IRI of a group
     * @return The administrators
     */
    protected Collection<PlatformUser> getGroupAdmins(String groupIRI) {
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("group", nodes.getIRINode(groupIRI));
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-get-group-admins"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return null;
        ResultSolutions result = ((XSPReplyResult<ResultSolutions>) reply).getData();
        Collection<PlatformUser> users = new ArrayList<>(result.getSolutions().size());
        for (RDFPatternSolution solution : result.getSolutions()) {
            String userId = ((IRINode) solution.get("user")).getIRIValue().substring(USERS.length());
            String userName = ((LiteralNode) solution.get("name")).getLexicalValue();
            users.add(getUser(userId, userName));
        }
        return users;
    }

    /**
     * Gets the members of a group
     *
     * @param groupIRI The IRI of a group
     * @return The members
     */
    protected Collection<PlatformUser> getGroupMembers(String groupIRI) {
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("group", nodes.getIRINode(groupIRI));
        XSPReply reply = database.executeStoredProcedure(procedures.get("procedure-get-group-members"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return null;
        ResultSolutions result = ((XSPReplyResult<ResultSolutions>) reply).getData();
        Collection<PlatformUser> users = new ArrayList<>(result.getSolutions().size());
        for (RDFPatternSolution solution : result.getSolutions()) {
            String userId = ((IRINode) solution.get("user")).getIRIValue().substring(USERS.length());
            String userName = ((LiteralNode) solution.get("name")).getLexicalValue();
            users.add(getUser(userId, userName));
        }
        return users;
    }

    /**
     * Resolves a user from the cache
     *
     * @param identifier The identifier of the user
     * @param name       The expected user's name
     * @return The user
     */
    protected PlatformUser getUser(String identifier, String name) {
        synchronized (cacheUsers) {
            XOWLInternalUser user = cacheUsers.get(identifier);
            if (user != null)
                return user;
            user = new XOWLInternalUser(this, identifier, name);
            cacheUsers.put(identifier, user);
            return user;
        }
    }

    /**
     * Resolves a group from the cache
     *
     * @param identifier The identifier of the group
     * @param name       The expected group's name
     * @return The group
     */
    protected PlatformGroup getGroup(String identifier, String name) {
        synchronized (cacheGroups) {
            XOWLInternalGroup group = cacheGroups.get(identifier);
            if (group != null)
                return group;
            group = new XOWLInternalGroup(this, identifier, name);
            cacheGroups.put(identifier, group);
            return group;
        }
    }

    /**
     * Resolves a role from the cache
     *
     * @param identifier The identifier of the role
     * @param name       The expected role's name
     * @return The role
     */
    protected PlatformRole getRole(String identifier, String name) {
        synchronized (cacheRoles) {
            PlatformRoleBase role = cacheRoles.get(identifier);
            if (role != null)
                return role;
            role = new PlatformRoleBase(identifier, name);
            cacheRoles.put(identifier, role);
            return role;
        }
    }
}
