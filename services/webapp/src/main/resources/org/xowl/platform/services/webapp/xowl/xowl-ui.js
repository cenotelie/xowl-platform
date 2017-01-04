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
 * A non-null value is an object:
 * - count:   The number of concurrent ongoing operations.
 * - remover: The function to be called to remove the on-going message.
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
 * @param platform	   The current xOWL platform object (access to the platform API)
 * @param mustBeLoggedIn Whether a user must be logged-in to see the page
 * @param breadcrumbs	The current breadcrumbs for the page
 * @param onReady		The hook to call when the page is ready
 */
function doSetupPage(platform, mustBeLoggedIn, breadcrumbs, onReady) {
	if (mustBeLoggedIn && (platform === null || !platform.isLoggedIn())) {
		document.location.href = "/web/login.html";
		return;
	}
	PLATFORM = platform;
	PAGE_BREADCRUMBS = PAGE_BREADCRUMBS.concat(breadcrumbs);
	PAGE_READY_HOOK = onReady;
	loadComponent("/web/components/title.html", function (node) {
		PAGE_COMPONENT_TITLE = node;
		doSetupHeader();
	});
	loadComponent("/web/components/header.html", function (node) {
		PAGE_COMPONENT_HEADER = node;
		doSetupHeader();
	});
	loadComponent("/web/components/footer.html", function (node) {
		PAGE_COMPONENT_FOOTER = node;
		doSetupFooter();
	});
}

function loadComponent(component, callback) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			if (xmlHttp.status == 200) {
				var doc = document.implementation.createHTMLDocument("example");
				doc.documentElement.innerHTML = xmlHttp.responseText;
				var node = doc.documentElement.children[1].children[0].cloneNode(true);
				callback(node);
			}
		}
	}
	xmlHttp.open("GET", component, true);
	xmlHttp.setRequestHeader("Accept", "text/html");
	xmlHttp.send();
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
		userLink.appendChild(document.createTextNode(PLATFORM.getLoggedInUserName()));
		userLink.href = "/web/modules/admin/security/user.html?id=" + encodeURIComponent(PLATFORM.getLoggedInUserId());
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

/**
 * Reacts to the user clicking on the logout button
 */
function onClickLogout() {
	PLATFORM.logout();
	document.location.href = "/web/login.html";
}



/*****************************************************
 * HTTP operations management
 ****************************************************/

/**
 * When an operation has been requested by the user
 *
 * @param message The message to display for the operation
 * @param count   The number of concurrent ongoing operations
 * @return Whether the operation can be performed
 */
function onOperationRequest(message, count) {
	if (PAGE_BUSY != null) {
		displayMessage("error", "Another operation is going on ...");
		return false;
	}
	var c = 1;
	if ((typeof count) !== "undefined") {
		c = count;
	}
	var remover = displayLoader(message);
	PAGE_BUSY = { count: c, remover: remover };
	return true;
}

/**
 * When the current operation was aborted
 *
 * @param message The message to display
 * @return Whether the operation was successful
 */
function onOperationAbort(message) {
	if (PAGE_BUSY == null) {
		displayMessage("error", "No on-going operation ...");
		return false;
	}
	PAGE_BUSY.count--;
	if (PAGE_BUSY.count === 0) {
		PAGE_BUSY.remover();
		PAGE_BUSY = null;
		displayMessage("error", message);
	}
	return true;
}

/**
 * When an operation ended
 *
 * @param code		  The HTTP code (other that 200 - OK)
 * @param content	   The content of the HTTP response
 * @param customMessage A custom message to override the default one (may be undefined)
 * @return Whether the operation was successful
 */
function onOperationEnded(code, content, customMessage) {
	if (PAGE_BUSY == null) {
		displayMessage("error", "No on-going operation ...");
		return false;
	}
	PAGE_BUSY.count--;
	if (PAGE_BUSY.count === 0) {
		PAGE_BUSY.remover();
		PAGE_BUSY = null;
	}
	if (code != 200) {
		if ((typeof customMessage) === "undefined")
			displayMessageHttpError(code, content);
		else
			displayMessage("error", customMessage);
		if (code === 401 || code === 440)
			waitAndGo("/web/login.html?next=" + encodeURIComponent(window.location.pathname + window.location.search));
	}
	return (code === 200);
}



/*****************************************************
 * Message display
 ****************************************************/

/**
 * Displays a message for loading
 *
 * @param message The message to display
 * @return A function that can be called to remove the loading message
 */
