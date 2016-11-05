// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3


/*****************************************************
 * Page management and setup
 ****************************************************/

/**
 * DOM node for the Title component
 */
var PAGE_COMPONENT_TITLE = null;
/**
 * DOM node for the Header component
 */
var PAGE_COMPONENT_HEADER = null;
/**
 * DOM node for the Footer component
 */
var PAGE_COMPONENT_FOOTER = null;
/**
 * The current breadcrumbs for the page
 */
var PAGE_BREADCRUMBS = [{name: "Home", uri: "/web/"}];
/**
 * The current xOWL platform object (access to the platform API)
 */
var PLATFORM = null;
/**
 * The index to know whether the page is ready (ready on 100)
 */
var PAGE_READY_INDEX = 0;
/**
 * The hook to call when the page is ready
 */
var PAGE_READY_HOOK = null;
/**
 * Whether the current page is busy with a running operation
 * Null indicate that no operation is underway.
 * A non-null value is the function to be called to remove the on-going message.
 */
var PAGE_BUSY = null;

/**
 * Get the value of an HTTP parameter
 *
 * @param name The name of the parameter to retrieve
 * @return The value associated to the parameter
 */
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

/**
 * Performs the initial setup of the current page
 *
 * @param platform       The current xOWL platform object (access to the platform API)
 * @param mustBeLoggedIn Whether a user must be logged-in to see the page
 * @param breadcrumbs    The current breadcrumbs for the page
 * @param onReady        The hook to call when the page is ready
 */
function doSetupPage(platform, mustBeLoggedIn, breadcrumbs, onReady) {
	if (mustBeLoggedIn && (platform === null || !platform.isLoggedIn())) {
		document.location.href = "/web/login.html";
		return;
	}
	PLATFORM = platform;
	PAGE_BREADCRUMBS = PAGE_BREADCRUMBS.concat(breadcrumbs);
	PAGE_READY_HOOK = onReady;
}

function doSetupHeader() {
	if (PAGE_COMPONENT_TITLE === null || PAGE_COMPONENT_HEADER === null)
		return;
	document.getElementById("placeholder-header").appendChild(PAGE_COMPONENT_HEADER);
	document.getElementById("placeholder-title").appendChild(PAGE_COMPONENT_TITLE);
	var breadcrumbs = document.getElementById("placeholder-breadcrumbs");
	for (var i = 0; i != PAGE_BREADCRUMBS.length; i++) {
		var name = PAGE_BREADCRUMBS[i].name;
		var uri = PAGE_BREADCRUMBS[i].uri;
		if (uri instanceof String || typeof uri === 'string') {
			var a = document.createElement("a");
			a.appendChild(document.createTextNode(name));
			a.href = uri;
			var li = document.createElement("li");
			li.appendChild(a);
			breadcrumbs.appendChild(li);
		} else {
			var li = document.createElement("li");
			li.appendChild(document.createTextNode(name));
			li.classList.add("active");
			breadcrumbs.appendChild(li);
		}
	}
	if (PLATFORM !== null && PLATFORM.isLoggedIn()) {
		var userLink = document.getElementById("placeholder-user");
		var image = document.createElement("img");
		image.src = "/web/assets/user.svg";
		image.width = 25;
		image.height = 25;
		image.style.marginRight = "20px";
		userLink.appendChild(image);
		userLink.appendChild(document.createTextNode(PLATFORM.getUserName()));
		userLink.href = "/web/modules/admin/security/user.html?id=" + encodeURIComponent(PLATFORM.getUserId());
	}
	PAGE_READY_INDEX += 50;
	if (PAGE_READY_INDEX >= 100)
		PAGE_READY_HOOK();
}

function doSetupFooter() {
	if (PAGE_COMPONENT_FOOTER === null)
		return;
	document.getElementById("placeholder-footer").appendChild(PAGE_COMPONENT_FOOTER);
	PAGE_READY_INDEX += 50;
	if (PAGE_READY_INDEX >= 100)
		PAGE_READY_HOOK();
}

function onComponentLoadedTitle() {
	PAGE_COMPONENT_TITLE = onComponentLoadedGetNode("component-title");
	doSetupHeader();
}

function onComponentLoadedHeader() {
	PAGE_COMPONENT_HEADER = onComponentLoadedGetNode("component-header");
	doSetupHeader();
}

function onComponentLoadedFooter() {
	PAGE_COMPONENT_FOOTER = onComponentLoadedGetNode("component-footer");
	doSetupFooter();
}

function onComponentLoadedGetNode(identifier) {
	var object = document.getElementById(identifier);
	var doc = object.contentDocument.documentElement;
	object.style.display = "none";
	return doc.children[1].children[0].cloneNode(true);
}

/**
 * Reacts to the user clicking on the logout buttun
 */
