// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

/*****************************************************
 * xOWL Collaboration Platform API - V1
 ****************************************************/

function XOWL() {
	var url = document.location.href;
	var index = url.indexOf("/web");
	this.endpoint = url.substring(0, index) + "/api/";
	this.userLogin = localStorage.getItem("xowl.user.login");
	this.userIdentifier = localStorage.getItem("xowl.user.identifier");
	this.userName = localStorage.getItem("xowl.user.name");
}

/**
 * MIME type for plain text
 */
var MIME_PLAIN_TEXT = "text/plain";
/**
 * MIME type for JSON
 */
var MIME_JSON = "application/json";
/**
 * MIME type for SPARQL
 */
var MIME_SPARQL = "application/sparql-query";
/**
 * MIME type for octet stream
 */
var MIME_OCTET_STREAM = "binary/octet-stream";


/*****************************************************
 * Kernel - Security Service
 ****************************************************/

XOWL.prototype.isLoggedIn = function () {
	return (this.userIdentifier !== null);
}

XOWL.prototype.getLoggedInUserId = function () {
	return this.userIdentifier;
}

XOWL.prototype.getLoggedInUserName = function () {
	return this.userName;
}

XOWL.prototype.login = function (callback, login, password) {
	var _self = this;
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			var user = JSON.parse(content);
			_self.userLogin = login;
			_self.userIdentifier = user.identifier;
			_self.userName = user.name;
			localStorage.setItem("xowl.user.login", login);
			localStorage.setItem("xowl.user.identifier", user.identifier);
			localStorage.setItem("xowl.user.name", user.name);
			callback(code, type, content);
		} else {
			_self.userLogin = null;
			_self.userIdentifier = null;
			_self.userName = null;
			localStorage.removeItem("xowl.user.login");
			localStorage.removeItem("xowl.user.identifier");
			localStorage.removeItem("xowl.user.name");
			callback(code, type, content);
		}
	}, "kernel/security/login", {login: login}, "POST", MIME_PLAIN_TEXT, password);
}

XOWL.prototype.logout = function () {
	var _self = this;
	this.doRequest(function (code, type, content) {
		_self.userLogin = null;
		_self.userIdentifier = null;
		_self.userName = null;
		localStorage.removeItem("xowl.user.login");
		localStorage.removeItem("xowl.user.identifier");
		localStorage.removeItem("xowl.user.name");
	}, "kernel/security/logout", null, "POST", null, null);
}

XOWL.prototype.getSecurityPolicy = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/policy", null, "GET", null, null);
}

XOWL.prototype.setSecuredActionPolicy = function (callback, actionId, policy) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/policy/actions/" + encodeURIComponent(actionId), null, "PUT", MIME_JSON, policy);
}

XOWL.prototype.getPlatformUsers = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users", null, "GET", null, null);
}

XOWL.prototype.getPlatformUser = function (callback, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId), null, "GET", null, null);
}

XOWL.prototype.createPlatformUser = function (callback, userId, name, password) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId), {name: name}, "PUT", MIME_PLAIN_TEXT, password);
}

XOWL.prototype.deletePlatformUser = function (callback, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId), null, "DELETE", null, null);
}

XOWL.prototype.renamePlatformUser = function (callback, userId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId) + "/rename", {name: name}, "POST", null, null);
}

XOWL.prototype.changePlatformUserPassword = function (callback, userId, oldPassword, newPassword) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId) + "/updateKey", {oldKey: oldPassword}, "POST", MIME_PLAIN_TEXT, newPassword);
}

XOWL.prototype.resetPlatformUserPassword = function (callback, userId, newPassword) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId) + "/resetKey", null, "POST", MIME_PLAIN_TEXT, newPassword);
}

XOWL.prototype.getPlatformGroups = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups", null, "GET", null, null);
}

XOWL.prototype.getPlatformGroup = function (callback, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId), null, "GET", null, null);
}

XOWL.prototype.createPlatformGroup = function (callback, groupId, name, admin) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId), {name: name, admin: admin}, "PUT", null, null);
}

