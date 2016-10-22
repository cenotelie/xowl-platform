// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var DEFAULT_URI_MAPPINGS = [
    ["rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"],
    ["rdfs", "http://www.w3.org/2000/01/rdf-schema#"],
    ["xsd", "http://www.w3.org/2001/XMLSchema#"],
    ["owl", "http://www.w3.org/2002/07/owl#"]];

var MIME_TYPES = [
	{ name: 'N-Triples', value: 'application/n-triples', extensions: ['.nt'] },
	{ name: 'N-Quads', value: 'application/n-quads', extensions: ['.nq'] },
	{ name: 'Turtle', value: 'text/turtle', extensions: ['.ttl'] },
	{ name: 'TriG', value: 'application/trig', extensions: ['.trig'] },
	{ name: 'JSON-LD', value: 'application/ld+json', extensions: ['.jsonld'] },
	{ name: 'RDF/XML', value: 'application/rdf+xml', extensions: ['.rdf'] },
	{ name: 'Functional OWL2', value: 'text/owl-functional', extensions: ['.ofn', '.fs'] },
	{ name: 'OWL/XML', value: 'application/owl+xml', extensions: ['.owx', '.owl'] },
	{ name: 'xOWL RDF Rules', value: 'application/x-xowl-rdft', extensions: ['.rdft'] },
	{ name: 'xOWL Ontology', value: 'application/x-xowl', extensions: ['.xowl'] }
];

var MSG_ERROR_BAD_REQUEST = "Oops, wrong request.";
var MSG_ERROR_UNAUTHORIZED = "You must be logged in to perform this operation.";
var MSG_ERROR_FORBIDDEN = "You are not authorized to perform this operation.";
var MSG_ERROR_NOT_FOUND = "Can't find the requested data.";
var MSG_ERROR_INTERNAL_ERROR = "Something very wrong happened on the server ...";
var MSG_ERROR_NOT_IMPLEMENTED = "This operation is not supported.";
var MSG_ERROR_UNKNOWN_ERROR = "The operation failed on the server.";
var MSG_ERROR_OTHER = "The connection failed.";

function setupPage(xowl) {
	if (xowl !== null && !xowl.isLoggedIn()) {
		document.location.href = "/web/login.html";
		return;
	}
	document.getElementById("branding-title").onload = function () {
		document.title = document.getElementById("branding-title").contentDocument.getElementById("title-value").innerHTML + document.title;
	};
	document.getElementById("btn-logout").innerHTML = "Logout (" + xowl.getUserName() + ")";
	document.getElementById("btn-logout").onclick = function() {
		xowl.logout();
		document.location.href = "/web/login.html";
	};
}

function getShortURI(value) {
	for (var i = 0; i != DEFAULT_URI_MAPPINGS.length; i++) {
		if (value.indexOf(DEFAULT_URI_MAPPINGS[i][1]) === 0) {
			return DEFAULT_URI_MAPPINGS[i][0] + ":" + value.substring(DEFAULT_URI_MAPPINGS[i][1].length);
		}
	}
	return value;
}

function renderJobPayload(payload) {
	if (payload instanceof String || typeof payload === 'string')
		return payload;
	return JSON.stringify(payload);
}

function renderXSPReply(xsp) {
	if (!xsp.hasOwnProperty("isSuccess"))
		return "No result ...";
	if (!xsp.isSuccess) {
		return "FAILURE: " + xsp.message;
	} else if (xsp.hasOwnProperty("payload")) {
		if (xsp.payload == null)
			return "SUCCESS: " + xsp.message;
		if (xsp.payload instanceof String)
			return xsp.payload;
		return JSON.stringify(xsp.payload);
	} else {
		return "SUCCESS: " + xsp.message;
	}
}

function renderMessage(message) {
	if (message instanceof String || typeof message === 'string')
		return document.createTextNode(message);
	if (message.type === "org.xowl.platform.kernel.RichString") {
		var span = document.createElement("span");
		for (var i = 0; i != message.parts.length; i++) {
			span.appendChild(renderMessagePart(message.parts[i]));
		}
		return span;
	}
	return document.createTextNode(JSON.stringify(message));
}

function renderMessagePart(part) {
	if (part instanceof String || typeof part === 'string')
		return document.createTextNode(part);
	if (part.type === "org.xowl.platform.kernel.jobs.Job") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/admin/jobs/job.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.kernel.artifacts.Artifact") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/core/artifacts/artifact.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.kernel.platform.PlatformUser") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/admin/security/user.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.kernel.platform.PlatformGroup") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/admin/security/group.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.kernel.platform.PlatformRole") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/admin/security/role.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.services.connection.ConnectorService") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/admin/connectors/connector.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.services.consistency.ConsistencyRule") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/core/consistency/rule.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.services.importation.Document") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/core/importation/document.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	}
	return document.createTextNode(JSON.stringify(part));
}

