// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

function XOWL() {
	this.endpoint = '/api/';
	this.authToken = localStorage.getItem('xowl.authToken');
	this.userId = localStorage.getItem('xowl.userId');
	this.userName = localStorage.getItem('xowl.userName');
}



////
// Security Service
////

XOWL.prototype.isLoggedIn = function () {
	return (this.authToken !== null && this.userId !== null);
}

XOWL.prototype.getUserId = function () {
	return this.userId;
}

XOWL.prototype.getUserName = function () {
	return this.userName;
}

XOWL.prototype.login = function (callback, login, password) {
	var _self = this;
	var token = window.btoa(unescape(encodeURIComponent(login + ':' + password)));
	this.authToken = token;
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			var user = JSON.parse(content);
			_self.authToken = token;
			_self.userId = user.identifier;
			_self.userName = user.name;
			localStorage.setItem('xowl.authToken', token);
			localStorage.setItem('xowl.userId', user.identifier);
			localStorage.setItem('xowl.userName', user.name);
			callback(code, type, content);
		} else {
			_self.authToken = null;
			_self.userId = null;
			_self.userName = null;
			localStorage.removeItem('xowl.authToken');
			localStorage.removeItem('xowl.userId');
			localStorage.removeItem('xowl.userName');
			callback(code, type, content);
		}
	}, "services/core/security", null);
}

XOWL.prototype.logout = function () {
	this.authToken = null;
	this.userId = null;
	this.userName = null;
	localStorage.removeItem('xowl.authToken');
	localStorage.removeItem('xowl.userId');
	localStorage.removeItem('xowl.userName');
}

XOWL.prototype.getPlatformUsers = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", null);
}

XOWL.prototype.getPlatformUser = function (callback, userId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", {id: userId});
}

XOWL.prototype.createPlatformUser = function (callback, userId, name, password) {
	this.doHttpPut(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", {id: userId, name: name, key: password});
}

XOWL.prototype.deletePlatformUser = function (callback, userId) {
	this.doHttpDelete(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", {id: userId});
}

XOWL.prototype.renamePlatformUser = function (callback, userId, name) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", {action: "rename", id: userId, name: name});
}

XOWL.prototype.changePlatformUserPassword = function (callback, userId, oldPassword, newPassword) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", {action: "changeKey", id: userId, oldKey: oldPassword, newKey: newPassword});
}

XOWL.prototype.resetPlatformUserPassword = function (callback, userId, newPassword) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/users", {action: "resetKey", id: userId, newKey: newPassword});
}

XOWL.prototype.getPlatformGroups = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", null);
}

XOWL.prototype.getPlatformGroup = function (callback, groupId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {id: groupId});
}

XOWL.prototype.createPlatformGroup = function (callback, groupId, name, admin) {
	this.doHttpPut(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {id: groupId, name: name, admin: admin});
}

XOWL.prototype.deletePlatformGroup = function (callback, groupId) {
	this.doHttpDelete(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {id: groupId});
}

XOWL.prototype.renamePlatformGroup = function (callback, groupId, name) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "rename", id: groupId, name: name});
}

XOWL.prototype.addMemberToPlatformGroup = function (callback, groupId, userId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "addMember", id: groupId, user: userId});
}

XOWL.prototype.removeMemberFromPlatformGroup = function (callback, groupId, userId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "removeMember", id: groupId, user: userId});
}

XOWL.prototype.addAdminToPlatformGroup = function (callback, groupId, userId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "addAdmin", id: groupId, user: userId});
}

XOWL.prototype.removeAdminFromPlatformGroup = function (callback, groupId, userId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/groups", {action: "removeAdmin", id: groupId, user: userId});
}

XOWL.prototype.getPlatformRoles = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", null);
}

XOWL.prototype.getPlatformRole = function (callback, roleId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {id: roleId});
}

XOWL.prototype.createPlatformRole = function (callback, roleId, name) {
	this.doHttpPut(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {id: roleId, name: name});
}

XOWL.prototype.deletePlatformRole = function (callback, roleId) {
	this.doHttpDelete(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {id: roleId});
}

XOWL.prototype.renamePlatformRole = function (callback, roleId, name) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "rename", id: roleId, name: name});
}

XOWL.prototype.assignRoleToPlatformUser = function (callback, roleId, userId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "assign", id: roleId, user: userId});
}

XOWL.prototype.unassignRoleFromPlatformUser = function (callback, roleId, userId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "unassign", id: roleId, user: userId});
}

