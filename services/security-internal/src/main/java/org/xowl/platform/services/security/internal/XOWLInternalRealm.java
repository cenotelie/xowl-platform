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

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.xowl.infra.server.ServerConfiguration;
import org.xowl.infra.server.api.XOWLDatabase;
import org.xowl.infra.server.api.XOWLServer;
import org.xowl.infra.server.api.XOWLStoredProcedure;
import org.xowl.infra.server.api.XOWLUser;
import org.xowl.infra.server.base.BaseStoredProcedureContext;
import org.xowl.infra.server.embedded.EmbeddedServer;
import org.xowl.infra.server.xsp.*;
import org.xowl.infra.store.Repository;
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
import org.xowl.infra.utils.IOUtils;
import org.xowl.infra.utils.config.Section;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.*;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.platform.*;
import org.xowl.platform.kernel.security.SecurityRealm;
import org.xowl.platform.kernel.security.SecurityService;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * An authentication realm that uses a xOWL server as a user base
 *
 * @author Laurent Wouters
 */
class XOWLInternalRealm implements SecurityRealm, ManagedService {
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
    protected final Map<String, XOWLInternalUser> cacheUsers;
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
     *
     * @param configuration The configuration for the realm
     */
    public XOWLInternalRealm(Section configuration) {
        XOWLServer server = null;
        XOWLDatabase database = null;
        try {
            String location = (PlatformUtils.resolve(configuration.get("location"))).getAbsolutePath();
            ServerConfiguration serverConfiguration = new ServerConfiguration(location);
            server = new EmbeddedServer(Logging.get(), serverConfiguration);
            database = ((ReplyResult<XOWLDatabase>) server.getDatabase(serverConfiguration.getAdminDBName())).getData();
        } catch (Exception exception) {
            Logging.get().error(exception);
        }
        this.nodes = new CachedNodes();
        this.server = server;
        this.database = database;
        this.procedures = new HashMap<>();
        this.cacheUsers = new HashMap<>();
        this.cacheGroups = new HashMap<>();
        this.cacheRoles = new HashMap<>();
        initializeDatabase();
        BundleContext context = FrameworkUtil.getBundle(XOWLInternalRealm.class).getBundleContext();
        context.registerService(Service.class, this, null);
        context.registerService(ManagedService.class, this, null);
    }