function rdfToDom(value) {
	if (value.type === "uri" || value.type === "iri") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(getShortURI(value.value)));
		dom.href = "/web/modules/core/discovery/explorer.html?id=" + encodeURIComponent(value.value);
		dom.classList.add("rdfIRI");
		return dom;
    } else if (value.type === "bnode") {
		var dom = document.createElement("span");
		dom.appendChild(document.createTextNode('_:' + value.value));
		dom.classList.add("rdfBlank");
		return dom;
    } else if (value.type === "blank") {
		var dom = document.createElement("span");
		dom.appendChild(document.createTextNode('_:' + value.id));
		dom.classList.add("rdfBlank");
		return dom;
    } else if (value.type === "variable") {
		var dom = document.createElement("span");
		dom.appendChild(document.createTextNode('?' + value.value));
		dom.classList.add("rdfVariable");
		return dom;
    } else if (value.hasOwnProperty("lexical")) {
		var span1 = document.createElement("span");
		span1.appendChild(document.createTextNode('"' + value.lexical + '"'));
		var dom = document.createElement("span");
		dom.classList.add("rdfLiteral");
		dom.appendChild(span1);
		if (value.datatype !== null) {
			dom.appendChild(document.createTextNode("^^<"));
			var link = document.createElement("a");
			link.appendChild(document.createTextNode(getShortURI(value.datatype)));
			link.href = "/web/modules/core/discovery/explorer.html?id=" + encodeURIComponent(value.datatype);
			link.classList.add("rdfIRI");
			dom.appendChild(link);
			dom.appendChild(document.createTextNode(">"));
		}
		if (value.lang !== null) {
			var span2 = document.createElement("span");
			span2.appendChild(document.createTextNode('@' + value.lang));
			span2.classList.add("badge");
			dom.appendChild(span2);
		}
		return dom;
    } else {
		var span1 = document.createElement("span");
		span1.appendChild(document.createTextNode('"' + value.value + '"'));
		var dom = document.createElement("span");
		dom.classList.add("rdfLiteral");
		dom.appendChild(span1);
		if (value.datatype !== null) {
			dom.appendChild(document.createTextNode("^^<"));
			var link = document.createElement("a");
			link.appendChild(document.createTextNode(getShortURI(value.datatype)));
			link.href = "/web/modules/core/discovery/explorer.html?id=" + encodeURIComponent(value.datatype);
			link.classList.add("rdfIRI");
			dom.appendChild(link);
			dom.appendChild(document.createTextNode(">"));
		}
		if (value.hasOwnProperty("xml:lang")) {
			var span2 = document.createElement("span");
			span2.appendChild(document.createTextNode('@' + value["xml:lang"]));
			span2.classList.add("badge");
			dom.appendChild(span2);
		}
		return dom;
    }
}

function displayMessage(text) {
	if (text === null) {
		document.getElementById("loader").style.display = "none";
		return;
	}
	var parts = text.split("\n");
	var span = document.getElementById("loader-text");
	while (span.hasChildNodes())
		span.removeChild(span.lastChild);
	if (parts.length > 0) {
		span.appendChild(document.createTextNode(parts[0]));
		for (var i = 1; i != parts.length; i++) {
			span.appendChild(document.createElement("br"));
			span.appendChild(document.createTextNode(parts[i]));
		}
	}
	document.getElementById("loader").style.display = "";
}

function getErrorFor(code, content) {
	if (content != null) {
		if (content == '' || (typeof content) == 'undefined')
			content = null;
	}
	switch (code) {
		case 400:
			return (MSG_ERROR_BAD_REQUEST + (content !== null ? "\n" + content : ""));
		case 401:
			return (MSG_ERROR_UNAUTHORIZED + (content !== null ? "\n" + content : ""));
		case 403:
			return (MSG_ERROR_FORBIDDEN + (content !== null ? "\n" + content : ""));
		case 404:
			return (MSG_ERROR_NOT_FOUND + (content !== null ? "\n" + content : ""));
		case 500:
			return (MSG_ERROR_INTERNAL_ERROR + (content !== null ? "\n" + content : ""));
		case 501:
			return (MSG_ERROR_NOT_IMPLEMENTED + (content !== null ? "\n" + content : ""));
		case 520:
			return (MSG_ERROR_UNKNOWN_ERROR + (content !== null ? "\n" + content : ""));
		default:
			return (MSG_ERROR_OTHER + "(" + code + ")" + (content !== null ? "\n" + content : ""));
	}
}

function trackJob(jobId, text, callback) {
	var link = document.createElement("a");
	link.href = "/web/modules/admin/jobs/job.html?id=" + encodeURIComponent(jobId);
	link.appendChild(document.createTextNode(text));
	var span = document.getElementById("loader-text");
	while (span.hasChildNodes())
		span.removeChild(span.lastChild);
	span.appendChild(link);
	document.getElementById("loader").style.display = "";
	var doTrack = function () {
		xowl.getJob(function (status, ct, content) {
			if (status == 200) {
				JOB = content;
				if (content.status === "Completed") {
					JOB = null;
					if (content.result.isSuccess) {
						document.getElementById("loader").style.display = "none";
					} else {
						span.removeChild(link);
						span.appendChild(document.createTextNode("FAILURE: " + content.result.message));
					}
					callback(content.result.isSuccess)
				} else {
					window.setTimeout(doTrack, 2000);
				}
			}
		}, jobId);
	};
	doTrack();
}

function compareArtifacts(a1, a2) {
	var d1 = new Date(a1.creation);
	var d2 = new Date(a2.creation);
	return d1.getTime() < d2.getTime();
}


function Loader(count) {
	this.count = count;
	this.message = null;
}
Loader.prototype.onLoaded = function () {
	this.count--;
	if (this.count <= 0 && this.message == null)
		displayMessage(null);
}
Loader.prototype.onError = function (code, content) {
	this.count--;
	this.message = getErrorFor(code, content);
	displayMessage(message);
}