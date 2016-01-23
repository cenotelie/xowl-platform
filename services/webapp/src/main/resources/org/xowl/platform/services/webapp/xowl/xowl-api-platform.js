// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

function XOWL() {
	this.endpoint = "/api/";
}

XOWL.prototype.getBasicStats = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "statistics");
}

XOWL.prototype.getPlatformBundles = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "statistics?platform");
}

XOWL.prototype.getDomains = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "domains");
}

XOWL.prototype.getConnectors = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "connectors");
}

XOWL.prototype.getConnector = function (callback, connectorId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "connectors?id=" + encodeURIComponent(connectorId));
}

XOWL.prototype.createConnector = function (callback, domain, definition) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "connectors?action=spawn&domain=" + encodeURIComponent(domain.identifier), definition);
}

XOWL.prototype.deleteConnector = function (callback, connectorId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, null, null);
		} else {
			callback(code, type, content);
		}
	}, "connectors?action=delete&id=" + encodeURIComponent(connectorId), {});
}

XOWL.prototype.pullFromConnector = function (callback, connectorId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "connectors?action=pull&id=" + encodeURIComponent(connectorId), {});
}

XOWL.prototype.pushToConnector = function (callback, connectorId, artifactId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "connectors?action=push&id=" + encodeURIComponent(connectorId) + "&artifact=" + encodeURIComponent(artifactId), {});
}

XOWL.prototype.getJobs = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "jobs");
}

XOWL.prototype.getJob = function (callback, jobId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "jobs?id=" + encodeURIComponent(jobId));
}

XOWL.prototype.getAllArtifacts = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "artifacts");
}

XOWL.prototype.getLiveArtifacts = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "artifacts?live=true");
}

XOWL.prototype.getArtifactVersions = function (callback, base) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "artifacts?base=" + encodeURIComponent(base));
}

XOWL.prototype.pullFromLive = function (callback, artifactId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "artifacts?action=pull&id=" + encodeURIComponent(artifactId), {});
}

XOWL.prototype.pushToLive = function (callback, artifactId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "artifacts?action=push&id=" + encodeURIComponent(artifactId), {});
}

XOWL.prototype.getArtifactMetadata = function (callback, artifactId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "artifacts?id=" + encodeURIComponent(artifactId));
}

XOWL.prototype.getArtifactContent = function (callback, artifactId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/n-quads", content);
		} else {
			callback(code, type, content);
		}
	}, "artifacts?content=true&id=" + encodeURIComponent(artifactId));
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
	}, "artifacts?diffLeft=" + encodeURIComponent(artifactLeft) + "&diffRight=" + encodeURIComponent(artifactRight));
}

XOWL.prototype.getInconsistencies = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "inconsistencies");
}

XOWL.prototype.getConsistencyRules = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "consistency");
}

XOWL.prototype.getConsistencyRule = function (callback, ruleId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "consistency?id=" + encodeURIComponent(ruleId));
}

XOWL.prototype.newConsistencyRule = function (callback, name, message, prefixes, conditions) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "consistency?action=create&name=" + encodeURIComponent(name) + "&message=" + encodeURIComponent(message) + "&prefixes=" + encodeURIComponent(prefixes) + "&conditions=" + encodeURIComponent(conditions), "");
}

XOWL.prototype.activateConsistencyRule = function (callback, ruleId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "consistency?action=activate&id=" + encodeURIComponent(ruleId), "");
}

XOWL.prototype.deactivateConsistencyRule = function (callback, ruleId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "consistency?action=deactivate&id=" + encodeURIComponent(ruleId), "");
}

XOWL.prototype.deleteConsistencyRule = function (callback, ruleId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "consistency?action=delete&id=" + encodeURIComponent(ruleId), "");
}




XOWL.prototype.doQuery = function (callback, target) {
	this.doJSQuery(callback, target);
}

XOWL.prototype.doJSQuery = function (callback, target) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText)
		}
	}
	xmlHttp.open("GET", this.endpoint + target, true);
	xmlHttp.setRequestHeader("Accept", "text/plain, application/json");
	xmlHttp.send();
}

XOWL.prototype.doCommand = function (callback, target, payload) {
	this.doJSCommand(callback, target, payload);
}

XOWL.prototype.doJSCommand = function (callback, target, payload) {
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
	xmlHttp.send(JSON.stringify(payload));
}

XOWL.prototype.sparql = function (callback, payload) {
	this.doJSSPARQL(callback, payload);
}

XOWL.prototype.doJSSPARQL = function (callback, payload) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText)
		}
	}
	xmlHttp.open("POST", this.endpoint + "sparql", true);
	xmlHttp.setRequestHeader("Accept", "application/sparql-results+json, application/n-quads");
	xmlHttp.setRequestHeader("Content-Type", "application/sparql-query");
	xmlHttp.send(payload);
}

XOWL.prototype.upload = function (callback, connectorURI, payload, contentType, name, base, version) {
	this.doJSUpload(callback, connectorURI, payload, contentType, name, base, version);
}

XOWL.prototype.doJSUpload = function (callback, connectorURI, payload, contentType, name, base, version) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText)
		}
	}
	xmlHttp.open("POST", this.endpoint + connectorURI + "?name=" + encodeURIComponent(name) + "&base=" + encodeURIComponent(base) + "&version=" + encodeURIComponent(version), true);
	xmlHttp.setRequestHeader("Accept", "application/json");
	xmlHttp.setRequestHeader("Content-Type", contentType);
	xmlHttp.send(payload);
}