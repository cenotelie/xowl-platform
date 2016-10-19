// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

function XOWL() {
	this.endpoint = '/api/';
	this.authToken = localStorage.getItem('xowl.authToken');
	this.userName = localStorage.getItem('xowl.userName');
}



////
// Security Service
////

XOWL.prototype.isLoggedIn = function () {
	return (this.authToken !== null && this.userName !== null);
}

XOWL.prototype.getUser = function () {
	return this.userName;
}

XOWL.prototype.login = function (callback, login, password) {
	var _self = this;
	var token = window.btoa(unescape(encodeURIComponent(login + ':' + password)));
	this.authToken = token;
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			_self.authToken = token;
			_self.userName = login;
			localStorage.setItem('xowl.authToken', token);
			localStorage.setItem('xowl.userName', login);
			callback(code, type, content);
		} else {
			_self.authToken = null;
			_self.userName = null;
			localStorage.removeItem('xowl.authToken');
			localStorage.removeItem('xowl.userName');
			callback(code, type, content);
		}
	}, "services/core/security", null);
}

XOWL.prototype.logout = function () {
	this.authToken = null;
	this.userName = null;
	localStorage.removeItem('xowl.authToken');
	localStorage.removeItem('xowl.userName');
}



////
// Admin Module - Platform Descriptor Service
////

XOWL.prototype.getPlatformBundles = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/platform", null);
}



////
// Admin Module - Logging Service
////

XOWL.prototype.getLogMessages = function (callback) {
	this.doQuery(function (code, type, content) {
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
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/jobs", null);
}

XOWL.prototype.getJob = function (callback, jobId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/jobs", {id: jobId});
}

XOWL.prototype.cancelJob = function (callback, jobId) {
	this.doCommand(function (code, type, content) {
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

XOWL.prototype.getStatisticsList = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/statistics", null);
}

XOWL.prototype.getStatisticsValues = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/statistics", null);
}



////
// Admin Module - Connection Service
////

XOWL.prototype.getDescriptors = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/descriptors", null);
}

XOWL.prototype.getConnectors = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", null);
}

XOWL.prototype.getConnector = function (callback, connectorId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {id: connectorId});
}

XOWL.prototype.createConnector = function (callback, domain, definition) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "spawn", descriptor: domain.identifier}, definition);
}

XOWL.prototype.deleteConnector = function (callback, connectorId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "delete", id: connectorId}, {});
}

XOWL.prototype.pullFromConnector = function (callback, connectorId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/admin/connectors", {action: "pull", id: connectorId}, {});
}

XOWL.prototype.pushToConnector = function (callback, connectorId, artifactId) {
	this.doCommand(function (code, type, content) {
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
	this.doQuery(function (code, type, content) {
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
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/domains", null);
}

XOWL.prototype.getBusinessDomain = function (callback, domainId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/domain", {id: domainId});
}

XOWL.prototype.getArtifactArchetypes = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/archetypes", null);
}

XOWL.prototype.getBusinessArchetype = function (callback, archetypeId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/archetype", {id: archetypeId});
}

XOWL.prototype.getBusinessSchemas = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/schemas", null);
}

XOWL.prototype.getBusinessSchema = function (callback, schemaId) {
	this.doQuery(function (code, type, content) {
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
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", null);
}

XOWL.prototype.getLiveArtifacts = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {live: "true"});
}

XOWL.prototype.getArtifactsForBase = function (callback, base) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {base: base});
}

XOWL.prototype.getArtifactsForArchetype = function (callback, archetype) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {archetype: archetype});
}

XOWL.prototype.getArtifact = function (callback, artifactId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {id: artifactId});
}

XOWL.prototype.getArtifactMetadata = function (callback, artifactId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {quads: "metadata", id: artifactId});
}

XOWL.prototype.getArtifactContent = function (callback, artifactId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {quads: "content", id: artifactId});
}

XOWL.prototype.deleteArtifact = function (callback, artifactId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {action: "delete", id: artifactId}, {});
}