XOWL.prototype.deletePlatformGroup = function (callback, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId), null, "DELETE", null, null);
}

XOWL.prototype.renamePlatformGroup = function (callback, groupId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/rename", {name: name}, "POST", null, null);
}

XOWL.prototype.addMemberToPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/addMember", {user: userId}, "POST", null, null);
}

XOWL.prototype.removeMemberFromPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/removeMember", {user: userId}, "POST", null, null);
}

XOWL.prototype.addAdminToPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/addAdmin", {user: userId}, "POST", null, null);
}

XOWL.prototype.removeAdminFromPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/removeAdmin", {user: userId}, "POST", null, null);
}

XOWL.prototype.getPlatformRoles = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles", null, "GET", null, null);
}

XOWL.prototype.getPlatformRole = function (callback, roleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles/" + encodeURIComponent(roleId), null, "GET", null, null);
}

XOWL.prototype.createPlatformRole = function (callback, roleId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles/" + encodeURIComponent(roleId), {name: name}, "PUT", null, null);
}

XOWL.prototype.deletePlatformRole = function (callback, roleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles/" + encodeURIComponent(roleId), null, "DELETE", null, null);
}

XOWL.prototype.renamePlatformRole = function (callback, roleId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles/" + encodeURIComponent(roleId) + "/rename", {name: name}, "POST", null, null);
}

XOWL.prototype.assignRoleToPlatformUser = function (callback, roleId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId) + "/assign", {role: roleId}, "POST", null, null);
}

XOWL.prototype.unassignRoleFromPlatformUser = function (callback, roleId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId) + "/unassign", {role: roleId}, "POST", null, null);
}

XOWL.prototype.assignRoleToPlatformGroup = function (callback, roleId, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/assign", {role: roleId}, "POST", null, null);
}

XOWL.prototype.unassignRoleFromPlatformGroup = function (callback, roleId, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/unassign", {role: roleId}, "POST", null, null);
}

XOWL.prototype.addPlatformRoleImplication = function (callback, roleId, impliedRoleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles/" + encodeURIComponent(roleId) + "/imply", {target: impliedRoleId}, "POST", null, null);
}

XOWL.prototype.removePlatformRoleImplication = function (callback, roleId, impliedRoleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles/" + encodeURIComponent(roleId) + "/unimply", {target: impliedRoleId}, "POST", null, null);
}



/*****************************************************
 * Kernel - API Discovery Service
 ****************************************************/

XOWL.prototype.getApiResources = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/discovery/resources", null, "GET", null, null);
}

XOWL.prototype.getApiServices = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/discovery/services", null, "GET", null, null);
}



/*****************************************************
 * Kernel - Platform Management Service
 ****************************************************/

XOWL.prototype.getPlatformProduct = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/product", null, "GET", null, null);
}

XOWL.prototype.getPlatformBundles = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/bundles", null, "GET", null, null);
}

XOWL.prototype.getPlatformAddons = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/addons", null, "GET", null, null);
}

XOWL.prototype.getPlatformAddon = function (callback, addonId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/addons/" + encodeURIComponent(addonId), null, "GET", null, null);
}

XOWL.prototype.installPlatformAddon = function (callback, addonId, package) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/addons/" + encodeURIComponent(addonId), null, "PUT", MIME_OCTET_STREAM, package);
}

XOWL.prototype.uninstallPlatformAddon = function (callback, addonId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/addons/" + encodeURIComponent(addonId), null, "DELETE", null, null);
}

XOWL.prototype.platformShutdown = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, content);
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/shutdown", null, "POST", null, null);
}

XOWL.prototype.platformRestart = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, content);
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/restart", null, "POST", null, null);
}



/*****************************************************
 * Kernel - Logging Service
 ****************************************************/

XOWL.prototype.getLogMessages = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/log", null, "GET", null, null);
}



/*****************************************************
 * Kernel - Jobs Management Service
 ****************************************************/

XOWL.prototype.getJobs = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/jobs", null, "GET", null, null);
}

XOWL.prototype.getJob = function (callback, jobId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/jobs/" + encodeURIComponent(jobId), null, "GET", null, null);
}

