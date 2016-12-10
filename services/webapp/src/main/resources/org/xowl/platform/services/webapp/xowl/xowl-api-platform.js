// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

/*****************************************************
 * xOWL Federation Platform API - V1
 ****************************************************/

function XOWL() {
	this.endpoint = '/api/';
	this.userLogin = localStorage.getItem('xowl.user.login');
	this.userIdentifier = localStorage.getItem('xowl.user.identifier');
	this.userName = localStorage.getItem('xowl.user.name');
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
			localStorage.setItem('xowl.user.login', login);
			localStorage.setItem('xowl.user.identifier', user.identifier);
			localStorage.setItem('xowl.user.name', user.name);
			callback(code, type, content);
		} else {
			_self.userLogin = null;
			_self.userIdentifier = null;
			_self.userName = null;
			localStorage.removeItem('xowl.user.login');
			localStorage.removeItem('xowl.user.identifier');
			localStorage.removeItem('xowl.user.name');
			callback(code, type, content);
		}
	}, "kernel/security/login", {login: login}, "POST", MIME_PLAIN_TEXT, password);
}

XOWL.prototype.logout = function () {
	this.doRequest(function (code, type, content) {
		_self.userLogin = null;
		_self.userIdentifier = null;
		_self.userName = null;
		localStorage.removeItem('xowl.user.login');
		localStorage.removeItem('xowl.user.identifier');
		localStorage.removeItem('xowl.user.name');
		callback(code, type, content);
	}, "kernel/security/logout", null, "POST", null, null);
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
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId), null, "DELETE", null, null);
}

XOWL.prototype.renamePlatformUser = function (callback, userId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId) + "/rename", {name: name}, "POST", null, null);
}

XOWL.prototype.changePlatformUserPassword = function (callback, userId, oldPassword, newPassword) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId) + "/updateKey", {oldKey: oldPassword}, "POST", MIME_PLAIN_TEXT, newPassword);
}

XOWL.prototype.resetPlatformUserPassword = function (callback, userId, newPassword) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
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
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId), null, "DELETE", null, null);
}

XOWL.prototype.renamePlatformGroup = function (callback, groupId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/rename", {name: name}, "POST", null, null);
}

XOWL.prototype.addMemberToPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/addMember", {user: userId}, "POST", null, null);
}

XOWL.prototype.removeMemberFromPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/removeMember", {user: userId}, "POST", null, null);
}

XOWL.prototype.addAdminToPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/addAdmin", {user: userId}, "POST", null, null);
}

XOWL.prototype.removeAdminFromPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
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
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles/" + encodeURIComponent(roleId), null, "DELETE", null, null);
}

XOWL.prototype.renamePlatformRole = function (callback, roleId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles/" + encodeURIComponent(roleId) + "/rename", {name: name}, "POST", null, null);
}

XOWL.prototype.assignRoleToPlatformUser = function (callback, roleId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId) + "/assign", {role: userId}, "POST", null, null);
}

XOWL.prototype.unassignRoleFromPlatformUser = function (callback, roleId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId) + "/unassign", {role: userId}, "POST", null, null);
}

XOWL.prototype.assignRoleToPlatformGroup = function (callback, roleId, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/assign", {role: userId}, "POST", null, null);
}

XOWL.prototype.unassignRoleFromPlatformGroup = function (callback, roleId, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/groups/" + encodeURIComponent(groupId) + "/unassign", {role: userId}, "POST", null, null);
}

XOWL.prototype.addPlatformRoleImplication = function (callback, roleId, impliedRoleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/roles/" + encodeURIComponent(roleId) + "/imply", {target: impliedRoleId}, "POST", null, null);
}

XOWL.prototype.removePlatformRoleImplication = function (callback, roleId, impliedRoleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
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

XOWL.prototype.getPlatformOSGiImpl = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform", null, "GET", null, null);
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
			callback(code, MIME_JSON, JSON.parse(content));
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

XOWL.prototype.getBusinessDomains = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/business/domains", null, "GET", null, null);
}

XOWL.prototype.getBusinessDomain = function (callback, domainId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/business/domains/" + encodeURIComponent(domainId), null, "GET", null, null);
}

XOWL.prototype.getArtifactArchetypes = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/business/archetypes", null, "GET", null, null);
}

XOWL.prototype.getBusinessArchetype = function (callback, archetypeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/business/archetypes/" + encodeURIComponent(archetypeId), null, "GET", null, null);
}