function onClickLogout() {
	PLATFORM.logout();
	document.location.href = "/web/login.html";
}

/**
 * When an operation has been requested by the user
 *
 * @param message The message to display for the operation
 * @return Whether the operation can be performed
 */
function onOperationRequest(message) {
	if (PAGE_BUSY != null) {
		displayMessage("error", "Another operation is going on ...");
		return false;
	}
	PAGE_BUSY = displayLoader(message);
	return true;
}

/**
 * When the current operation was aborted
 *
 * @param message The message to display
 * @return Whether the operation was successful
 */
function onOperationAbort(message) {
	if (PAGE_BUSY != null) {
		displayMessage("error", "No on-going operation ...");
		return false;
	}
	PAGE_BUSY();
	PAGE_BUSY = null;
	displayMessage("error", message);
	return true;
}

/**
 * When an operation ended
 *
 * @param code          The HTTP code (other that 200 - OK)
 * @param content       The content of the HTTP response
 * @param customMessage A custom message to override the default one (may be undefined)
 * @return Whether the operation was successful
 */
function onOperationEnded(code, content, customMessage) {
	if (PAGE_BUSY != null) {
		displayMessage("error", "No on-going operation ...");
		return false;
	}
	PAGE_BUSY();
	PAGE_BUSY = null;
	if (code != 200) {
		if ((typeof customMessage) === "undefined")
			displayMessageHttpError(code, content);
		else
			displayMessage("error", customMessage);
	}
	return (code === 200);
}

/**
 * Displays a message for loading
 *
 * @param message The message to display
 * @return A function that can be called to remove the loading message
 */
function displayLoader(message) {
	var image = document.createElement("img");
	image.src = "/web/branding/spinner.gif";
	image.width = 32;
	image.height = 32;
	image.classList.add("message-icon");
	var content = document.createElement("span");
	var parts = message.split("\n");
	if (parts.length > 0) {
		content.appendChild(document.createTextNode(parts[0]));
		for (var i = 1; i != parts.length; i++) {
			content.appendChild(document.createElement("br"));
			content.appendChild(document.createTextNode(parts[i]));
		}
	}
	content.classList.add("message-content");
	var row = document.createElement("div");
	row.classList.add("header-message");
	row.appendChild(image);
	row.appendChild(content);
	var rows = document.getElementById("placeholder-messages");
	rows.appendChild(row);
	return function () {
		rows.removeChild(row);
	}
}

/**
 * Displays an information message
 *
 * @param type    The type of message (info, success, warning, error)
 * @param message The message to display
 */
function displayMessage(type, message) {
	var image = document.createElement("img");
	image.src = "/web/assets/message-" + type + ".svg";
	image.width = 32;
	image.height = 32;
	image.classList.add("message-icon");
	var content = document.createElement("span");
	var parts = message.split("\n");
	if (parts.length > 0) {
		content.appendChild(document.createTextNode(parts[0]));
		for (var i = 1; i != parts.length; i++) {
			content.appendChild(document.createElement("br"));
			content.appendChild(document.createTextNode(parts[i]));
		}
	}
	content.classList.add("message-content");
	var button = document.createElement("span");
	button.appendChild(document.createTextNode("×"));
	button.classList.add("message-button");
	var row = document.createElement("div");
	row.classList.add("header-message");
	row.classList.add("header-message-" + type);
	row.appendChild(image);
	row.appendChild(content);
	row.appendChild(button);
	var rows = document.getElementById("placeholder-messages");
	rows.appendChild(row);
	button.onclick = function () {
		rows.removeChild(row);
	}
}

/**
 * Displays an error message for a failed HTTP request
 *
 * @param code    The HTTP code (other that 200 - OK)
 * @param content The content of the HTTP response
 */
function displayMessageHttpError(code, content) {
	var message = null;
	switch (code) {
		case 400:
			message = "Oops, wrong request.";
			break;
		case 401:
			message =  "You must be logged in to perform this operation.";
			break;
		case 403:
			message =  "You are not authorized to perform this operation.";
			break;
		case 404:
			message =  "Can't find the requested data.";
			break;
		case 500:
			message =  "Something very wrong happened on the server ...";
			break;
		case 501:
			message =  "This operation is not supported.";
			break;
		case 520:
			message =  "The operation failed on the server.";
			break;
		default:
			message =  "The connection failed." + "(" + code + ")";
			break;
	}
	if (content != null && (content instanceof String || typeof content === 'string')) {
		message += "\n" + content;
	}
	displayMessage("error", message);
}


























/*
 * Page management
 */

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



var DEFAULT_URI_MAPPINGS = [
    ["rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"],
    ["rdfs", "http://www.w3.org/2000/01/rdf-schema#"],
    ["xsd", "http://www.w3.org/2001/XMLSchema#"],
    ["owl", "http://www.w3.org/2002/07/owl#"]];

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