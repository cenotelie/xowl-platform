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
 * Security Service
 ****************************************************/

XOWL.prototype.isLoggedIn = function () {
	return (this.userIdentifier !== null);
}

XOWL.prototype.getUserId = function () {
	return this.userIdentifier;
}

XOWL.prototype.getUserName = function () {
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
	}, "kernel/security/users", null);
}

XOWL.prototype.getPlatformUser = function (callback, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId), null);
}

XOWL.prototype.createPlatformUser = function (callback, userId, name, password) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId), {name: name}, password);
}

XOWL.prototype.deletePlatformUser = function (callback, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/security/users/" + encodeURIComponent(userId), null);
}

XOWL.prototype.renamePlatformUser = function (callback, userId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", {action: "rename", id: userId, name: name});
}

XOWL.prototype.changePlatformUserPassword = function (callback, userId, oldPassword, newPassword) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", {action: "changeKey", id: userId, oldKey: oldPassword, newKey: newPassword});
}

XOWL.prototype.resetPlatformUserPassword = function (callback, userId, newPassword) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", {action: "resetKey", id: userId, newKey: newPassword});
}

XOWL.prototype.getPlatformGroups = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", null);
}

XOWL.prototype.getPlatformGroup = function (callback, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {id: groupId});
}

XOWL.prototype.createPlatformGroup = function (callback, groupId, name, admin) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {id: groupId, name: name, admin: admin});
}

XOWL.prototype.deletePlatformGroup = function (callback, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {id: groupId});
}

XOWL.prototype.renamePlatformGroup = function (callback, groupId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "rename", id: groupId, name: name});
}

XOWL.prototype.addMemberToPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "addMember", id: groupId, user: userId});
}

XOWL.prototype.removeMemberFromPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "removeMember", id: groupId, user: userId});
}

XOWL.prototype.addAdminToPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "addAdmin", id: groupId, user: userId});
}

XOWL.prototype.removeAdminFromPlatformGroup = function (callback, groupId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "removeAdmin", id: groupId, user: userId});
}

XOWL.prototype.getPlatformRoles = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", null);
}

XOWL.prototype.getPlatformRole = function (callback, roleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {id: roleId});
}

XOWL.prototype.createPlatformRole = function (callback, roleId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {id: roleId, name: name});
}

XOWL.prototype.deletePlatformRole = function (callback, roleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {id: roleId});
}

XOWL.prototype.renamePlatformRole = function (callback, roleId, name) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "rename", id: roleId, name: name});
}

XOWL.prototype.assignRoleToPlatformUser = function (callback, roleId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "assign", id: roleId, user: userId});
}

XOWL.prototype.unassignRoleFromPlatformUser = function (callback, roleId, userId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "unassign", id: roleId, user: userId});
}

XOWL.prototype.assignRoleToPlatformGroup = function (callback, roleId, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "assign", id: roleId, group: groupId});
}

XOWL.prototype.unassignRoleFromPlatformGroup = function (callback, roleId, groupId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "unassign", id: roleId, group: groupId});
}

XOWL.prototype.addPlatformRoleImplication = function (callback, roleId, impliedRoleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "addImplication", id: roleId, target: impliedRoleId});
}

XOWL.prototype.removePlatformRoleImplication = function (callback, roleId, impliedRoleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "removeImplication", id: roleId, target: impliedRoleId});
}



////
// Admin Module - Platform Descriptor Service
////

XOWL.prototype.getPlatformOSGiImpl = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform", null);
}

XOWL.prototype.getPlatformBundles = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/bundles", null);
}

XOWL.prototype.platformShutdown = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, content);
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/shutdown", null, null);
}

XOWL.prototype.platformRestart = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, content);
		} else {
			callback(code, type, content);
		}
	}, "kernel/platform/restart", null, null);
}



////
// Admin Module - Logging Service
////

XOWL.prototype.getLogMessages = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/log", null);
}



////
// Admin Module - Job Execution Service
////

XOWL.prototype.getJobs = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/jobs", null);
}

XOWL.prototype.getJob = function (callback, jobId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/jobs", {id: jobId});
}