XOWL.prototype.cancelJob = function (callback, jobId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "kernel/jobs/" + encodeURIComponent(jobId) + "/cancel", null, "POST", null, null);
}



/*****************************************************
 * Kernel - Statistics Service
 ****************************************************/

XOWL.prototype.getAllMetrics = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/statistics/metrics", null, "GET", null, null);
}

XOWL.prototype.getMetric = function (callback, metricId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/statistics/metrics/" + encodeURIComponent(metricId), null, "GET", null, null);
}

XOWL.prototype.getMetricSnapshot = function (callback, metricId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/statistics/metrics/" + encodeURIComponent(metricId) + "/snapshot", null, "GET", null, null);
}



/*****************************************************
 * Kernel - Business Directory Service
 ****************************************************/

XOWL.prototype.getArtifactArchetypes = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/business/archetypes", null, "GET", null, null);
}

XOWL.prototype.getArtifactArchetype = function (callback, archetypeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/business/archetypes/" + encodeURIComponent(archetypeId), null, "GET", null, null);
}

XOWL.prototype.getArtifactSchemas = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/business/schemas", null, "GET", null, null);
}

XOWL.prototype.getArtifactSchema = function (callback, schemaId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/business/schemas/" + encodeURIComponent(schemaId), null, "GET", null, null);
}



/*****************************************************
 * Webapp - Web Modules Directory Service
 ****************************************************/

XOWL.prototype.getWebModules = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/webapp/modules", null, "GET", null, null);
}



/*****************************************************
 * Collaboration - Collaboration Service
 ****************************************************/

XOWL.prototype.archiveCollaboration = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/archive", null, "POST", null, null);
}

XOWL.prototype.deleteCollaboration = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/delete", null, "POST", null, null);
}

XOWL.prototype.getCollaborationManifest = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest", null, "GET", null, null);
}

XOWL.prototype.getCollaborationInputSpecifications = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/inputs", null, "GET", null, null);
}

XOWL.prototype.addCollaborationInputSpecification = function (callback, specification) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/inputs", null, "PUT", MIME_JSON, specification);
}

XOWL.prototype.removeCollaborationInputSpecification = function (callback, specificationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/inputs/" + encodeURIComponent(specificationId), null, "DELETE", null, null);
}

XOWL.prototype.getArtifactsForCollaborationInput = function (callback, specificationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/inputs/" + encodeURIComponent(specificationId) + "/artifacts", null, "GET", null, null);
}

XOWL.prototype.registerArtifactForCollaborationInput = function (callback, specificationId, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/inputs/" + encodeURIComponent(specificationId) + "/artifacts/" + encodeURIComponent(artifactId), null, "PUT", null, null);
}

XOWL.prototype.unregisterArtifactForCollaborationInput = function (callback, specificationId, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/inputs/" + encodeURIComponent(specificationId) + "/artifacts/" + encodeURIComponent(artifactId), null, "DELETE", null, null);
}

XOWL.prototype.getCollaborationOutputSpecifications = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/outputs", null, "GET", null, null);
}

XOWL.prototype.addCollaborationOutputSpecification = function (callback, specification) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/outputs", null, "PUT", MIME_JSON, specification);
}

XOWL.prototype.removeCollaborationOutputSpecification = function (callback, specificationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/outputs/" + encodeURIComponent(specificationId), null, "DELETE", null, null);
}

XOWL.prototype.getArtifactsForCollaborationOutput = function (callback, specificationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/outputs/" + encodeURIComponent(specificationId) + "/artifacts", null, "GET", null, null);
}

XOWL.prototype.registerArtifactForCollaborationOutput = function (callback, specificationId, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/outputs/" + encodeURIComponent(specificationId) + "/artifacts/" + encodeURIComponent(artifactId), null, "PUT", null, null);
}

XOWL.prototype.unregisterArtifactForCollaborationOutput = function (callback, specificationId, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/outputs/" + encodeURIComponent(specificationId) + "/artifacts/" + encodeURIComponent(artifactId), null, "DELETE", null, null);
}

XOWL.prototype.getCollaborationRoles = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/roles", null, "GET", null, null);
}