XOWL.prototype.assignRoleToPlatformGroup = function (callback, roleId, groupId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "assign", id: roleId, group: groupId});
}

XOWL.prototype.unassignRoleFromPlatformGroup = function (callback, roleId, groupId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "unassign", id: roleId, group: groupId});
}

XOWL.prototype.addPlatformRoleImplication = function (callback, roleId, impliedRoleId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "addImplication", id: roleId, target: impliedRoleId});
}

XOWL.prototype.removePlatformRoleImplication = function (callback, roleId, impliedRoleId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/security/roles", {action: "removeImplication", id: roleId, target: impliedRoleId});
}



////
// Admin Module - Platform Descriptor Service
////

XOWL.prototype.getPlatformOSGiImpl = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/platform", null);
}

XOWL.prototype.getPlatformBundles = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/platform/bundles", null);
}

XOWL.prototype.platformShutdown = function (callback) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/platform", {"action": "shutdown"}, null);
}

XOWL.prototype.platformRestart = function (callback) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/platform", {"action": "restart"}, null);
}



////
// Admin Module - Logging Service
////

XOWL.prototype.getLogMessages = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/log", null);
}



////
// Admin Module - Job Execution Service
////

XOWL.prototype.getJobs = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/jobs", null);
}

XOWL.prototype.getJob = function (callback, jobId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/jobs", {id: jobId});
}

XOWL.prototype.cancelJob = function (callback, jobId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/jobs", {cancel: jobId}, null);
}



////
// Admin Module - Statistics Service
////

XOWL.prototype.getAllMetrics = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/statistics", null);
}

XOWL.prototype.getMetric = function (callback, metricId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/statistics", {id: metricId});
}

XOWL.prototype.getMetricSnapshot = function (callback, metricId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/statistics", {poll: metricId});
}



////
// Admin Module - Connection Service
////

XOWL.prototype.getDescriptors = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/descriptors", null);
}

XOWL.prototype.getConnectors = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", null);
}

XOWL.prototype.getConnector = function (callback, connectorId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {id: connectorId});
}

XOWL.prototype.createConnector = function (callback, domain, definition) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "spawn", descriptor: domain.identifier}, definition);
}

XOWL.prototype.deleteConnector = function (callback, connectorId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "delete", id: connectorId}, {});
}

XOWL.prototype.pullFromConnector = function (callback, connectorId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "pull", id: connectorId}, {});
}

XOWL.prototype.pushToConnector = function (callback, connectorId, artifactId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "push", id: connectorId, artifact: artifactId}, {});
}



////
// Core Module - Web Application Modules Service
////

XOWL.prototype.getWebModules = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/webapp/modules", null);
}



////
// Core Module - Business Directory Service
////

XOWL.prototype.getBusinessDomains = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/domains", null);
}

XOWL.prototype.getBusinessDomain = function (callback, domainId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/domain", {id: domainId});
}

XOWL.prototype.getArtifactArchetypes = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/archetypes", null);
}

XOWL.prototype.getBusinessArchetype = function (callback, archetypeId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/archetype", {id: archetypeId});
}

XOWL.prototype.getBusinessSchemas = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/schemas", null);
}

XOWL.prototype.getBusinessSchema = function (callback, schemaId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/schema", {id: schemaId});
}



////
// Core Module - Artifact Storage Service
////

XOWL.prototype.getAllArtifacts = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", null);
}

XOWL.prototype.getLiveArtifacts = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {live: "true"});
}

XOWL.prototype.getArtifactsForBase = function (callback, base) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {base: base});
}

XOWL.prototype.getArtifactsForArchetype = function (callback, archetype) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {archetype: archetype});
}

XOWL.prototype.getArtifact = function (callback, artifactId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {id: artifactId});
}

XOWL.prototype.getArtifactMetadata = function (callback, artifactId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {quads: "metadata", id: artifactId});
}

XOWL.prototype.getArtifactContent = function (callback, artifactId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {quads: "content", id: artifactId});
}

XOWL.prototype.deleteArtifact = function (callback, artifactId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {action: "delete", id: artifactId}, {});
}

XOWL.prototype.diffArtifacts = function (callback, artifactLeft, artifactRight) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			var leftIndex = content.indexOf("--xowlQuads");
			var rightIndex = content.lastIndexOf("--xowlQuads");
			var contentLeft = content.substring(leftIndex + "--xowlQuads".length, rightIndex);
			var contentRight = content.substring(rightIndex + "--xowlQuads".length);
			callback(code, "application/json", {
				left: contentLeft,
				right: contentRight
			});
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {diffLeft: artifactLeft, diffRight: artifactRight});
}

