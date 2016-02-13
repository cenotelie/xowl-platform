// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

function XOWL() {
	this.endpoint = '/api/';
	this.authToken = localStorage.getItem('xowl.authToken');
	this.userName = localStorage.getItem('xowl.userName');
}

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
	}, "services/core/security");
}

XOWL.prototype.logout = function () {
	this.authToken = null;
	this.userName = null;
	localStorage.removeItem('xowl.authToken');
	localStorage.removeItem('xowl.userName');
}

XOWL.prototype.getBasicStats = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/statistics");
}

XOWL.prototype.getPlatformBundles = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/statistics?platform");
}

XOWL.prototype.getDescriptors = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/descriptors");
}

XOWL.prototype.getConnectors = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/connectors");
}

XOWL.prototype.getConnector = function (callback, connectorId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/connectors?id=" + encodeURIComponent(connectorId));
}

XOWL.prototype.createConnector = function (callback, domain, definition) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/connectors?action=spawn&descriptor=" + encodeURIComponent(domain.identifier), definition);
}

XOWL.prototype.deleteConnector = function (callback, connectorId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "services/core/connectors?action=delete&id=" + encodeURIComponent(connectorId), {});
}

XOWL.prototype.pullFromConnector = function (callback, connectorId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/connectors?action=pull&id=" + encodeURIComponent(connectorId), {});
}

XOWL.prototype.pushToConnector = function (callback, connectorId, artifactId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/connectors?action=push&id=" + encodeURIComponent(connectorId) + "&artifact=" + encodeURIComponent(artifactId), {});
}

XOWL.prototype.getBusinessDomains = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/domains");
}

XOWL.prototype.getBusinessDomain = function (callback, domainId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/domain?id=" + encodeURIComponent(domainId));
}

XOWL.prototype.getArtifactArchetypes = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/archetypes");
}

XOWL.prototype.getBusinessArchetype = function (callback, archetypeId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/archetype?id=" + encodeURIComponent(archetypeId));
}

XOWL.prototype.getBusinessSchemas = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/schemas");
}

XOWL.prototype.getBusinessSchema = function (callback, schemaId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/business/schema?id=" + encodeURIComponent(schemaId));
}

XOWL.prototype.getJobs = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/jobs");
}

XOWL.prototype.getJob = function (callback, jobId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/jobs?id=" + encodeURIComponent(jobId));
}

XOWL.prototype.getAllArtifacts = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts");
}

XOWL.prototype.getLiveArtifacts = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts?live=true");
}

XOWL.prototype.getArtifactVersions = function (callback, base) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts?base=" + encodeURIComponent(base));
}

XOWL.prototype.pullFromLive = function (callback, artifactId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts?action=pull&id=" + encodeURIComponent(artifactId), {});
}

XOWL.prototype.pushToLive = function (callback, artifactId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts?action=push&id=" + encodeURIComponent(artifactId), {});
}

XOWL.prototype.getArtifact = function (callback, artifactId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts?id=" + encodeURIComponent(artifactId));
}

XOWL.prototype.getArtifactMetadata = function (callback, artifactId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts?quads=metadata&id=" + encodeURIComponent(artifactId));
}

XOWL.prototype.getArtifactContent = function (callback, artifactId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "services/core/artifacts?quads=content&id=" + encodeURIComponent(artifactId));
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
	}, "services/core/artifacts?diffLeft=" + encodeURIComponent(artifactLeft) + "&diffRight=" + encodeURIComponent(artifactRight));
}

XOWL.prototype.getInconsistencies = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/inconsistencies");
}

XOWL.prototype.getConsistencyRules = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency");
}

XOWL.prototype.getConsistencyRule = function (callback, ruleId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency?id=" + encodeURIComponent(ruleId));
}