XOWL.prototype.createCollaborationRole = function (callback, role) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/roles", null, "PUT", MIME_JSON, role);
}

XOWL.prototype.addCollaborationRole = function (callback, roleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/roles/" + encodeURIComponent(roleId), null, "PUT", null, null);
}

XOWL.prototype.removeCollaborationRole = function (callback, roleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/roles/" + encodeURIComponent(roleId), null, "DELETE", null, null);
}

XOWL.prototype.getCollaborationPattern = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/manifest/pattern", null, "GET", null, null);
}

XOWL.prototype.getKnownIOSpecifications = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/specifications", null, "GET", null, null);
}

XOWL.prototype.getKnownPatterns = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/patterns", null, "GET", null, null);
}

XOWL.prototype.getCollaborationNeighbours = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours", null, "GET", null, null);
}

XOWL.prototype.spawnCollaboration = function (callback, specification) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours", null, "PUT", MIME_JSON, specification);
}

XOWL.prototype.getCollaborationNeighbour = function (callback, neighbourId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours/" + encodeURIComponent(neighbourId), null, "GET", null, null);
}

XOWL.prototype.getCollaborationNeighbourManifest = function (callback, neighbourId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours/" + encodeURIComponent(neighbourId) + "/manifest", null, "GET", null, null);
}

XOWL.prototype.getCollaborationNeighbourStatus = function (callback, neighbourId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours/" + encodeURIComponent(neighbourId) + "/status", null, "GET", null, null);
}

XOWL.prototype.deleteCollaborationNeighbour = function (callback, neighbourId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, null);
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours/" + encodeURIComponent(neighbourId), null, "DELETE", null, null);
}

XOWL.prototype.archiveCollaborationNeighbour = function (callback, neighbourId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours/" + encodeURIComponent(neighbourId) + "/archive", null, "POST", null, null);
}

XOWL.prototype.restartCollaborationNeighbour = function (callback, neighbourId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours/" + encodeURIComponent(neighbourId) + "/restart", null, "POST", null, null);
}

XOWL.prototype.getCollaborationNeighbourInputs = function (callback, neighbourId, specificationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours/" + encodeURIComponent(neighbourId) + "/manifest/inputs/" + encodeURIComponent(specificationId) + "/artifacts", null, "GET", null, null);
}

XOWL.prototype.getCollaborationNeighbourOutputs = function (callback, neighbourId, specificationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/collaboration/neighbours/" + encodeURIComponent(neighbourId) + "/manifest/outputs/" + encodeURIComponent(specificationId) + "/artifacts", null, "GET", null, null);
}



/*****************************************************
 * Community - Bots Management Service
 ****************************************************/

XOWL.prototype.getBots = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/community/bots", null, "GET", null, null);
}

XOWL.prototype.getBot = function (callback, botId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/community/bots/" + encodeURIComponent(botId), null, "GET", null, null);
}

XOWL.prototype.getBotMessages = function (callback, botId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/community/bots/" + encodeURIComponent(botId) + "/messages", null, "GET", null, null);
}

XOWL.prototype.wakeupBot = function (callback, botId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/community/bots/" + encodeURIComponent(botId) + "/wakeup", null, "POST", null, null);
}

XOWL.prototype.putBotToSleep = function (callback, botId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/community/bots/" + encodeURIComponent(botId) + "/putToSleep", null, "POST", null, null);
}



/*****************************************************
 * Community - Profile Service
 ****************************************************/

XOWL.prototype.getPublicProfile = function (callback, profileId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/community/profiles/" + encodeURIComponent(profileId) + "/public", null, "GET", null, null);
}

XOWL.prototype.updatePublicProfile = function (callback, profile) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/community/profiles/" + encodeURIComponent(profile.identifier) + "/public", null, "PUT", MIME_JSON, profile);
}

XOWL.prototype.getBadges = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/community/badges", null, "GET", null, null);
}

XOWL.prototype.getBadge = function (callback, badgeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/community/badges/" + encodeURIComponent(badgeId), null, "GET", null, null);
}