XOWL.prototype.diffArtifacts = function (callback, artifactLeft, artifactRight) {
	this.doQuery(function (code, type, content) {
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
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts", {action: "pull", id: artifactId}, {});
}

XOWL.prototype.pushArtifactToLive = function (callback, artifactId) {
	this.doCommand(function (code, type, content) {
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
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {what: "document"});
}

XOWL.prototype.getUploadedDocument = function (callback, docId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {document: docId});
}

XOWL.prototype.getDocumentImporters = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {what: "importer"});
}

XOWL.prototype.getDocumentImporter = function (callback, importerId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {importer: importerId});
}

XOWL.prototype.getUploadedDocumentPreview = function (callback, docId, importer, configuration) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {preview: docId, importer: importer}, configuration);
}

XOWL.prototype.dropUploadedDocument = function (callback, docId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {drop: docId}, {});
}

XOWL.prototype.importUploadedDocument = function (callback, docId, importer, configuration) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/importation", {import: docId, importer: importer}, configuration);
}

XOWL.prototype.uploadDocument = function (callback, name, content) {
	this.doJSRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "PUT", "services/core/importation", {name: name}, content, "application/octet-stream", "application/json");
}



////
// Core Module - Consistency Service
////

XOWL.prototype.getInconsistencies = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/inconsistencies", null);
}

XOWL.prototype.getConsistencyRules = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", null);
}

XOWL.prototype.getConsistencyRule = function (callback, ruleId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {id: ruleId});
}

XOWL.prototype.newConsistencyRule = function (callback, name, message, prefixes, conditions) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
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
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {action: "activate", id: ruleId}, {});
}

XOWL.prototype.deactivateConsistencyRule = function (callback, ruleId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency", {action: "deactivate", id: ruleId}, {});
}

XOWL.prototype.deleteConsistencyRule = function (callback, ruleId) {
	this.doCommand(function (code, type, content) {
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
	this.doCommand(function (code, type, content) {
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
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluations", null);
}

XOWL.prototype.getEvaluation = function (callback, evaluationId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluation", {id: evaluationId});
}

XOWL.prototype.getEvaluableTypes = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluableTypes", null);
}

XOWL.prototype.getEvaluables = function (callback, typeId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/evaluables", {type: typeId});
}

XOWL.prototype.getEvaluationCriteria = function (callback, typeId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/evaluation/criterionTypes", {"for": typeId});
}

XOWL.prototype.newEvaluation = function (callback, definition) {
	this.doCommand(function (code, type, content) {
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
	this.doJSSPARQL(callback, payload);
}

XOWL.prototype.upload = function (callback, connectorURI, payload, contentType, name, base, version, supersede, archetype) {
	this.doJSUpload(callback, connectorURI, payload, contentType, name, base, version, supersede, archetype);
}



////
// Low-Level Core API
////

XOWL.prototype.doQuery = function (callback, target, parameters) {
	this.doJSQuery(callback, target, parameters);
}

XOWL.prototype.doCommand = function (callback, target, parameters, payload) {
	this.doJSCommand(callback, target, parameters, payload);
}

XOWL.prototype.doJSQuery = function (callback, target, parameters) {
	this.doJSRequest(callback, "GET", target, parameters, null, null, "text/plain, application/json");
}

XOWL.prototype.doJSCommand = function (callback, target, parameters, payload) {
	this.doJSRequest(callback, "POST", target, parameters, payload, "application/json", "text/plain, application/json");
}

XOWL.prototype.doJSSPARQL = function (callback, payload) {
	this.doJSRequest(callback, "POST", "services/core/sparql", null, payload, "application/sparql-query", "application/sparql-results+json, application/n-quads");
}

XOWL.prototype.doJSUpload = function (callback, connectorURI, payload, contentType, name, base, version, supersede, archetype) {
	var parameters = {
		name: name,
		base: base,
		version: version };
	if (supersede !== null && supersede !== "" && supersede !== "none")
		parameters.supersede = supersede;
	if (archetype !== null && archetype !== "")
		parameters.archetype = archetype;
	this.doJSRequest(callback, "POST", connectorURI, parameters, payload, contentType, "application/json");
}

XOWL.prototype.doJSRequest = function (callback, verb, uriComplement, parameters, payload, contentType, accept) {
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