function displayLoader(message) {
	var image = document.createElement("img");
	image.src = "/web/assets/spinner.gif";
	image.width = 32;
	image.height = 32;
	image.classList.add("header-message-icon");
	var content = renderMessage(message);
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
 * @param type	The type of message (info, success, warning, error)
 * @param message The message to display
 */
function displayMessage(type, message) {
	var image = document.createElement("img");
	image.src = "/web/assets/message-" + type + ".svg";
	image.width = 32;
	image.height = 32;
	image.classList.add("header-message-icon");
	var content = renderMessage(message);
	var button = document.createElement("span");
	button.appendChild(document.createTextNode("×"));
	button.classList.add("header-message-button");
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
 * @param code	The HTTP code (other that 200 - OK)
 * @param content The content of the HTTP response
 */
function displayMessageHttpError(code, content) {
	var message = null;
	switch (code) {
		case 400:
			message = "There is a problem with the request, see details.";
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
		case 440:
			message =  "The session has expired, login again to continue.";
			break;
		case 461:
			message =  "The SPARQL query failed.";
			break;
		case 500:
			message =  "An unexpected error occurred on the server.";
			break;
		case 501:
			message =  "This operation is not supported.";
			break;
		case 560:
			message =  "An unknown error error occurred on the server.";
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

/**
 * Renders in HTML the specified message
 *
 * @param message The message to render, may be a complex RichText message
 * @return The HTML DOM element corresponding to the rendered message
 */
function renderMessage(message) {
	var result = document.createElement("p");
	result.classList.add("header-message-content");
	if (message instanceof String || typeof message === 'string') {
		result.appendChild(renderMessagePart(message));
	} else if (message.type === "org.xowl.infra.utils.RichString") {
		for (var i = 0; i != message.parts.length; i++) {
			result.appendChild(renderMessagePart(message.parts[i]));
		}
	} else {
		result.appendChild(document.createTextNode(JSON.stringify(message)));
	}
	return result;
}

/**
 * Renders in HTML the specified message part
 *
 * @param message The message to part render
 * @return The HTML DOM element corresponding to the rendered message part
 */
function renderMessagePart(part) {
	if (part instanceof String || typeof part === 'string') {
		var parts = part.split("\n");
		if (parts.length > 0) {
			var dom = document.createElement("span");
			dom.appendChild(document.createTextNode(parts[0]));
			for (var i = 1; i != parts.length; i++) {
				dom.appendChild(document.createElement("br"));
				dom.appendChild(document.createTextNode(parts[i]));
			}
			return dom;
		} else {
			return document.createTextNode(part);
		}
	} else if (part.type === "org.xowl.platform.kernel.jobs.Job") {
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
	} else if (part.type === "org.xowl.platform.kernel.platform.Addon") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/admin/platform/addon.html?id=" + encodeURIComponent(part.identifier);
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
	} else if (part.type === "org.xowl.platform.services.evaluation.Evaluation") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/core/evaluation/eval.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.services.evaluation.EvaluationReference") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/core/evaluation/eval.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.services.importation.Document") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = "/web/modules/core/importation/document.html?id=" + encodeURIComponent(part.identifier);
		return dom;
	} else if (part.type === "org.xowl.platform.services.importation.Importer") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(part.name));
		dom.href = part.wizardUri;
		return dom;
	}
	return document.createTextNode(JSON.stringify(part));
}

/**
 * Waits for a small time (1.5s) and then refreshes the current page
 */
function waitAndRefresh() {
	window.setTimeout(function () {
		window.location.reload(true);
	}, 1500);
}

/**
 * Waits for a small time (1.5s) and then go to the specified reference
 *
 * @param target The target reference to go to after the wait
 */
function waitAndGo(target) {
	window.setTimeout(function () {
		window.location.href = target;
	}, 1500);
}



/*****************************************************
 * Auto-complete field
 ****************************************************/

function AutoComplete(inputId) {
	var __self = this;
	this.input = document.getElementById(inputId);
	this.indicator = document.getElementById(inputId + "-indicator");
	this.panel = null;
	this.input.oninput = function () {
		__self.onInputChange();
	};
	this.input.onblur = function () {
		/*if (__self.panel != null) {
			__self.panel.remove();
			__self.panel = null;
			__self.isSet = false;
		}*/
	};
}

/**
 * When the content of the input has changed
 *
 * @param items	The items to render
 */
AutoComplete.prototype.onInputChange = function () {
	if (PAGE_BUSY != null)
		return;
	PAGE_BUSY = { count: 1 };
	if (this.panel != null)
		this.panel.remove();
	if (this.input.value === null || this.input.value.length == 0) {
		PAGE_BUSY = null;
		return;
	}
	if (this.indicator != null) {
		this.indicator.src = "/web/assets/spinner.gif";
		this.indicator.style.display = "";
	}
	this.lookupItems(this.input.value);
}

/**
 * When the items to suggest have been received
 *
 * @param items	The items items
 */