XOWL.prototype.awardBadge = function (callback, profileId, badgeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/community/profiles/" + encodeURIComponent(profileId) + "/public/badges/" + encodeURIComponent(badgeId), null, "PUT", null, null);
}

XOWL.prototype.rescindBadge = function (callback, profileId, badgeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/community/profiles/" + encodeURIComponent(profileId) + "/public/badges/" + encodeURIComponent(badgeId), null, "DELETE", null, null);
}



/*****************************************************
 * Connection - Connection Service
 ****************************************************/

XOWL.prototype.getDescriptors = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/connection/descriptors", null, "GET", null, null);
}

XOWL.prototype.getConnectors = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/connection/connectors", null, "GET", null, null);
}

XOWL.prototype.getConnector = function (callback, connectorId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/connection/connectors/" + encodeURIComponent(connectorId), null, "GET", null, null);
}

XOWL.prototype.createConnector = function (callback, descriptor, definition) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/connection/connectors/" + encodeURIComponent(definition.identifier), {descriptor: descriptor.identifier}, "PUT", MIME_JSON, definition);
}

XOWL.prototype.deleteConnector = function (callback, connectorId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/connection/connectors/" + encodeURIComponent(connectorId), null, "DELETE", null, null);
}

XOWL.prototype.pullFromConnector = function (callback, connectorId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/connection/connectors/" + encodeURIComponent(connectorId) + "/pull", null, "POST", null, null);
}

XOWL.prototype.pushToConnector = function (callback, connectorId, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/connection/connectors/" + encodeURIComponent(connectorId) + "/push", {artifact: artifactId}, "POST", null, null);
}



/*****************************************************
 * Storage - Storage Service
 ****************************************************/

XOWL.prototype.sparql = function (callback, payload) {
	this.doRequest(function (code, type, content) {
		callback(code, type, content);
	}, "services/storage/sparql", null, "POST", "application/sparql-query", payload);
}

XOWL.prototype.sparqlOn = function (callback, payload, store) {
	this.doRequest(function (code, type, content) {
		callback(code, type, content);
	}, "services/storage/sparql", {store: store}, "POST", "application/sparql-query", payload);
}

XOWL.prototype.getAllArtifacts = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts", null, "GET", null, null);
}

XOWL.prototype.getLiveArtifacts = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts/live", null, "GET", null, null);
}

XOWL.prototype.getArtifactsForBase = function (callback, base) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts", {base: base}, "GET", null, null);
}

XOWL.prototype.getArtifactsForArchetype = function (callback, archetype) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts", {archetype: archetype}, "GET", null, null);
}

XOWL.prototype.getArtifact = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts/" + encodeURIComponent(artifactId), null, "GET", null, null);
}

XOWL.prototype.getArtifactMetadata = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts/" + encodeURIComponent(artifactId) + "/metadata", null, "GET", null, null);
}

XOWL.prototype.getArtifactContent = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts/" + encodeURIComponent(artifactId) + "/content", null, "GET", null, null);
}

XOWL.prototype.deleteArtifact = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts/" + encodeURIComponent(artifactId), null, "DELETE", null, null);
}

XOWL.prototype.diffArtifacts = function (callback, artifactLeft, artifactRight) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			var leftIndex = content.indexOf("--xowl_boundary");
			var rightIndex = content.lastIndexOf("--xowl_boundary");
			var contentLeft = content.substring(leftIndex + "--xowl_boundary".length + 1 + "Content-Type: ".length + MIME_JSON.length, rightIndex);
			var contentRight = content.substring(rightIndex + "--xowl_boundary".length + 1 + "Content-Type: ".length + MIME_JSON.length);
			callback(code, MIME_JSON, {
				added: JSON.parse(contentLeft),
				removed: JSON.parse(contentRight)
			});
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts/diff", {left: artifactLeft, right: artifactRight}, "POST", null, null);
}

XOWL.prototype.pullArtifactFromLive = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts/" + encodeURIComponent(artifactId) + "/deactivate", null, "POST", null, null);
}

XOWL.prototype.pushArtifactToLive = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts/" + encodeURIComponent(artifactId) + "/activate", null, "POST", null, null);
}