XOWL.prototype.newConsistencyRule = function (callback, name, message, prefixes, conditions) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency?action=create&name=" + encodeURIComponent(name) + "&message=" + encodeURIComponent(message) + "&prefixes=" + encodeURIComponent(prefixes) + "&conditions=" + encodeURIComponent(conditions), "");
}

XOWL.prototype.activateConsistencyRule = function (callback, ruleId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency?action=activate&id=" + encodeURIComponent(ruleId), "");
}

XOWL.prototype.deactivateConsistencyRule = function (callback, ruleId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency?action=deactivate&id=" + encodeURIComponent(ruleId), "");
}

XOWL.prototype.deleteConsistencyRule = function (callback, ruleId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "services/core/consistency?action=delete&id=" + encodeURIComponent(ruleId), "");
}




XOWL.prototype.doQuery = function (callback, target) {
	this.doJSQuery(callback, target);
}

XOWL.prototype.doJSQuery = function (callback, target) {
	if (this.authToken === null || this.authToken == "")
		callback(401, "text/plain", "");
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText)
		}
	}
	xmlHttp.open("GET", this.endpoint + target, true);
	xmlHttp.setRequestHeader("Accept", "text/plain, application/json");
	xmlHttp.withCredentials = true;
	xmlHttp.setRequestHeader("Authorization", "Basic " + this.authToken);
	xmlHttp.send();
}

XOWL.prototype.doCommand = function (callback, target, payload) {
	this.doJSCommand(callback, target, payload);
}

XOWL.prototype.doJSCommand = function (callback, target, payload) {
	if (this.authToken === null || this.authToken == "")
		callback(401, "text/plain", "");
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText)
		}
	}
	xmlHttp.open("POST", this.endpoint + target, true);
	xmlHttp.setRequestHeader("Accept", "text/plain, application/json");
	xmlHttp.setRequestHeader("Content-Type", "application/json");
	xmlHttp.withCredentials = true;
	xmlHttp.setRequestHeader("Authorization", "Basic " + this.authToken);
	xmlHttp.send(JSON.stringify(payload));
}

XOWL.prototype.sparql = function (callback, payload) {
	this.doJSSPARQL(callback, payload);
}

XOWL.prototype.doJSSPARQL = function (callback, payload) {
	if (this.authToken === null || this.authToken == "")
		callback(401, "text/plain", "");
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText)
		}
	}
	xmlHttp.open("POST", this.endpoint + "services/core/sparql", true);
	xmlHttp.setRequestHeader("Accept", "application/sparql-results+json, application/n-quads");
	xmlHttp.setRequestHeader("Content-Type", "application/sparql-query");
	xmlHttp.withCredentials = true;
	xmlHttp.setRequestHeader("Authorization", "Basic " + this.authToken);
	xmlHttp.send(payload);
}

XOWL.prototype.upload = function (callback, connectorURI, payload, contentType, name, base, version, supersede, archetype) {
	this.doJSUpload(callback, connectorURI, payload, contentType, name, base, version, supersede, archetype);
}

XOWL.prototype.doJSUpload = function (callback, connectorURI, payload, contentType, name, base, version, supersede, archetype) {
	if (this.authToken === null || this.authToken == "")
		callback(401, "text/plain", "");
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText)
		}
	}
	var uri = this.endpoint + connectorURI + "?name=" + encodeURIComponent(name) + "&base=" + encodeURIComponent(base) + "&version=" + encodeURIComponent(version);
	if (supersede !== null && supersede !== "" && supersede !== "none")
		uri += "&supersede=" + encodeURIComponent(supersede);
	if (archetype !== null && archetype !== "")
		uri += "&archetype=" + encodeURIComponent(archetype);
	xmlHttp.open("POST", uri, true);
	xmlHttp.setRequestHeader("Accept", "application/json");
	xmlHttp.setRequestHeader("Content-Type", contentType);
	xmlHttp.withCredentials = true;
	xmlHttp.setRequestHeader("Authorization", "Basic " + this.authToken);
	xmlHttp.send(payload);
}