AutoComplete.prototype.onItems = function (items) {
	var inputBounds = this.input.getBoundingClientRect();
	this.panel = document.createElement("div");
	this.panel.classList.add("autocomplete-panel");
	this.panel.style.width = inputBounds.width + "px";
	this.panel.style.left = inputBounds.left + "px";
	this.panel.style.top = inputBounds.bottom + "px";
	var count = items.length;
	if (count > 5)
		count = 5;
	for (var i = 0; i != count; i++) {
		var content = this.renderItem(items[i]);
		var node = document.createElement("div");
		node.appendChild(content);
		node.classList.add("autocomplete-item");
		(function (self, item, node) {
			node.onclick = function () {
				self.onItemClick(item);
			};
		})(this, items[i], node);
		this.panel.appendChild(node);
	}
	this.input.parentElement.appendChild(this.panel);
	if (this.indicator != null) {
		this.indicator.style.display = "none";
	}
	PAGE_BUSY = null;
}

/**
 * When an auto-complete suggestion has been clicked
 *
 * @param item The selected item
 */
AutoComplete.prototype.onItemClick = function (item) {
	this.input.value = this.getItemString(item);
	this.onItemSelected(item);
	this.panel.remove();
}

/**
 * Gets a list of the items that match the specified input
 *
 * @param value	The input value to match
 * @return The list of the matching items
 */
AutoComplete.prototype.lookupItems = function (value) {
	this.onItems([]);
}

/**
 * Renders an item for the auto-complete list
 *
 * @param item The item to render
 * @return The HTML DOM object for the item
 */
AutoComplete.prototype.renderItem = function (item) {
	var result = document.createElement("div");
	if (item instanceof String || typeof item === 'string') {
		result.appendChild(document.createTextNode(item));
	} else {
		result.appendChild(document.createTextNode(JSON.stringify(item)));
	}
	return result;
}

/**
 * Gets the string representation of an item
 *
 * @param item The item to render
 * @return The string representation for the item
 */
AutoComplete.prototype.getItemString = function (item) {
	if (item instanceof String || typeof item === 'string') {
		return item;
	}
	return JSON.stringify(item);
}

/**
 * Event when an item has been selected
 *
 * @param item The selected item
 */
AutoComplete.prototype.onItemSelected = function (item) {
}



/*****************************************************
 * Job tracking
 ****************************************************/

/**
 * Wait for a job to complete
 *
 * @param jobId	The identifier of the job to wait for
 * @param jobName  The name of the job
 * @param callback The callback when the job has been completed
 *				 The callback is expected to have 1 parameter for the job object
 */
function waitForJob(jobId, jobName, callback) {
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: [
			"Job ",
			{type: "org.xowl.platform.kernel.jobs.Job", identifier: jobId, name: jobName},
			" is running ..."]}))
		return;

	var trackOnce = function() {
		xowl.getJob(function (status, ct, content) {
			if (status == 200) {
				if (content.status === "Completed") {
					onOperationEnded(status, content);
					callback(content);
				} else {
					window.setTimeout(trackOnce, 2000);
				}
			} else {
				onOperationEnded(status, content, { type: "org.xowl.infra.utils.RichString", parts: [
					"Failed to retrieve data for job ",
					{type: "org.xowl.platform.kernel.jobs.Job", identifier: jobId, name: jobName}]});
			}
		}, jobId);
	};
	trackOnce();
}



/*****************************************************
 * RDF rendering
 ****************************************************/

/*
 * The known MIME types for the RDF and OWL datasets
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

/*
 * The defaults known URI mappings
 */
var DEFAULT_URI_MAPPINGS = [
	["rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"],
	["rdfs", "http://www.w3.org/2000/01/rdf-schema#"],
	["xsd", "http://www.w3.org/2001/XMLSchema#"],
	["owl", "http://www.w3.org/2002/07/owl#"]];

/*
 * Gets the short URI (prefix:suffix) for the specified full URI
 *
 * @param value The full URI
 * @return The associated short URI (or the full URI if it cannot be shortened)
 */
function getShortURI(value) {
	for (var i = 0; i != DEFAULT_URI_MAPPINGS.length; i++) {
		if (value.indexOf(DEFAULT_URI_MAPPINGS[i][1]) === 0) {
			return DEFAULT_URI_MAPPINGS[i][0] + ":" + value.substring(DEFAULT_URI_MAPPINGS[i][1].length);
		}
	}
	return value;
}

/*
 * Gets the HTML DOM object rendering the specified RDF node
 *
 * @param value An RDF node (represented as a Javascript object)
 * @return The HTML DOM rendering of the node
 */
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



/*****************************************************
 * Misc
 ****************************************************/

/*
 * Compares two artifacts by their creation time
 *
 * @param a1 An artifact
 * @param a2 Another artifact
 * @return Whether the creation of a1 is before the creation of a2
 */
function compareArtifacts(a1, a2) {
	var d1 = new Date(a1.creation);
	var d2 = new Date(a2.creation);
	return d1.getTime() < d2.getTime();
}