/*****************************************************
 * Importation - Importation Service
 ****************************************************/

XOWL.prototype.getDocumentImporters = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/importers", null, "GET", null, null);
}

XOWL.prototype.getDocumentImporter = function (callback, importerId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/importers/" + encodeURIComponent(importerId), null, "GET", null, null);
}

XOWL.prototype.getImporterConfigurationsFor = function (callback, importerId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/importers/" + encodeURIComponent(importerId) + "/configurations", null, "GET", null, null);
}

XOWL.prototype.getImporterConfigurations = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/configurations", null, "GET", null, null);
}

XOWL.prototype.getImporterConfiguration = function (callback, configurationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/configurations/" + encodeURIComponent(configurationId), null, "GET", null, null);
}

XOWL.prototype.deleteImporterConfiguration = function (callback, configurationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/importation/configurations/" + encodeURIComponent(configurationId), null, "DELETE", null, null);
}

XOWL.prototype.storeImporterConfiguration = function (callback, configuration) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/importation/configurations", null, "PUT", MIME_JSON, configuration);
}

XOWL.prototype.getUploadedDocuments = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/documents", null, "GET", null, null);
}

XOWL.prototype.getUploadedDocument = function (callback, documentId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/documents/" + encodeURIComponent(documentId), null, "GET", null, null);
}

XOWL.prototype.dropUploadedDocument = function (callback, documentId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/importation/documents/" + encodeURIComponent(documentId), null, "DELETE", null, null);
}

XOWL.prototype.uploadDocument = function (callback, name, content, fileName) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/documents", {name: name, fileName: fileName}, "PUT", MIME_OCTET_STREAM, content);
}

XOWL.prototype.getUploadedDocumentPreviewWith = function (callback, documentId, configurationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/documents/" + encodeURIComponent(documentId) + "/preview", {configuration: configurationId}, "POST", null, null);
}

XOWL.prototype.getUploadedDocumentPreview = function (callback, documentId, configuration) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/documents/" + encodeURIComponent(documentId) + "/preview", null, "POST", MIME_JSON, configuration);
}

XOWL.prototype.importUploadedDocumentWith = function (callback, documentId, configurationId, metadata) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/documents/" + encodeURIComponent(documentId) + "/import", {
		configuration: configurationId,
		name: metadata.name,
		base: metadata.base,
		version: metadata.version,
		archetype: metadata.archetype,
		superseded: metadata.superseded
	}, "POST", null, null);
}

XOWL.prototype.importUploadedDocument = function (callback, documentId, configuration, metadata) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/importation/documents/" + encodeURIComponent(documentId) + "/import", {
		name: metadata.name,
		base: metadata.base,
		version: metadata.version,
		archetype: metadata.archetype,
		superseded: metadata.superseded
	}, "POST", MIME_JSON, configuration);
}



/*****************************************************
 * Consistency - Consistency Service
 ****************************************************/

XOWL.prototype.getInconsistencies = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/inconsistencies", null, "GET", null, null);
}


XOWL.prototype.getReasoningRules = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/rules", null, "GET", null, null);
}

XOWL.prototype.getReasoningRule = function (callback, ruleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/rules/" + encodeURIComponent(ruleId), null, "GET", null, null);
}

XOWL.prototype.newReasoningRule = function (callback, name, definition) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/rules", {
		name: name
	}, "PUT", "application/x-xowl-rdft", definition);
}

XOWL.prototype.addReasoningRule = function (callback, rule) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/rules/" + encodeURIComponent(rule.identifier), null, "PUT", MIME_JSON, rule);
}

XOWL.prototype.activateReasoningRule = function (callback, ruleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/rules/" + encodeURIComponent(ruleId) + "/activate", null, "POST", null, null);
}

XOWL.prototype.deactivateReasoningRule = function (callback, ruleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/rules/" + encodeURIComponent(ruleId) + "/deactivate", null, "POST", null, null);
}

XOWL.prototype.deleteReasoningRule = function (callback, ruleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/rules/" + encodeURIComponent(ruleId), null, "DELETE", null, null);
}

XOWL.prototype.getConsistencyConstraints = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/constraints", null, "GET", null, null);
}