XOWL.prototype.getBusinessSchemas = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/business/schemas", null, "GET", null, null);
}

XOWL.prototype.getBusinessSchema = function (callback, schemaId) {
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
	}, "services/connection/connectors/" + encodeURIComponent(connectorId), {descriptor: descriptor.identifier}, "PUT", MIME_JSON, definition);
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
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "services/storage/artifacts/" + encodeURIComponent(artifactId) + "/metadata", null, "GET", null, null);
}

XOWL.prototype.getArtifactContent = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
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
			var leftIndex = content.indexOf("--xowlQuads");
			var rightIndex = content.lastIndexOf("--xowlQuads");
			var contentLeft = content.substring(leftIndex + "--xowlQuads".length, rightIndex);
			var contentRight = content.substring(rightIndex + "--xowlQuads".length);
			callback(code, MIME_JSON, {
				left: contentLeft,
				right: contentRight
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



////
// Core Module - Data Import Service
////

XOWL.prototype.getUploadedDocuments = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {what: "document"});
}

XOWL.prototype.getUploadedDocument = function (callback, docId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {document: docId});
}

XOWL.prototype.getDocumentImporters = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {what: "importer"});
}

XOWL.prototype.getDocumentImporter = function (callback, importerId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {importer: importerId});
}

XOWL.prototype.getUploadedDocumentPreview = function (callback, docId, importer, configuration) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {preview: docId, importer: importer}, configuration);
}

XOWL.prototype.dropUploadedDocument = function (callback, docId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {drop: docId}, {});
}

XOWL.prototype.importUploadedDocument = function (callback, docId, importer, configuration) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {import: docId, importer: importer}, configuration);
}

XOWL.prototype.uploadDocument = function (callback, name, content, fileName) {
	this.doHttpRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "PUT", "services/core/importation", {name: name, fileName: fileName}, content, "application/octet-stream", MIME_JSON);
}



////
// Core Module - Consistency Service
////

XOWL.prototype.getInconsistencies = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/inconsistencies", null);
}

XOWL.prototype.getConsistencyRules = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", null);
}

XOWL.prototype.getConsistencyRule = function (callback, ruleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {id: ruleId});
}

XOWL.prototype.newConsistencyRule = function (callback, name, message, prefixes, conditions) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {
		action: "create",
		name: name,
		message: message,
		prefixes: prefixes,
		conditions: conditions
	}, {});
}

XOWL.prototype.activateConsistencyRule = function (callback, ruleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {action: "activate", id: ruleId}, {});
}

XOWL.prototype.deactivateConsistencyRule = function (callback, ruleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {action: "deactivate", id: ruleId}, {});
}

XOWL.prototype.deleteConsistencyRule = function (callback, ruleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {action: "delete", id: ruleId}, {});
}



////
// Core Module - Impact Analysis Service
////

XOWL.prototype.newImpactAnalysis = function (callback, definition) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/impact", null, definition);
}



////
// Core Module - Evaluation Analysis Service
////

XOWL.prototype.getEvaluations = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluations", null);
}

XOWL.prototype.getEvaluation = function (callback, evaluationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluation", {id: evaluationId});
}

XOWL.prototype.getEvaluableTypes = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluableTypes", null);
}

XOWL.prototype.getEvaluables = function (callback, typeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluables", {type: typeId});
}

XOWL.prototype.getEvaluationCriteria = function (callback, typeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/criterionTypes", {"for": typeId});
}

XOWL.prototype.newEvaluation = function (callback, definition) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/service", null, definition);
}



////
// Core Module - Other API
////



XOWL.prototype.upload = function (callback, connectorURI, payload, contentType, name, base, version, supersede, archetype) {
	var parameters = {
		name: name,
		base: base,
		version: version };
	if (supersede !== null && supersede !== "" && supersede !== "none")
		parameters.supersede = supersede;
	if (archetype !== null && archetype !== "")
		parameters.archetype = archetype;
	this.doHttpRequest(callback, "POST", connectorURI, parameters, payload, contentType, MIME_JSON);
}



/*****************************************************
 * Utility API
 ****************************************************/

XOWL.prototype.doRequest = function (callback, complement, parameters, method, contentType, content) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText)
		}
	}
	var uri = this.endpoint + complement;
	if (parameters != null) {
		var names = Object.getOwnPropertyNames(parameters);
		for (var p = 0; p != names.length; p++) {
			var value = parameters[names[p]];
			uri += (p === 0) ? "?" : "&";
			uri += names[p];
			uri += "=";
			uri += encodeURIComponent(value);
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