XOWL.prototype.cancelJob = function (callback, jobId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/jobs", {cancel: jobId}, null);
}



////
// Admin Module - Statistics Service
////

XOWL.prototype.getAllMetrics = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/statistics/metrics", null);
}

XOWL.prototype.getMetric = function (callback, metricId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/statistics/metrics/" + encodeURIComponent(metricId), null);
}

XOWL.prototype.getMetricSnapshot = function (callback, metricId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "kernel/statistics/metrics/" + encodeURIComponent(metricId) + "/snapshot", null);
}



////
// Admin Module - Connection Service
////

XOWL.prototype.getDescriptors = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/descriptors", null);
}

XOWL.prototype.getConnectors = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", null);
}

XOWL.prototype.getConnector = function (callback, connectorId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {id: connectorId});
}

XOWL.prototype.createConnector = function (callback, domain, definition) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "spawn", descriptor: domain.identifier}, definition);
}

XOWL.prototype.deleteConnector = function (callback, connectorId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "delete", id: connectorId}, {});
}

XOWL.prototype.pullFromConnector = function (callback, connectorId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "pull", id: connectorId}, {});
}

XOWL.prototype.pushToConnector = function (callback, connectorId, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "push", id: connectorId, artifact: artifactId}, {});
}



////
// Core Module - Web Application Modules Service
////

XOWL.prototype.getWebModules = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/webapp/modules", null);
}



////
// Core Module - Business Directory Service
////

XOWL.prototype.getBusinessDomains = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/domains", null);
}

XOWL.prototype.getBusinessDomain = function (callback, domainId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/domain", {id: domainId});
}

XOWL.prototype.getArtifactArchetypes = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/archetypes", null);
}

XOWL.prototype.getBusinessArchetype = function (callback, archetypeId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/archetype", {id: archetypeId});
}

XOWL.prototype.getBusinessSchemas = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/schemas", null);
}

XOWL.prototype.getBusinessSchema = function (callback, schemaId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/schema", {id: schemaId});
}



////
// Core Module - Artifact Storage Service
////

XOWL.prototype.getAllArtifacts = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", null);
}

XOWL.prototype.getLiveArtifacts = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {live: "true"});
}

XOWL.prototype.getArtifactsForBase = function (callback, base) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {base: base});
}

XOWL.prototype.getArtifactsForArchetype = function (callback, archetype) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {archetype: archetype});
}

XOWL.prototype.getArtifact = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {id: artifactId});
}

XOWL.prototype.getArtifactMetadata = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {quads: "metadata", id: artifactId});
}

XOWL.prototype.getArtifactContent = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {quads: "content", id: artifactId});
}

XOWL.prototype.deleteArtifact = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {action: "delete", id: artifactId}, {});
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
	}, "services/core/artifacts", {diffLeft: artifactLeft, diffRight: artifactRight});
}

XOWL.prototype.pullArtifactFromLive = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {action: "pull", id: artifactId}, {});
}

XOWL.prototype.pushArtifactToLive = function (callback, artifactId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {action: "push", id: artifactId}, {});
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
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/inconsistencies", null);
}

XOWL.prototype.getConsistencyRules = function (callback) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", null);
}

XOWL.prototype.getConsistencyRule = function (callback, ruleId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {id: ruleId});
}

XOWL.prototype.newConsistencyRule = function (callback, name, message, prefixes, conditions) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
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
			callback(code, MIME_JSON, JSON.parse(content).payload);
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
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluations", null);
}

XOWL.prototype.getEvaluation = function (callback, evaluationId) {
	this.doRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, MIME_JSON, JSON.parse(content).payload);
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
			callback(code, MIME_JSON, JSON.parse(content).payload);
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
			callback(code, MIME_JSON, JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/service", null, definition);
}



////
// Core Module - Other API
////

XOWL.prototype.sparql = function (callback, payload) {
	this.doHttpRequest(callback, "POST", "services/core/sparql", null, payload, "application/sparql-query", "application/sparql-results+json, application/n-quads");
}

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
	var uri = this.endpoint + uriComplement;
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