XOWL.prototype.getConsistencyConstraint = function (callback, constraintId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/constraints/" + encodeURIComponent(constraintId), null, "GET", null, null);
}

XOWL.prototype.newConsistencyConstraint = function (callback, name, message, prefixes, conditions) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/constraints", {
		name: name,
		message: message,
		prefixes: prefixes
	}, "PUT", "application/x-xowl-rdft", conditions);
}

XOWL.prototype.addConsistencyConstraint = function (callback, constraint) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/constraints/" + encodeURIComponent(constraint.identifier), null, "PUT", MIME_JSON, constraint);
}

XOWL.prototype.activateConsistencyConstraint = function (callback, constraintId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/constraints/" + encodeURIComponent(constraintId) + "/activate", null, "POST", null, null);
}

XOWL.prototype.deactivateConsistencyConstraint = function (callback, constraintId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/constraints/" + encodeURIComponent(constraintId) + "/deactivate", null, "POST", null, null);
}

XOWL.prototype.deleteConsistencyConstraint = function (callback, constraintId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/consistency/constraints/" + encodeURIComponent(constraintId), null, "DELETE", null, null);
}



/*****************************************************
 * Impact - Impact Analysis Service
 ****************************************************/

XOWL.prototype.newImpactAnalysis = function (callback, definition) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/impact", null, "POST", MIME_JSON, definition);
}



/*****************************************************
 * Evaluation - Evaluation Service
 ****************************************************/

XOWL.prototype.getEvaluations = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/evaluation/evaluations", null, "GET", null, null);
}

XOWL.prototype.getEvaluation = function (callback, evaluationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/evaluation/evaluations/" + encodeURIComponent(evaluationId), null, "GET", null, null);
}

XOWL.prototype.getEvaluableTypes = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/evaluation/evaluableTypes", null, "GET", null, null);
}

XOWL.prototype.getEvaluables = function (callback, typeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/evaluation/evaluables", {type: typeId}, "GET", null, null);
}

XOWL.prototype.getEvaluationCriteria = function (callback, typeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/evaluation/criterionTypes", {"for": typeId}, "GET", null, null);
}

XOWL.prototype.newEvaluation = function (callback, definition) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/evaluation/evaluations", null, "PUT", MIME_JSON, definition);
}



/*****************************************************
 * Marketplace - Marketplace service
 ****************************************************/

XOWL.prototype.marketplaceLookupAddons = function (callback, input) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/marketplace/addons", {input: input}, "GET", null, null);
}

XOWL.prototype.marketplaceGetAddon = function (callback, addonId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/marketplace/addons/" + encodeURIComponent(addonId), null, "GET", null, null);
}

XOWL.prototype.marketplaceInstallAddon = function (callback, addonId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/marketplace/addons/" + encodeURIComponent(addonId) + "/install", null, "POST", null, null);
}



/*****************************************************
 * Utility API
 ****************************************************/

XOWL.prototype.doRequest = function (callback, complement, parameters, method, contentType, content) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText);
		}
	}
	var uri = this.endpoint + complement;
	if (parameters != null) {
		var names = Object.getOwnPropertyNames(parameters);
		var first = true;
		for (var p = 0; p != names.length; p++) {
			var value = parameters[names[p]];
			if (Array.isArray(value)) {
				for (var i = 0; i != value.length; i++) {
					uri += (first) ? "?" : "&";
					uri += names[p];
					uri += "=";
					uri += encodeURIComponent(value[i]);
					first = false;
				}
			} else {
				uri += (first) ? "?" : "&";
				uri += names[p];
				uri += "=";
				uri += encodeURIComponent(value);
			}
			first = false;
		}
	}
	xmlHttp.open(method, uri, true);
	xmlHttp.setRequestHeader("Accept", "text/plain, application/json");
	if (contentType !== null)
		xmlHttp.setRequestHeader("Content-Type", contentType);
	xmlHttp.withCredentials = true;
	if (content === null)
		xmlHttp.send();
	else if (contentType === MIME_JSON)
		xmlHttp.send(JSON.stringify(content));
	else
		xmlHttp.send(content);
}