XOWL.prototype.pullArtifactFromLive = function (callback, artifactId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {action: "pull", id: artifactId}, {});
}

XOWL.prototype.pushArtifactToLive = function (callback, artifactId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {action: "push", id: artifactId}, {});
}



////
// Core Module - Data Import Service
////

XOWL.prototype.getUploadedDocuments = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {what: "document"});
}

XOWL.prototype.getUploadedDocument = function (callback, docId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {document: docId});
}

XOWL.prototype.getDocumentImporters = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {what: "importer"});
}

XOWL.prototype.getDocumentImporter = function (callback, importerId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {importer: importerId});
}

XOWL.prototype.getUploadedDocumentPreview = function (callback, docId, importer, configuration) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {preview: docId, importer: importer}, configuration);
}

XOWL.prototype.dropUploadedDocument = function (callback, docId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {drop: docId}, {});
}

XOWL.prototype.importUploadedDocument = function (callback, docId, importer, configuration) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {import: docId, importer: importer}, configuration);
}

XOWL.prototype.uploadDocument = function (callback, name, content, fileName) {
	this.doHttpRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "PUT", "services/core/importation", {name: name, fileName: fileName}, content, "application/octet-stream", "application/json");
}



////
// Core Module - Consistency Service
////

XOWL.prototype.getInconsistencies = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/inconsistencies", null);
}

XOWL.prototype.getConsistencyRules = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", null);
}

XOWL.prototype.getConsistencyRule = function (callback, ruleId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {id: ruleId});
}

XOWL.prototype.newConsistencyRule = function (callback, name, message, prefixes, conditions) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
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
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {action: "activate", id: ruleId}, {});
}

XOWL.prototype.deactivateConsistencyRule = function (callback, ruleId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {action: "deactivate", id: ruleId}, {});
}

XOWL.prototype.deleteConsistencyRule = function (callback, ruleId) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {action: "delete", id: ruleId}, {});
}



////
// Core Module - Impact Analysis Service
////

XOWL.prototype.newImpactAnalysis = function (callback, definition) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/impact", null, definition);
}



////
// Core Module - Evaluation Analysis Service
////

XOWL.prototype.getEvaluations = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluations", null);
}

XOWL.prototype.getEvaluation = function (callback, evaluationId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluation", {id: evaluationId});
}

XOWL.prototype.getEvaluableTypes = function (callback) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluableTypes", null);
}

XOWL.prototype.getEvaluables = function (callback, typeId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluables", {type: typeId});
}

XOWL.prototype.getEvaluationCriteria = function (callback, typeId) {
	this.doHttpGet(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/criterionTypes", {"for": typeId});
}

XOWL.prototype.newEvaluation = function (callback, definition) {
	this.doHttpPost(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
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
	this.doHttpRequest(callback, "POST", connectorURI, parameters, payload, contentType, "application/json");
}



////
// Low-Level Core API
////

XOWL.prototype.doHttpGet = function (callback, target, parameters) {
	this.doHttpRequest(callback, "GET", target, parameters, null, null, "text/plain, application/json");
}

XOWL.prototype.doHttpPost = function (callback, target, parameters, payload) {
	this.doHttpRequest(callback, "POST", target, parameters, payload, "application/json", "text/plain, application/json");
}

XOWL.prototype.doHttpPut = function (callback, target, parameters, payload) {
	this.doHttpRequest(callback, "PUT", target, parameters, payload, "application/json", "text/plain, application/json");
}

XOWL.prototype.doHttpDelete = function (callback, target, parameters) {
	this.doHttpRequest(callback, "DELETE", target, parameters, null, null, "text/plain, application/json");
}

XOWL.prototype.doHttpRequest = function (callback, verb, uriComplement, parameters, payload, contentType, accept) {
	if (this.authToken === null || this.authToken == "")
		callback(401, "text/plain", "");
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
	xmlHttp.open(verb, uri, true);
	xmlHttp.setRequestHeader("Accept", accept);
	if (contentType != null)
		xmlHttp.setRequestHeader("Content-Type", contentType);
	xmlHttp.withCredentials = true;
	xmlHttp.setRequestHeader("Authorization", "Basic " + this.authToken);
	if (payload === null)
		xmlHttp.send();
	else if (contentType === "application/json")
		xmlHttp.send(JSON.stringify(payload));
	else
		xmlHttp.send(payload);
}