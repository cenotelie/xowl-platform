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
var MSG_ERROR_INTERNAL_ERROR = "Something wrong happened ...";
var MSG_ERROR_CONNECTION = "Error while accessing the server!";

function setupPage(xowl) {
	if (xowl !== null && !xowl.isLoggedIn()) {
		document.location.href = "/web/login.html";
		return;
	}
	document.getElementById("branding-title").onload = function () {
		document.title = document.getElementById("branding-title").contentDocument.getElementById("title-value").innerHTML + document.title;
	};
	document.getElementById("btn-logout").innerHTML = "Logout (" + xowl.getUser() + ")";
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
	if (payload instanceof String)
		return payload;
	return JSON.stringify(payload);
}

function renderXSPReply(xsp) {
	if (!xsp.hasOwnProperty("isSuccess"))
		return "No result ...";
	if (!xsp.isSuccess) {
		return "FAILURE: " + xsp.message;
	} else if (xsp.hasOwnProperty("payload")) {
		// TODO: complete this
		return xsp.payload.name;
	} else {
		return "SUCCESS: " + xsp.message;
	}
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
		case 502:
			return (MSG_ERROR_INTERNAL_ERROR + (content !== null ? "\n" + content : ""));
		default:
			return (MSG_ERROR_CONNECTION + (content !== null ? "\n" + content : ""));
	}
}

function trackJob(jobId, text, callback) {
	var link = document.createElement("a");
	link.href = "/web/modules/core/jobs/job.html?id=" + encodeURIComponent(jobId);
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