    /**
     * Initializes the database when empty
     */
    private void initializeDatabase() {
        // (re-)upload the rules
        Reply reply = database.upload(Repository.SYNTAX_XRDF, readResource("rules.xrdf"));
        if (!reply.isSuccess()) {
            Logging.get().error("Failed to initialize the security database");
            return;
        }

        // check for stored procedures
        reply = database.getStoredProcedures();
        if (!reply.isSuccess()) {
            Logging.get().error("Failed to initialize the security database");
            return;
        }

        if (!((ReplyResultCollection<?>) reply).getData().isEmpty()) {
            // we already have the stored procedures, retrieve them
            getProcedure("procedure-add-admin");
            getProcedure("procedure-add-member");
            getProcedure("procedure-remove-admin");
            getProcedure("procedure-remove-member");
            getProcedure("procedure-assign-role");
            getProcedure("procedure-unassign-role");
            getProcedure("procedure-check-role");
            getProcedure("procedure-create-group");
            getProcedure("procedure-create-role");
            getProcedure("procedure-create-user");
            getProcedure("procedure-delete-entity");
            getProcedure("procedure-rename-entity");
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
            deployProcedure("procedure-remove-admin", "group", "admin");
            deployProcedure("procedure-remove-member", "group", "user");
            deployProcedure("procedure-assign-role", "entity", "role");
            deployProcedure("procedure-unassign-role", "entity", "role");
            deployProcedure("procedure-check-role", "user", "role");
            deployProcedure("procedure-create-group", "group", "name", "admin");
            deployProcedure("procedure-create-role", "role", "name");
            deployProcedure("procedure-create-user", "user", "name");
            deployProcedure("procedure-delete-entity", "entity");
            deployProcedure("procedure-rename-entity", "entity", "newName");
            deployProcedure("procedure-get-groups");
            deployProcedure("procedure-get-roles");
            deployProcedure("procedure-get-users");
            deployProcedure("procedure-get-entity-name", "entity");
            deployProcedure("procedure-get-entity-roles", "entity");
            deployProcedure("procedure-get-group-admins", "group");
            deployProcedure("procedure-get-group-members", "group");
            deployProcedure("procedure-imply-role", "source", "target");
            // deploy root user
            Map<String, Node> parameters = new HashMap<>();
            parameters.put("user", nodes.getIRINode(USERS + PlatformUserRoot.INSTANCE.getIdentifier()));
            parameters.put("name", nodes.getLiteralNode(PlatformUserRoot.INSTANCE.getName(), Vocabulary.xsdString, null));
            reply = database.executeStoredProcedure(procedures.get("procedure-create-user"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                Logging.get().error(reply);
            // deploy admin user
            parameters = new HashMap<>();
            parameters.put("user", nodes.getIRINode(USERS + PlatformUtils.DEFAULT_ADMIN_LOGIN));
            parameters.put("name", nodes.getLiteralNode("Administrator", Vocabulary.xsdString, null));
            reply = database.executeStoredProcedure(procedures.get("procedure-create-user"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                Logging.get().error(reply);
            // deploy admin group
            parameters = new HashMap<>();
            parameters.put("group", nodes.getIRINode(GROUPS + PlatformUtils.DEFAULT_ADMIN_GROUP));
            parameters.put("name", nodes.getLiteralNode("Administrators", Vocabulary.xsdString, null));
            parameters.put("admin", nodes.getIRINode(USERS + PlatformUserRoot.INSTANCE.getIdentifier()));
            reply = database.executeStoredProcedure(procedures.get("procedure-create-group"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                Logging.get().error(reply);
            // deploy admin role
            parameters = new HashMap<>();
            parameters.put("role", nodes.getIRINode(ROLES + PlatformRoleAdmin.INSTANCE.getIdentifier()));
            parameters.put("name", nodes.getLiteralNode(PlatformRoleAdmin.INSTANCE.getName(), Vocabulary.xsdString, null));
            reply = database.executeStoredProcedure(procedures.get("procedure-create-role"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                Logging.get().error(reply);
            // assign platform admin role to admin group
            parameters = new HashMap<>();
            parameters.put("entity", nodes.getIRINode(GROUPS + PlatformUtils.DEFAULT_ADMIN_GROUP));
            parameters.put("role", nodes.getIRINode(ROLES + PlatformRoleAdmin.INSTANCE.getIdentifier()));
            reply = database.executeStoredProcedure(procedures.get("procedure-assign-role"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                Logging.get().error(reply);
            // add user admin as administrator of group admin
            parameters = new HashMap<>();
            parameters.put("group", nodes.getIRINode(GROUPS + PlatformUtils.DEFAULT_ADMIN_GROUP));
            parameters.put("admin", nodes.getIRINode(USERS + PlatformUtils.DEFAULT_ADMIN_LOGIN));
            reply = database.executeStoredProcedure(procedures.get("procedure-add-admin"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                Logging.get().error(reply);
        }
    }

    /**
     * Retrieves a stored procedure
     *
     * @param name The name of the procedure
     */
    private void getProcedure(String name) {
        XOWLStoredProcedure procedure = ((ReplyResult<XOWLStoredProcedure>) database.getStoreProcedure(name)).getData();
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
        Reply reply = database.addStoredProcedure(name, content, Arrays.asList(parameters));
        if (!reply.isSuccess()) {
            Logging.get().error("Failed to deploy the procedure " + name);
            return;
        }
        XOWLStoredProcedure procedure = ((ReplyResult<XOWLStoredProcedure>) reply).getData();
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
            return IOUtils.read(stream, IOUtils.CHARSET);
        } catch (IOException exception) {
            Logging.get().error(exception);
            return null;
        }
    }

    @Override
    public String getIdentifier() {
        return XOWLInternalRealm.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - xOWL Internal Realm";
    }

    @Override
    public int getLifecycleTier() {
        return TIER_IO;
    }

    @Override
    public void onLifecycleStart() {
        // do nothing
    }

    @Override
    public void onLifecycleStop() {
        try {
            server.close();
        } catch (IOException exception) {
            Logging.get().error(exception);
        }
    }

    @Override
    public PlatformUser authenticate(String login, String password) {
        Reply reply = server.login(login, password);
        if (!reply.isSuccess())
            return null;
        return getUser(login);
    }

    @Override
    public boolean checkHasRole(String userId, String roleId) {
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("user", nodes.getIRINode(USERS + userId));
        parameters.put("role", nodes.getIRINode(ROLES + roleId));
        Reply reply = database.executeStoredProcedure(procedures.get("procedure-check-role"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return false;
        Result result = ((ReplyResult<Result>) reply).getData();
        return reply.isSuccess() && ((ResultYesNo) result).getValue();
    }

    @Override
    public Collection<PlatformUser> getUsers() {
        Map<String, Node> parameters = new HashMap<>();
        Reply reply = database.executeStoredProcedure(procedures.get("procedure-get-users"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return Collections.emptyList();
        ResultSolutions result = ((ReplyResult<ResultSolutions>) reply).getData();
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
        Reply reply = database.executeStoredProcedure(procedures.get("procedure-get-groups"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return Collections.emptyList();
        ResultSolutions result = ((ReplyResult<ResultSolutions>) reply).getData();
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
        Reply reply = database.executeStoredProcedure(procedures.get("procedure-get-roles"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return Collections.emptyList();
        ResultSolutions result = ((ReplyResult<ResultSolutions>) reply).getData();
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
        if (name == null)
            return null;
        return getUser(identifier, name);
    }

    @Override
    public PlatformGroup getGroup(String identifier) {
        PlatformGroup group = cacheGroups.get(identifier);
        if (group != null)
            return group;
        String name = getEntityName(GROUPS + identifier);
        if (name == null)
            return null;
        return getGroup(identifier, name);
    }

    @Override
    public PlatformRole getRole(String identifier) {
        PlatformRole role = cacheRoles.get(identifier);
        if (role != null)
            return role;
        String name = getEntityName(ROLES + identifier);
        if (name == null)
            return null;
        return getRole(identifier, name);
    }

    @Override
    public Reply createUser(String identifier, String name, String key) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_CREATE_USER);
        if (!reply.isSuccess())
            return reply;
        // check identifier format
        if (!identifier.matches("[_a-zA-Z0-9]+"))
            return new ReplyApiError(ERROR_INVALID_IDENTIFIER, "[_a-zA-Z0-9]+");
        // create the user as an embedded server user
        reply = server.createUser(identifier, key);
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
        XOWLInternalUser user = getUser(identifier, name);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new PlatformUserCreatedEvent(user, securityService));
        return new ReplyResult<>(user);
    }

    @Override
    public Reply createGroup(String identifier, String name, String adminId) {
        /// check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_CREATE_GROUP);
        if (!reply.isSuccess())
            return reply;
        // check identifier format
        if (!identifier.matches("[_a-zA-Z0-9]+"))
            return new ReplyApiError(ERROR_INVALID_IDENTIFIER, "[_a-zA-Z0-9]+");
        if (getUser(adminId) == null)
            return new ReplyApiError(ERROR_INVALID_USER, adminId);
        // create the group data
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("group", nodes.getIRINode(GROUPS + identifier));
        parameters.put("name", nodes.getLiteralNode(name, Vocabulary.xsdString, null));
        parameters.put("admin", nodes.getIRINode(USERS + adminId));
        reply = database.executeStoredProcedure(procedures.get("procedure-create-group"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        XOWLInternalGroup group = getGroup(identifier, name);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new PlatformGroupCreatedEvent(group, securityService));
        return new ReplyResult<>(group);
    }

    @Override
    public Reply createRole(String identifier, String name) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_CREATE_ROLE);
        if (!reply.isSuccess())
            return reply;
        // check identifier format
        if (!identifier.matches("[_a-zA-Z0-9]+"))
            return new ReplyApiError(ERROR_INVALID_IDENTIFIER, "[_a-zA-Z0-9]+");
        // create the group data
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("role", nodes.getIRINode(ROLES + identifier));
        parameters.put("name", nodes.getLiteralNode(name, Vocabulary.xsdString, null));
        reply = database.executeStoredProcedure(procedures.get("procedure-create-role"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        PlatformRoleBase role = getRole(identifier, name);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new PlatformRoleCreatedEvent(role, securityService));
        return new ReplyResult<>(role);
    }

    @Override
    public Reply renameUser(String identifier, String name) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        PlatformUser userObject = getUser(identifier);
        if (userObject == null)
            return new ReplyApiError(ERROR_INVALID_USER, identifier);
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_RENAME_USER, userObject);
        if (!reply.isSuccess())
            return reply;
        synchronized (cacheUsers) {
            Map<String, Node> parameters = new HashMap<>();
            parameters.put("entity", nodes.getIRINode(USERS + identifier));
            parameters.put("newName", nodes.getLiteralNode(name, Vocabulary.xsdString, null));
            reply = database.executeStoredProcedure(procedures.get("procedure-rename-entity"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                return reply;
            cacheUsers.remove(identifier);
            return ReplySuccess.instance();
        }
    }

    @Override
    public Reply renameGroup(String identifier, String name) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        PlatformGroup groupObject = getGroup(identifier);
        if (groupObject == null)
            return new ReplyApiError(ERROR_INVALID_GROUP, identifier);
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_RENAME_GROUP, groupObject);
        if (!reply.isSuccess())
            return reply;
        synchronized (cacheGroups) {
            // rename the entity
            Map<String, Node> parameters = new HashMap<>();
            parameters.put("entity", nodes.getIRINode(GROUPS + identifier));
            parameters.put("newName", nodes.getLiteralNode(name, Vocabulary.xsdString, null));
            reply = database.executeStoredProcedure(procedures.get("procedure-rename-entity"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                return reply;
            cacheGroups.remove(identifier);
            return ReplySuccess.instance();
        }
    }

    @Override
    public Reply renameRole(String identifier, String name) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        PlatformRole roleObject = getRole(identifier);
        if (roleObject == null)
            return new ReplyApiError(ERROR_INVALID_ROLE, identifier);
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_RENAME_ROLE, roleObject);
        if (!reply.isSuccess())
            return reply;
        synchronized (cacheRoles) {
            // rename the entity
            Map<String, Node> parameters = new HashMap<>();
            parameters.put("entity", nodes.getIRINode(ROLES + identifier));
            parameters.put("newName", nodes.getLiteralNode(name, Vocabulary.xsdString, null));
            reply = database.executeStoredProcedure(procedures.get("procedure-rename-entity"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                return reply;
            cacheRoles.remove(identifier);
            return ReplySuccess.instance();
        }
    }

    @Override
    public Reply deleteUser(String identifier) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        PlatformUser userObject = getUser(identifier);
        if (userObject == null)
            return new ReplyApiError(ERROR_INVALID_USER, identifier);
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_DELETE_USER, userObject);
        if (!reply.isSuccess())
            return reply;
        // check for entity that cannot be deleted
        if (PlatformUserRoot.INSTANCE.getIdentifier().equals(identifier))
            return new ReplyApiError(ERROR_CANNOT_DELETE_ENTITY, identifier);
        // delete the server user
        synchronized (cacheUsers) {
            reply = server.getUser(identifier);
            if (!reply.isSuccess())
                return reply;
            reply = server.deleteUser(((ReplyResult<XOWLUser>) reply).getData());
            if (!reply.isSuccess())
                return reply;
            // delete the entity
            Map<String, Node> parameters = new HashMap<>();
            parameters.put("entity", nodes.getIRINode(USERS + identifier));
            reply = database.executeStoredProcedure(procedures.get("procedure-delete-entity"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                return reply;
            XOWLInternalUser deleted = cacheUsers.remove(identifier);
            EventService eventService = Register.getComponent(EventService.class);
            if (eventService != null)
                eventService.onEvent(new PlatformUserDeletedEvent(deleted, securityService));
            return ReplySuccess.instance();
        }
    }

    @Override
    public Reply deleteGroup(String identifier) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        PlatformGroup groupObject = getGroup(identifier);
        if (groupObject == null)
            return new ReplyApiError(ERROR_INVALID_GROUP, identifier);
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_DELETE_GROUP, groupObject);
        if (!reply.isSuccess())
            return reply;
        // delete the entity
        synchronized (cacheGroups) {
            Map<String, Node> parameters = new HashMap<>();
            parameters.put("entity", nodes.getIRINode(GROUPS + identifier));
            reply = database.executeStoredProcedure(procedures.get("procedure-delete-entity"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                return reply;
            XOWLInternalGroup deleted = cacheGroups.remove(identifier);
            EventService eventService = Register.getComponent(EventService.class);
            if (eventService != null)
                eventService.onEvent(new PlatformGroupDeletedEvent(deleted, securityService));
            return ReplySuccess.instance();
        }
    }

    @Override
    public Reply deleteRole(String identifier) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        PlatformRole roleObject = getRole(identifier);
        if (roleObject == null)
            return new ReplyApiError(ERROR_INVALID_ROLE, identifier);
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_DELETE_ROLE, roleObject);
        if (!reply.isSuccess())
            return reply;
        // check for entity that cannot be deleted
        if (PlatformRoleAdmin.INSTANCE.getIdentifier().equals(identifier))
            return new ReplyApiError(ERROR_CANNOT_DELETE_ENTITY, identifier);
        // delete the entity
        synchronized (cacheRoles) {
            Map<String, Node> parameters = new HashMap<>();
            parameters.put("entity", nodes.getIRINode(ROLES + identifier));
            reply = database.executeStoredProcedure(procedures.get("procedure-delete-entity"),
                    new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
            if (!reply.isSuccess())
                return reply;
            PlatformRoleBase deleted = cacheRoles.remove(identifier);
            EventService eventService = Register.getComponent(EventService.class);
            if (eventService != null)
                eventService.onEvent(new PlatformRoleDeletedEvent(deleted, securityService));
            return ReplySuccess.instance();
        }
    }

    @Override
    public Reply changeUserKey(String identifier, String oldKey, String newKey) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        PlatformUser platformUser = authenticate(identifier, oldKey);
        if (platformUser == null)
            return new ReplyApiError(ERROR_INVALID_USER, identifier);
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_UPDATE_USER_KEY, platformUser);
        if (!reply.isSuccess())
            return reply;
        // execute
        reply = server.getUser(identifier);
        if (!reply.isSuccess())
            return reply;
        XOWLUser xowlUser = ((ReplyResult<XOWLUser>) reply).getData();
        return xowlUser.updatePassword(newKey);
    }

    @Override
    public Reply resetUserKey(String identifier, String newKey) {
        // check authorization
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        PlatformUser platformUser = getUser(identifier);
        if (platformUser == null)
            return new ReplyApiError(ERROR_INVALID_USER, identifier);
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_RESET_USER_KEY, platformUser);
        if (!reply.isSuccess())
            return reply;
        // execute
        reply = server.getUser(identifier);
        if (!reply.isSuccess())
            return reply;
        XOWLUser xowlUser = ((ReplyResult<XOWLUser>) reply).getData();
        return xowlUser.updatePassword(newKey);
    }

    @Override
    public Reply addUserToGroup(String user, String group) {
        // check input data
        PlatformGroup groupObject = getGroup(group);
        if (groupObject == null)
            return new ReplyApiError(ERROR_INVALID_GROUP, group);
        PlatformUser newUser = getUser(user);
        if (newUser == null)
            return new ReplyApiError(ERROR_INVALID_USER, user);
        // check the current user is either the platform admin or the group admin
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_ADD_USER_TO_GROUP, groupObject);
        if (!reply.isSuccess())
            return reply;
        // execute
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("group", nodes.getIRINode(GROUPS + group));
        parameters.put("user", nodes.getIRINode(USERS + user));
        reply = database.executeStoredProcedure(procedures.get("procedure-add-member"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return ReplySuccess.instance();
    }

    @Override
    public Reply addAdminToGroup(String user, String group) {
        // check input data
        PlatformGroup groupObject = getGroup(group);
        if (groupObject == null)
            return new ReplyApiError(ERROR_INVALID_GROUP, group);
        PlatformUser newUser = getUser(user);
        if (newUser == null)
            return new ReplyApiError(ERROR_INVALID_USER, user);
        // check the current user is either the platform admin or the group admin
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_ADD_ADMIN_TO_GROUP, groupObject);
        if (!reply.isSuccess())
            return reply;
        // execute
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("group", nodes.getIRINode(GROUPS + group));
        parameters.put("admin", nodes.getIRINode(USERS + user));
        reply = database.executeStoredProcedure(procedures.get("procedure-add-admin"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return ReplySuccess.instance();
    }

    @Override
    public Reply removeUserFromGroup(String user, String group) {
        // check input data
        PlatformGroup groupObject = getGroup(group);
        if (groupObject == null)
            return new ReplyApiError(ERROR_INVALID_GROUP, group);
        PlatformUser newUser = getUser(user);
        if (newUser == null)
            return new ReplyApiError(ERROR_INVALID_USER, user);
        // check the current user is either the platform admin or the group admin
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_REMOVE_USER_FROM_GROUP, groupObject);
        if (!reply.isSuccess())
            return reply;
        // execute
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("group", nodes.getIRINode(GROUPS + group));
        parameters.put("user", nodes.getIRINode(USERS + user));
        reply = database.executeStoredProcedure(procedures.get("procedure-remove-member"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return ReplySuccess.instance();
    }

    @Override
    public Reply removeAdminFromGroup(String user, String group) {
        // check input data
        PlatformGroup groupObject = getGroup(group);
        if (groupObject == null)
            return new ReplyApiError(ERROR_INVALID_GROUP, group);
        PlatformUser newUser = getUser(user);
        if (newUser == null)
            return new ReplyApiError(ERROR_INVALID_USER, user);
        // check the current user is either the platform admin or the group admin
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_REMOVE_ADMIN_FROM_GROUP, groupObject);
        if (!reply.isSuccess())
            return reply;
        // execute
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("group", nodes.getIRINode(GROUPS + group));
        parameters.put("admin", nodes.getIRINode(USERS + user));
        reply = database.executeStoredProcedure(procedures.get("procedure-remove-admin"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return ReplySuccess.instance();
    }

    @Override
    public Reply assignRoleToUser(String user, String role) {
        // check input data
        PlatformUser userObj = getUser(user);
        if (userObj == null)
            return new ReplyApiError(ERROR_INVALID_USER, user);
        PlatformRole roleObj = getRole(role);
        if (roleObj == null)
            return new ReplyApiError(ERROR_INVALID_ROLE, role);
        // check for current user with admin role
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_ASSIGN_ROLE_TO_USER);
        if (!reply.isSuccess())
            return reply;
        // execute
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("entity", nodes.getIRINode(USERS + user));
        parameters.put("role", nodes.getIRINode(ROLES + role));
        reply = database.executeStoredProcedure(procedures.get("procedure-assign-role"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return ReplySuccess.instance();
    }

    @Override
    public Reply assignRoleToGroup(String group, String role) {
        // check input data
        PlatformGroup groupObj = getGroup(group);
        if (groupObj == null)
            return new ReplyApiError(ERROR_INVALID_GROUP, group);
        PlatformRole roleObj = getRole(role);
        if (roleObj == null)
            return new ReplyApiError(ERROR_INVALID_ROLE, role);
        // check for current user with admin role
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_ASSIGN_ROLE_TO_GROUP);
        if (!reply.isSuccess())
            return reply;
        // execute
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("entity", nodes.getIRINode(GROUPS + group));
        parameters.put("role", nodes.getIRINode(ROLES + role));
        reply = database.executeStoredProcedure(procedures.get("procedure-assign-role"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return ReplySuccess.instance();
    }

    @Override
    public Reply unassignRoleToUser(String user, String role) {
        // check input data
        PlatformUser userObj = getUser(user);
        if (userObj == null)
            return new ReplyApiError(ERROR_INVALID_USER, user);
        PlatformRole roleObj = getRole(role);
        if (roleObj == null)
            return new ReplyApiError(ERROR_INVALID_ROLE, role);
        // check for current user with admin role
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_UNASSIGN_ROLE_TO_USER);
        if (!reply.isSuccess())
            return reply;
        // execute
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("entity", nodes.getIRINode(USERS + user));
        parameters.put("role", nodes.getIRINode(ROLES + role));
        reply = database.executeStoredProcedure(procedures.get("procedure-unassign-role"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return ReplySuccess.instance();
    }

    @Override
    public Reply unassignRoleToGroup(String group, String role) {
        // check input data
        PlatformGroup groupObj = getGroup(group);
        if (groupObj == null)
            return new ReplyApiError(ERROR_INVALID_GROUP, group);
        PlatformRole roleObj = getRole(role);
        if (roleObj == null)
            return new ReplyApiError(ERROR_INVALID_ROLE, role);
        // check for current user with admin role
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        Reply reply = securityService.getPolicy().checkAction(securityService, SecurityService.ACTION_UNASSIGN_ROLE_TO_GROUP);
        if (!reply.isSuccess())
            return reply;
        // execute
        Map<String, Node> parameters = new HashMap<>();
        parameters.put("entity", nodes.getIRINode(GROUPS + group));
        parameters.put("role", nodes.getIRINode(ROLES + role));
        reply = database.executeStoredProcedure(procedures.get("procedure-unassign-role"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return reply;
        return ReplySuccess.instance();
    }

    @Override
    public Reply addRoleImplication(String sourceRole, String targetRole) {
        return ReplyUnsupported.instance();
    }

    @Override
    public Reply removeRoleImplication(String sourceRole, String targetRole) {
        return ReplyUnsupported.instance();
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
        Reply reply = database.executeStoredProcedure(procedures.get("procedure-get-entity-name"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return null;
        ResultSolutions result = ((ReplyResult<ResultSolutions>) reply).getData();
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
        Reply reply = database.executeStoredProcedure(procedures.get("procedure-get-entity-roles"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return null;
        ResultSolutions result = ((ReplyResult<ResultSolutions>) reply).getData();
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
        Reply reply = database.executeStoredProcedure(procedures.get("procedure-get-group-admins"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return null;
        ResultSolutions result = ((ReplyResult<ResultSolutions>) reply).getData();
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
        Reply reply = database.executeStoredProcedure(procedures.get("procedure-get-group-members"),
                new BaseStoredProcedureContext(Collections.<String>emptyList(), Collections.<String>emptyList(), parameters));
        if (!reply.isSuccess())
            return null;
        ResultSolutions result = ((ReplyResult<ResultSolutions>) reply).getData();
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
    protected XOWLInternalUser getUser(String identifier, String name) {
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
    protected XOWLInternalGroup getGroup(String identifier, String name) {
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
    protected PlatformRoleBase getRole(String identifier, String name) {
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
