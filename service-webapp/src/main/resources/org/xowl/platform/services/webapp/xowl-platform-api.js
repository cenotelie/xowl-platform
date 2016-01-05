// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

function XOWL() {
	this.endpoint = "/api/";
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
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "artifacts&id=" + encodeURIComponent(artifactId));
}

XOWL.prototype.getArtifactContent = function (callback, artifactId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content));
		} else {
			callback(code, type, content);
		}
	}, "artifacts&content=true&id=" + encodeURIComponent(artifactId));
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